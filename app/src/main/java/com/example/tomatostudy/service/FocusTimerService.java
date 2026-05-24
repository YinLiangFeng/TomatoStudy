package com.example.tomatostudy.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.tomatostudy.util.AppExecutors;

/**
 * 前台服务：在应用退至后台时，统筹管理专注计时器运行。
 * 本文件应保持精简：负责接收指令、推进计时器状态、
 * 启动 / 停止前台执行任务，并向 FocusActivity 广播状态。
 * 计时逻辑统一放在 FocusTimerState 中，通知相关逻辑放在 FocusTimerNotifier 中，
 * 数据持久化 / 数据库写入操作放在 FocusTimerStore 中。
 */
public class FocusTimerService extends Service {

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            tickTimer();
        }
    };

    private FocusTimerState state;
    private FocusTimerStore store;
    private FocusTimerNotifier notifier;
    private FocusTimerBroadcaster broadcaster;
    private boolean focusRecordSaveInProgress;

    @Override
    public void onCreate() {
        super.onCreate();
        state = new FocusTimerState();
        store = new FocusTimerStore(getApplicationContext());
        notifier = new FocusTimerNotifier(getApplicationContext());
        broadcaster = new FocusTimerBroadcaster(getApplicationContext());
        notifier.createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            store.restoreSnapshot(state);
            if (state.isTimerActive()) {
                //重新进入前台服务，安排下一次计时刷新，广播给 FocusActivity
                enterForeground();
                scheduleTick();
                broadcastState(null, true);
                //如果 Service 以后又被系统杀掉，请系统尽量重新创建它。
                return START_STICKY;
            }
            //恢复出来发现没有正在进行的计时任务
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (FocusTimerContract.ACTION_START_FOCUS.equals(action)) {
            startFocus(intent);
            return START_STICKY;
        }
        if (FocusTimerContract.ACTION_PAUSE_FOCUS.equals(action)) {
            pauseFocus();
            return START_STICKY;
        }
        if (FocusTimerContract.ACTION_RESUME_FOCUS.equals(action)) {
            resumeFocus();
            return START_STICKY;
        }
        if (FocusTimerContract.ACTION_COMPLETE_FOCUS.equals(action)) {
            completeFocus(intent.getBooleanExtra(FocusTimerContract.EXTRA_COMPLETED_BY_TIMER, false));
            return START_STICKY;
        }
        if (FocusTimerContract.ACTION_STOP_REST.equals(action)) {
            stopRest();
            return START_NOT_STICKY;
        }
        if (FocusTimerContract.ACTION_REQUEST_SNAPSHOT.equals(action)) {
            sendSnapshot(startId);
            return state.isTimerActive() ? START_STICKY : START_NOT_STICKY;
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }

    private void startFocus(Intent intent) {
        state.startFromIntent(intent);
        store.persistSnapshot(state);
        enterForeground();
        scheduleTick();
        broadcastState(null, true);
    }

    private void pauseFocus() {
        if (!state.isFocusRunning()) {
            broadcastState(null, true);
            return;
        }

        state.pause();
        //后台 Service 的秒级刷新停下来。
        timerHandler.removeCallbacks(timerRunnable);
        //更新通知栏
        notifier.updateOngoingNotification(state);
        broadcastState(null, true);
    }

    private void resumeFocus() {
        if (!state.isFocusPaused()) {
            broadcastState(null, true);
            return;
        }

        state.resume();
        notifier.updateOngoingNotification(state);
        scheduleTick();
        broadcastState(null, true);
    }

    private void completeFocus(boolean completedByTimer) {
        if (state.isResting()) {
            broadcastState(null, true);
            return;
        }
        if (focusRecordSaveInProgress) {
            broadcastState(null, true);
            return;
        }
        if (state.timerState == FocusTimerContract.STATE_FINISHED && state.focusRecordSaved) {
            startRestCountdown(completedByTimer, true);
            return;
        }

        state.markFocusFinished();
        timerHandler.removeCallbacks(timerRunnable);

        saveFocusRecordOnIo(completedByTimer);
    }

    private void saveFocusRecordOnIo(final boolean completedByTimer) {
        focusRecordSaveInProgress = true;
        final int taskId = state.taskId;
        final String taskTitle = state.taskTitle;
        final long focusStartTime = state.focusStartTime;
        final int elapsedSeconds = state.elapsedSeconds;
        final int focusTotalSeconds = state.getFocusTotalSeconds();
        final boolean focusRecordSaved = state.focusRecordSaved;

        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final boolean saveSuccess = store.saveFocusRecordSnapshot(
                        taskId,
                        taskTitle,
                        focusStartTime,
                        elapsedSeconds,
                        focusTotalSeconds,
                        focusRecordSaved,
                        completedByTimer
                );
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        handleFocusRecordSaved(completedByTimer, saveSuccess);
                    }
                });
            }
        });
    }

    private void handleFocusRecordSaved(boolean completedByTimer, boolean saveSuccess) {
        focusRecordSaveInProgress = false;
        if (!saveSuccess) {
            state.markFocusSaveFailed();
            notifier.notifyFocusSaveFailed(state);
            broadcastState(FocusTimerContract.EVENT_FOCUS_COMPLETED, false);
            stopForegroundCompat();
            stopSelf();
            return;
        }

        state.focusRecordSaved = true;
        if (completedByTimer) {
            notifier.showFocusCompletedAlert(state);
        }
        startRestCountdown(completedByTimer, true);
    }

    private void startRestCountdown(boolean completedByTimer, boolean saveSuccess) {
        state.startRest();
        notifier.updateOngoingNotification(state);
        scheduleTick();
        broadcastState(FocusTimerContract.EVENT_FOCUS_COMPLETED, saveSuccess, completedByTimer);
    }

    private void stopRest() {
        state.stopRest();
        timerHandler.removeCallbacks(timerRunnable);
        broadcastState(FocusTimerContract.EVENT_REST_STOPPED, true);
        stopForegroundCompat();
        stopSelf();
    }

    private void sendSnapshot(int startId) {
        if (!state.isTimerKnown()) {
            store.restoreSnapshot(state);
        }
        if (state.isTimerActive()) {
            enterForeground();
        }
        refreshCounters(true);
        broadcastState(null, true);
        if (!state.isTimerActive()) {
            stopSelf(startId);
        }
    }

    private void tickTimer() {
        refreshCounters(true);
    }

    private void refreshCounters(boolean allowComplete) {
        if (state.isFocusRunning()) {
            refreshFocusCounters(allowComplete);
            return;
        }
        if (state.isFocusPaused()) {
            state.refreshFocusCounters();
            broadcastState(null, true);
            return;
        }
        if (state.isResting()) {
            refreshRestCounters(allowComplete);
        }
    }

    private void refreshFocusCounters(boolean allowComplete) {
        state.refreshFocusCounters();
        notifier.updateOngoingNotification(state);

        if (state.isFocusFinished()) {
            if (allowComplete) {
                completeFocus(true);
            } else {
                broadcastState(null, true);
            }
            return;
        }

        broadcastState(null, true);
        if (allowComplete) {
            scheduleTick();
        }
    }

    private void refreshRestCounters(boolean allowComplete) {
        state.refreshRestCounters();
        notifier.updateOngoingNotification(state);

        if (state.isRestFinished()) {
            if (allowComplete) {
                completeRest();
            } else {
                broadcastState(null, true);
            }
            return;
        }

        broadcastState(null, true);
        if (allowComplete) {
            scheduleTick();
        }
    }

    private void completeRest() {
        state.completeRest();
        timerHandler.removeCallbacks(timerRunnable);
        notifier.showRestCompletedAlert();
        broadcastState(FocusTimerContract.EVENT_REST_COMPLETED, true);
        stopForegroundCompat();
        stopSelf();
    }
//重新变成前台服务
    private void enterForeground() {
        startForeground(
                FocusTimerNotifier.NOTIFICATION_TIMER_ID,
                notifier.buildOngoingNotification(state)
        );
    }

    private void scheduleTick() {
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000L);
    }

    private void broadcastState(String event, boolean saveSuccess) {
        broadcastState(event, saveSuccess, false);
    }

    private void broadcastState(String event, boolean saveSuccess, boolean completedByTimer) {
        store.persistSnapshot(state);
        broadcaster.broadcastState(state, event, saveSuccess, completedByTimer);
    }

    private void stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }
}
