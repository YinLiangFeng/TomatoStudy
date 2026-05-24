package com.example.tomatostudy.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.ui.activity.FocusActivity;
import com.example.tomatostudy.ui.activity.MainActivity;

import java.util.Locale;

/**
 专注计时器的通知与震动辅助工具类
 本文件负责安卓通知渠道、前台通知内容、完成提醒及震动功能
 不修改计时器状态、不持久化数据，也不处理服务指令
 */
class FocusTimerNotifier {

    static final int NOTIFICATION_TIMER_ID = 1001;

    private static final String CHANNEL_TIMER = "focus_timer_ongoing";
    private static final String CHANNEL_ALERT = "focus_timer_alert";
    private static final int NOTIFICATION_FOCUS_DONE_ID = 1002;
    private static final int NOTIFICATION_REST_DONE_ID = 1003;
    private static final long[] VIBRATION_PATTERN = new long[]{0L, 350L, 120L, 350L};

    private final Context context;

    FocusTimerNotifier(Context context) {
        this.context = context.getApplicationContext();
    }
//创建通知渠道，分别创建了“专注计时”和“专注提醒”两个通知渠道。
    void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel timerChannel = new NotificationChannel(
                CHANNEL_TIMER,
                "专注计时",
                NotificationManager.IMPORTANCE_LOW
        );
        timerChannel.setDescription("显示正在进行的专注和休息计时");

        NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ALERT,
                "专注提醒",
                NotificationManager.IMPORTANCE_HIGH
        );
        alertChannel.setDescription("专注结束和休息结束提醒");
        alertChannel.enableVibration(true);
        alertChannel.setVibrationPattern(VIBRATION_PATTERN);

        NotificationManager manager = getNotificationManager();
        manager.createNotificationChannel(timerChannel);
        manager.createNotificationChannel(alertChannel);
    }
//正在计时的前台服务通知
    Notification buildOngoingNotification(FocusTimerState state) {
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_TIMER)
                : new Notification.Builder(context);

        builder.setSmallIcon(R.drawable.ic_nav_lock_focus)
                .setContentTitle(getOngoingTitle(state))
                .setContentText(getOngoingContentText(state))
                .setContentIntent(buildFocusPendingIntent(state))
                .setOngoing(state.isTimerActive())
                .setOnlyAlertOnce(true)
                .setShowWhen(false);

        return builder.build();
    }
//如果计时器还活着，就刷新通知栏。
    void updateOngoingNotification(FocusTimerState state) {
        if (state.isTimerActive()) {
            getNotificationManager().notify(NOTIFICATION_TIMER_ID, buildOngoingNotification(state));
        }
    }

    void showFocusCompletedAlert(FocusTimerState state) {
        showAlertNotification(
                NOTIFICATION_FOCUS_DONE_ID,
                "专注结束",
                "本次专注已完成，已自动进入休息时间。",
                buildFocusPendingIntent(state)
        );
        vibrateOnce();
    }

    void showRestCompletedAlert() {
        showAlertNotification(
                NOTIFICATION_REST_DONE_ID,
                "休息结束",
                "休息时间结束，准备回到下一轮学习吧。",
                buildMainPendingIntent()
        );
        vibrateOnce();
    }

    void notifyFocusSaveFailed(FocusTimerState state) {
        showAlertNotification(
                NOTIFICATION_FOCUS_DONE_ID,
                "专注记录保存失败",
                "请确认已登录后重新开始专注。",
                buildFocusPendingIntent(state)
        );
    }

    private PendingIntent buildFocusPendingIntent(FocusTimerState state) {
        Intent intent = new Intent(context, FocusActivity.class);
        intent.putExtra(FocusActivity.EXTRA_TASK_ID, state.taskId);
        intent.putExtra(FocusActivity.EXTRA_TASK_TITLE, state.taskTitle);
        intent.putExtra(FocusActivity.EXTRA_FOCUS_MODE, state.focusMode);
        intent.putExtra(FocusActivity.EXTRA_FOCUS_MINUTES, state.focusMinutes);
        intent.putExtra(FocusActivity.EXTRA_REST_MINUTES, state.restMinutes);
        intent.putExtra(FocusActivity.EXTRA_BACKGROUND_RES, state.backgroundRes);
        intent.putExtra(FocusTimerContract.EXTRA_FROM_TIMER_NOTIFICATION, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | getImmutableFlag()
        );
    }

    private PendingIntent buildMainPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | getImmutableFlag()
        );
    }

    private String getOngoingTitle(FocusTimerState state) {
        if (state.timerState == FocusTimerContract.STATE_RESTING) {
            return "休息中";
        }
        if (state.timerState == FocusTimerContract.STATE_PAUSED) {
            return "专注已暂停";
        }
        return "专注中";
    }

    private String getOngoingContentText(FocusTimerState state) {
        String safeTitle = TextUtils.isEmpty(state.taskTitle) ? "学习任务" : state.taskTitle;
        if (state.timerState == FocusTimerContract.STATE_RESTING) {
            return safeTitle + " · 休息剩余 " + formatSeconds(state.restRemainingSeconds);
        }
        if (Task.FOCUS_MODE_COUNTDOWN.equals(state.focusMode)) {
            return safeTitle + " · 专注剩余 " + formatSeconds(state.remainingSeconds);
        }
        return safeTitle + " · 已专注 " + formatSeconds(state.elapsedSeconds)
                + " / " + formatSeconds(state.getFocusTotalSeconds());
    }

    private void showAlertNotification(int notificationId,
                                       String title,
                                       String content,
                                       PendingIntent contentIntent) {
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ALERT)
                : new Notification.Builder(context);

        builder.setSmallIcon(R.drawable.ic_nav_lock_focus)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }

        getNotificationManager().notify(notificationId, builder.build());
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private int getImmutableFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_IMMUTABLE;
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    private void vibrateOnce() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1));
        } else {
            vibrator.vibrate(VIBRATION_PATTERN, -1);
        }
    }

    private String formatSeconds(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int minutes = safeSeconds / 60;
        int seconds = safeSeconds % 60;
        return String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
    }
}
