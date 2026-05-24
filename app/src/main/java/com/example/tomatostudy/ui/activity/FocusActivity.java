package com.example.tomatostudy.ui.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.service.FocusTimerContract;
import com.example.tomatostudy.service.FocusTimerService;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class FocusActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_FOCUS_MODE = "focus_mode";
    public static final String EXTRA_FOCUS_MINUTES = "focus_minutes";
    public static final String EXTRA_REST_MINUTES = "rest_minutes";
    public static final String EXTRA_BACKGROUND_RES = "background_res";

    private static final int STATE_FOCUSING = FocusTimerContract.STATE_FOCUSING;
    private static final int STATE_PAUSED = FocusTimerContract.STATE_PAUSED;
    private static final int STATE_FINISHED = FocusTimerContract.STATE_FINISHED;
    private static final int STATE_RESTING = FocusTimerContract.STATE_RESTING;

    private static final String KEY_TIMER_STATE = "timer_state";
    private static final String KEY_ELAPSED_SECONDS = "elapsed_seconds";
    private static final String KEY_REMAINING_SECONDS = "remaining_seconds";
    private static final String KEY_REST_REMAINING_SECONDS = "rest_remaining_seconds";
    private static final String KEY_FOCUS_START_TIME = "focus_start_time";
    private static final String KEY_FOCUS_RECORD_SAVED = "focus_record_saved";
    private static final String KEY_FOCUS_START_ELAPSED_REALTIME = "focus_start_elapsed_realtime";
    private static final String KEY_FOCUS_PAUSED_MILLIS = "focus_paused_millis";
    private static final String KEY_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME = "focus_pause_started_elapsed_realtime";
    private static final String KEY_REST_START_ELAPSED_REALTIME = "rest_start_elapsed_realtime";
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            refreshTimerFromClock();
        }
    };
    private final BroadcastReceiver focusTimerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            applyServiceState(intent);
        }
    };

    private int taskId;
    private String taskTitle;
    private String focusMode;
    private int focusMinutes;
    private int restMinutes;
    private String backgroundRes;
    private int timerState = STATE_FOCUSING;
    private int elapsedSeconds;
    private int remainingSeconds;
    private int restRemainingSeconds;
    private long focusStartTime;
    private long focusStartElapsedRealtime;
    private long focusPausedMillis;
    private long focusPauseStartedElapsedRealtime;
    private long restStartElapsedRealtime;
    private boolean focusRecordSaved;
    private boolean serviceReceiverRegistered;

    private TextView pageTitleText;
    private TextView taskTitleText;
    private TextView statusText;
    private TextView timeText;
    private TextView modeText;
    private TextView parameterText;
    private TextView backgroundText;
    private MaterialButton pauseButton;
    private MaterialButton finishButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);
        //获取弹窗权限
        requestPostNotificationPermissionIfNeeded();
        //读取从任务卡片传来的 taskId、时长、模式
        readTaskInfo();
        bindViews();
        //第一次全新打开，并且不是从计时通知栏回来
        if (savedInstanceState == null && !isOpenedFromTimerNotification()) {
            startFocusTimer();
        } else {
            //之前有实例，但是由于旋转或者从后台回到activity，就需要重建
            if (savedInstanceState != null) {
                restoreTimerState(savedInstanceState);
            }
            applyCurrentStateToViews();//根据当前计时状态（休息或者专注），把页面上的文字和按钮刷新成正确样子。
            refreshTimerFromClock();//同步计时
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readTaskInfo();
        applyCurrentStateToViews();
        requestServiceSnapshot();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerFocusTimerReceiver();
        requestServiceSnapshot();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTimerFromClock();
    }

    @Override
    protected void onStop() {
        unregisterFocusTimerReceiver();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        syncTimerCounters(false);
        outState.putInt(KEY_TIMER_STATE, timerState);
        outState.putInt(KEY_ELAPSED_SECONDS, elapsedSeconds);
        outState.putInt(KEY_REMAINING_SECONDS, remainingSeconds);
        outState.putInt(KEY_REST_REMAINING_SECONDS, restRemainingSeconds);
        outState.putLong(KEY_FOCUS_START_TIME, focusStartTime);
        outState.putBoolean(KEY_FOCUS_RECORD_SAVED, focusRecordSaved);
        outState.putLong(KEY_FOCUS_START_ELAPSED_REALTIME, focusStartElapsedRealtime);
        outState.putLong(KEY_FOCUS_PAUSED_MILLIS, focusPausedMillis);
        outState.putLong(KEY_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME, focusPauseStartedElapsedRealtime);
        outState.putLong(KEY_REST_START_ELAPSED_REALTIME, restStartElapsedRealtime);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }
//读任务参数
    private void readTaskInfo() {
        taskId = getIntent().getIntExtra(EXTRA_TASK_ID, taskId);
        taskTitle = getIntent().getStringExtra(EXTRA_TASK_TITLE);
        focusMode = getIntent().getStringExtra(EXTRA_FOCUS_MODE);
        focusMinutes = getIntent().getIntExtra(EXTRA_FOCUS_MINUTES, focusMinutes > 0 ? focusMinutes : 25);
        restMinutes = getIntent().getIntExtra(EXTRA_REST_MINUTES, restMinutes > 0 ? restMinutes : 5);
        backgroundRes = getIntent().getStringExtra(EXTRA_BACKGROUND_RES);

        if (TextUtils.isEmpty(taskTitle)) {
            taskTitle = getString(R.string.focus_unknown_task);
        }
        if (TextUtils.isEmpty(focusMode)) {
            focusMode = Task.FOCUS_MODE_FORWARD;
        }
        if (focusMinutes <= 0) {
            focusMinutes = 25;
        }
        if (restMinutes <= 0) {
            restMinutes = 5;
        }
        if (remainingSeconds <= 0) {
            remainingSeconds = getFocusTotalSeconds();
        }
        if (restRemainingSeconds <= 0) {
            restRemainingSeconds = getRestTotalSeconds();
        }
    }
//找控件、绑按钮
    private void bindViews() {
        pageTitleText = findViewById(R.id.focusPageTitleText);
        taskTitleText = findViewById(R.id.focusTaskTitleText);
        statusText = findViewById(R.id.focusStatusText);
        timeText = findViewById(R.id.focusTimeText);
        modeText = findViewById(R.id.focusModeText);
        parameterText = findViewById(R.id.focusParameterText);
        backgroundText = findViewById(R.id.focusBackgroundText);
        pauseButton = findViewById(R.id.focusPauseButton);
        finishButton = findViewById(R.id.focusFinishButton);

        applyCurrentStateToViews();
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePauseState();
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFinishConfirmDialog();
            }
        });
    }

    private void startFocusTimer() {
        timerState = STATE_FOCUSING;
        focusStartTime = System.currentTimeMillis();
        focusStartElapsedRealtime = SystemClock.elapsedRealtime();
        focusPausedMillis = 0L;
        focusPauseStartedElapsedRealtime = 0L;
        restStartElapsedRealtime = 0L;
        elapsedSeconds = 0;
        remainingSeconds = getFocusTotalSeconds();
        restRemainingSeconds = getRestTotalSeconds();
        focusRecordSaved = false;
        applyCurrentStateToViews();
        startFocusTimerService();
        scheduleNextTimerTick();
    }

    private void refreshTimerFromClock() {
        syncTimerCounters(true);
    }

    private void syncTimerCounters(boolean allowFinish) {
        if (timerState == STATE_RESTING) {
            syncRestTimer(allowFinish);
            return;
        }
        if (timerState == STATE_PAUSED) {
            syncFocusTimer(false);
            return;
        }
        if (timerState == STATE_FOCUSING) {
            syncFocusTimer(allowFinish);
        }
    }
//刷新专注时间
    private void syncFocusTimer(boolean allowFinish) {
        elapsedSeconds = calculateFocusElapsedSeconds();
        remainingSeconds = Math.max(0, getFocusTotalSeconds() - elapsedSeconds);

        if (elapsedSeconds >= getFocusTotalSeconds()) {
            //修正数字，专注时间不能超过设置时间，剩余时间不能为负数
            elapsedSeconds = getFocusTotalSeconds();
            remainingSeconds = 0;
            timeText.setText(Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)
                    ? formatSeconds(remainingSeconds)
                    : formatSeconds(elapsedSeconds));
            //如果是true这次结束是计时器自然到点结束的，如果是false说明暂停任务，只是同步专注时间
            if (allowFinish) {
                finishFocus(true);
            }
            return;
        }

        timeText.setText(Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)
                ? formatSeconds(remainingSeconds)
                : formatSeconds(elapsedSeconds));
        if (allowFinish) {
            scheduleNextTimerTick();
        }
    }
//刷新休息时间
    private void syncRestTimer(boolean allowFinish) {
        long elapsedMillis = Math.max(0L, SystemClock.elapsedRealtime() - restStartElapsedRealtime);
        int restElapsedSeconds = (int) (elapsedMillis / 1000L);
        restRemainingSeconds = Math.max(0, getRestTotalSeconds() - restElapsedSeconds);
        timeText.setText(formatSeconds(restRemainingSeconds));//同步

        if (restRemainingSeconds <= 0) {
            restRemainingSeconds = 0;
            if (allowFinish) {
                finishRestAndReturnHome(true);
            }
            return;
        }

        if (allowFinish) {
            scheduleNextTimerTick();
        }
    }

    private void togglePauseState() {
        if (timerState == STATE_RESTING || timerState == STATE_FINISHED) {
            return;
        }

        if (timerState == STATE_FOCUSING) {
            syncFocusTimer(false);
            timerState = STATE_PAUSED;
            //停止 Activity 自己的每秒刷新
            timerHandler.removeCallbacks(timerRunnable);
            //记录“从什么时候开始暂停”
            focusPauseStartedElapsedRealtime = SystemClock.elapsedRealtime();
            //刷新页面时间
            applyCurrentStateToViews();
            //后台计时器也暂停
            sendTimerServiceAction(FocusTimerContract.ACTION_PAUSE_FOCUS);
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (focusPauseStartedElapsedRealtime > 0L) {
            focusPausedMillis += Math.max(0L, now - focusPauseStartedElapsedRealtime);
        }
        focusPauseStartedElapsedRealtime = 0L;
        timerState = STATE_FOCUSING;
        applyCurrentStateToViews();
        sendTimerServiceAction(FocusTimerContract.ACTION_RESUME_FOCUS);
        syncFocusTimer(true);
    }

    private void showFinishConfirmDialog() {
        if (timerState == STATE_RESTING) {
            sendTimerServiceAction(FocusTimerContract.ACTION_STOP_REST);
            finishRestAndReturnHome(false);
            return;
        }
        if (timerState == STATE_FINISHED) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.focus_finish_confirm_title)
                .setMessage(R.string.focus_finish_confirm_message)
                .setNegativeButton(R.string.focus_finish_confirm_negative, null)
                .setPositiveButton(R.string.focus_finish_confirm_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishFocus(false);
                    }
                })
                .show();
    }

    private void finishFocus(boolean completedByTimer) {
        syncFocusTimer(false);
        timerState = STATE_FINISHED;
        timerHandler.removeCallbacks(timerRunnable);
        requestServiceCompleteFocus(completedByTimer);
    }

    private void finishRestAndReturnHome(boolean finishedByTimer) {
        timerState = STATE_FINISHED;
        timerHandler.removeCallbacks(timerRunnable);
        int tipRes = finishedByTimer ? R.string.rest_finished_tip : R.string.rest_stop_tip;
        Toast.makeText(this, tipRes, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void applyServiceState(Intent intent) {
        if (intent == null) {
            return;
        }

        taskId = intent.getIntExtra(EXTRA_TASK_ID, taskId);
        taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        focusMode = intent.getStringExtra(EXTRA_FOCUS_MODE);
        focusMinutes = intent.getIntExtra(EXTRA_FOCUS_MINUTES, focusMinutes);
        restMinutes = intent.getIntExtra(EXTRA_REST_MINUTES, restMinutes);
        backgroundRes = intent.getStringExtra(EXTRA_BACKGROUND_RES);
        if (TextUtils.isEmpty(taskTitle)) {
            taskTitle = getString(R.string.focus_unknown_task);
        }
        if (TextUtils.isEmpty(focusMode)) {
            focusMode = Task.FOCUS_MODE_FORWARD;
        }

        timerState = intent.getIntExtra(FocusTimerContract.EXTRA_TIMER_STATE, timerState);
        elapsedSeconds = intent.getIntExtra(FocusTimerContract.EXTRA_ELAPSED_SECONDS, elapsedSeconds);
        remainingSeconds = intent.getIntExtra(FocusTimerContract.EXTRA_REMAINING_SECONDS, remainingSeconds);
        restRemainingSeconds = intent.getIntExtra(FocusTimerContract.EXTRA_REST_REMAINING_SECONDS, restRemainingSeconds);
        focusStartTime = intent.getLongExtra(FocusTimerContract.EXTRA_FOCUS_START_TIME, focusStartTime);
        focusStartElapsedRealtime = intent.getLongExtra(
                FocusTimerContract.EXTRA_FOCUS_START_ELAPSED_REALTIME,
                focusStartElapsedRealtime
        );
        focusPausedMillis = intent.getLongExtra(FocusTimerContract.EXTRA_FOCUS_PAUSED_MILLIS, focusPausedMillis);
        focusPauseStartedElapsedRealtime = intent.getLongExtra(
                FocusTimerContract.EXTRA_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME,
                focusPauseStartedElapsedRealtime
        );
        restStartElapsedRealtime = intent.getLongExtra(
                FocusTimerContract.EXTRA_REST_START_ELAPSED_REALTIME,
                restStartElapsedRealtime
        );
        focusRecordSaved = intent.getBooleanExtra(FocusTimerContract.EXTRA_FOCUS_RECORD_SAVED, focusRecordSaved);

        applyCurrentStateToViews();
        timerHandler.removeCallbacks(timerRunnable);
        if (timerState == STATE_FOCUSING || timerState == STATE_RESTING) {
            scheduleNextTimerTick();
        }

        handleServiceEvent(intent);
    }

    private void handleServiceEvent(Intent intent) {
        String event = intent.getStringExtra(FocusTimerContract.EXTRA_EVENT);
        boolean saveSuccess = intent.getBooleanExtra(FocusTimerContract.EXTRA_SAVE_SUCCESS, true);
        boolean completedByTimer = intent.getBooleanExtra(FocusTimerContract.EXTRA_COMPLETED_BY_TIMER, false);

        if (FocusTimerContract.EVENT_FOCUS_COMPLETED.equals(event)) {
            if (!saveSuccess) {
                Toast.makeText(this, R.string.focus_record_save_fail_tip, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            int tipRes = completedByTimer
                    ? R.string.focus_countdown_finished_tip
                    : R.string.focus_finished_tip;
            Toast.makeText(this, tipRes, Toast.LENGTH_SHORT).show();
            return;
        }

        if (FocusTimerContract.EVENT_REST_COMPLETED.equals(event)) {
            finishRestAndReturnHome(true);
            return;
        }

        String finishedReason = intent.getStringExtra(FocusTimerContract.EXTRA_FINISHED_REASON);
        if (timerState == STATE_FINISHED
                && FocusTimerContract.FINISHED_REASON_REST_COMPLETED.equals(finishedReason)) {
            finish();
        } else if (timerState == STATE_FINISHED
                && FocusTimerContract.FINISHED_REASON_FOCUS_SAVE_FAILED.equals(finishedReason)) {
            Toast.makeText(this, R.string.focus_record_save_fail_tip, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
//根据当前计时状态，把页面上的文字和按钮刷新成正确样子。
    private void applyCurrentStateToViews() {
        taskTitleText.setText(taskTitle);
        if (timerState == STATE_RESTING) {
            pageTitleText.setText(R.string.rest_page_title);
            statusText.setText(R.string.rest_status_running);
            timeText.setText(formatSeconds(restRemainingSeconds));
            modeText.setText(R.string.rest_mode_title);
            parameterText.setText(getString(R.string.rest_parameter_detail, restMinutes));
            backgroundText.setText(R.string.rest_background_tip);
            pauseButton.setEnabled(false);
            pauseButton.setText(R.string.rest_button_placeholder);
            finishButton.setText(R.string.rest_finish_button);
            return;
        }

        pageTitleText.setText(R.string.focus_page_title);
        statusText.setText(timerState == STATE_PAUSED ? R.string.focus_status_paused : R.string.focus_status_running);
        timeText.setText(Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)
                ? formatSeconds(remainingSeconds)
                : formatSeconds(elapsedSeconds));
        modeText.setText(getFocusModeText());
        parameterText.setText(getString(R.string.focus_parameter_detail, focusMinutes, restMinutes));
        backgroundText.setText(TextUtils.isEmpty(backgroundRes)
                ? getString(R.string.focus_default_background)
                : getString(R.string.focus_background_detail, backgroundRes));
        pauseButton.setEnabled(timerState != STATE_FINISHED);
        pauseButton.setText(timerState == STATE_PAUSED ? R.string.focus_continue_button : R.string.focus_pause_button);
        finishButton.setText(R.string.focus_finish_button);
    }
//重建状态
    private void restoreTimerState(Bundle savedInstanceState) {
        timerState = savedInstanceState.getInt(KEY_TIMER_STATE, STATE_FOCUSING);
        elapsedSeconds = savedInstanceState.getInt(KEY_ELAPSED_SECONDS, 0);
        remainingSeconds = savedInstanceState.getInt(KEY_REMAINING_SECONDS, getFocusTotalSeconds());
        restRemainingSeconds = savedInstanceState.getInt(KEY_REST_REMAINING_SECONDS, getRestTotalSeconds());
        focusStartTime = savedInstanceState.getLong(KEY_FOCUS_START_TIME, 0L);
        focusRecordSaved = savedInstanceState.getBoolean(KEY_FOCUS_RECORD_SAVED, false);
        focusStartElapsedRealtime = savedInstanceState.getLong(KEY_FOCUS_START_ELAPSED_REALTIME, 0L);
        focusPausedMillis = savedInstanceState.getLong(KEY_FOCUS_PAUSED_MILLIS, 0L);
        focusPauseStartedElapsedRealtime = savedInstanceState.getLong(KEY_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME, 0L);
        restStartElapsedRealtime = savedInstanceState.getLong(KEY_REST_START_ELAPSED_REALTIME, 0L);
//如果没有恢复到专注开始的 elapsedRealtime，就根据“当前时间 - 已经专注的秒数”反推出一个开始时间。
        if (focusStartElapsedRealtime <= 0L) {
            focusStartElapsedRealtime = SystemClock.elapsedRealtime() - (elapsedSeconds * 1000L);
        }
        if (focusStartTime <= 0L) {
            focusStartTime = System.currentTimeMillis() - (elapsedSeconds * 1000L);
        }
        if (timerState == STATE_RESTING && restStartElapsedRealtime <= 0L) {
            int restElapsedSeconds = Math.max(0, getRestTotalSeconds() - restRemainingSeconds);
            restStartElapsedRealtime = SystemClock.elapsedRealtime() - (restElapsedSeconds * 1000L);
        }
    }
//已专注时间 = 当前时间 - 专注开始时间 - 暂停总时长
    private int calculateFocusElapsedSeconds() {
        if (focusStartElapsedRealtime <= 0L) {
            return Math.max(0, elapsedSeconds);
        }

        long now = timerState == STATE_PAUSED && focusPauseStartedElapsedRealtime > 0L
                ? focusPauseStartedElapsedRealtime
                : SystemClock.elapsedRealtime();
        long elapsedMillis = now - focusStartElapsedRealtime - focusPausedMillis;
        return (int) (Math.max(0L, elapsedMillis) / 1000L);
    }
    //从 Android 13，也就是 API 33 开始，App 想发通知，需要用户授权 POST_NOTIFICATIONS 权限。
    private void requestPostNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
    }

    private void registerFocusTimerReceiver() {
        if (serviceReceiverRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter(FocusTimerContract.ACTION_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(focusTimerReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(focusTimerReceiver, filter);
        }
        serviceReceiverRegistered = true;
    }

    private void unregisterFocusTimerReceiver() {
        if (!serviceReceiverRegistered) {
            return;
        }
        unregisterReceiver(focusTimerReceiver);
        serviceReceiverRegistered = false;
    }

    private void startFocusTimerService() {
        Intent intent = new Intent(this, FocusTimerService.class);
        intent.setAction(FocusTimerContract.ACTION_START_FOCUS);
        putTimerExtras(intent);
        //启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
//请求 Service 结束本次专注
    private void requestServiceCompleteFocus(boolean completedByTimer) {
        Intent intent = new Intent(this, FocusTimerService.class);
        intent.setAction(FocusTimerContract.ACTION_COMPLETE_FOCUS);
        //EXTRA_COMPLETED_BY_TIMER 表示是不是计时自然结束的
        intent.putExtra(FocusTimerContract.EXTRA_COMPLETED_BY_TIMER, completedByTimer);
        sendTimerServiceIntent(intent);
    }
//向 Service 请求一份当前计时状态快照
    private void requestServiceSnapshot() {
        Intent intent = new Intent(this, FocusTimerService.class);
        intent.setAction(FocusTimerContract.ACTION_REQUEST_SNAPSHOT);
        sendTimerServiceIntent(intent);
    }

    private void sendTimerServiceAction(String action) {
        Intent intent = new Intent(this, FocusTimerService.class);
        intent.setAction(action);
        sendTimerServiceIntent(intent);
    }

    private void sendTimerServiceIntent(Intent intent) {
        try {
            startService(intent);
        } catch (IllegalStateException ignored) {
            // If Android refuses a background service command, the foreground timer snapshot
            // will be reconciled when the activity returns to the foreground.
        }
    }

    private void putTimerExtras(Intent intent) {
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        intent.putExtra(EXTRA_FOCUS_MODE, focusMode);
        intent.putExtra(EXTRA_FOCUS_MINUTES, focusMinutes);
        intent.putExtra(EXTRA_REST_MINUTES, restMinutes);
        intent.putExtra(EXTRA_BACKGROUND_RES, backgroundRes);
    }
//当前 FocusActivity 是不是用户从计时通知栏点进来的。
    private boolean isOpenedFromTimerNotification() {
        return getIntent().getBooleanExtra(FocusTimerContract.EXTRA_FROM_TIMER_NOTIFICATION, false);
    }

    private void scheduleNextTimerTick() {
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000L);
    }

    private String getFocusModeText() {
        if (Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)) {
            return getString(R.string.focus_mode_countdown);
        }
        return getString(R.string.focus_mode_forward);
    }

    private int getFocusTotalSeconds() {
        return Math.max(1, focusMinutes) * 60;
    }

    private int getRestTotalSeconds() {
        return Math.max(1, restMinutes) * 60;
    }

    private String formatSeconds(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int minutes = safeSeconds / 60;
        int seconds = safeSeconds % 60;
        return String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
    }
}

