package com.example.tomatostudy.service;

import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.ui.activity.FocusActivity;

/**
 单次专注任务的可变状态与时间计算逻辑。
 本文件用于处理这类逻辑：“已过去多少秒？”、“专注是否已完成？”
 以及 “剩余休息时间是多少？”。
 它刻意不进行数据保存、不发送通知，也不与安卓服务交互。
 */
class FocusTimerState {

    int taskId;
    String taskTitle;
    String focusMode;
    int focusMinutes;
    int restMinutes;
    String backgroundRes;
    int timerState = FocusTimerContract.STATE_FINISHED;
    int elapsedSeconds;
    int remainingSeconds;
    int restRemainingSeconds;
    long focusStartTime;
    long focusStartElapsedRealtime;
    long focusPausedMillis;
    long focusPauseStartedElapsedRealtime;
    long restStartElapsedRealtime;
    boolean focusRecordSaved;
    String finishedReason = FocusTimerContract.FINISHED_REASON_NONE;

    void startFromIntent(Intent intent) {
        taskId = intent.getIntExtra(FocusActivity.EXTRA_TASK_ID, 0);
        taskTitle = intent.getStringExtra(FocusActivity.EXTRA_TASK_TITLE);
        focusMode = intent.getStringExtra(FocusActivity.EXTRA_FOCUS_MODE);
        focusMinutes = intent.getIntExtra(FocusActivity.EXTRA_FOCUS_MINUTES, 25);
        restMinutes = intent.getIntExtra(FocusActivity.EXTRA_REST_MINUTES, 5);
        backgroundRes = intent.getStringExtra(FocusActivity.EXTRA_BACKGROUND_RES);

        normalizeTaskInfo();
        timerState = FocusTimerContract.STATE_FOCUSING;
        elapsedSeconds = 0;
        remainingSeconds = getFocusTotalSeconds();
        restRemainingSeconds = getRestTotalSeconds();
        focusStartTime = System.currentTimeMillis();
        focusStartElapsedRealtime = SystemClock.elapsedRealtime();
        focusPausedMillis = 0L;
        focusPauseStartedElapsedRealtime = 0L;
        restStartElapsedRealtime = 0L;
        focusRecordSaved = false;
        finishedReason = FocusTimerContract.FINISHED_REASON_NONE;
    }

    void normalizeTaskInfo() {
        if (TextUtils.isEmpty(taskTitle)) {
            taskTitle = "学习任务";
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
    }

    void pause() {
        refreshFocusCounters();
        timerState = FocusTimerContract.STATE_PAUSED;
        //记录 Service 是从什么时候开始暂停的
        focusPauseStartedElapsedRealtime = SystemClock.elapsedRealtime();
    }

    void resume() {
        long now = SystemClock.elapsedRealtime();
        if (focusPauseStartedElapsedRealtime > 0L) {
            focusPausedMillis += Math.max(0L, now - focusPauseStartedElapsedRealtime);
        }
        focusPauseStartedElapsedRealtime = 0L;
        timerState = FocusTimerContract.STATE_FOCUSING;
    }

    void markFocusFinished() {
        refreshFocusCounters();
        timerState = FocusTimerContract.STATE_FINISHED;
    }

    void startRest() {
        timerState = FocusTimerContract.STATE_RESTING;
        restStartElapsedRealtime = SystemClock.elapsedRealtime();
        restRemainingSeconds = getRestTotalSeconds();
        finishedReason = FocusTimerContract.FINISHED_REASON_NONE;
    }

    void stopRest() {
        timerState = FocusTimerContract.STATE_FINISHED;
        finishedReason = FocusTimerContract.FINISHED_REASON_REST_STOPPED;
        restRemainingSeconds = Math.max(0, restRemainingSeconds);
    }

    void completeRest() {
        timerState = FocusTimerContract.STATE_FINISHED;
        finishedReason = FocusTimerContract.FINISHED_REASON_REST_COMPLETED;
        restRemainingSeconds = 0;
    }

    void markFocusSaveFailed() {
        timerState = FocusTimerContract.STATE_FINISHED;
        finishedReason = FocusTimerContract.FINISHED_REASON_FOCUS_SAVE_FAILED;
    }
//重新计算当前已专注时间、剩余时间
    void refreshFocusCounters() {
        elapsedSeconds = calculateFocusElapsedSeconds();
        remainingSeconds = Math.max(0, getFocusTotalSeconds() - elapsedSeconds);
        if (elapsedSeconds >= getFocusTotalSeconds()) {
            elapsedSeconds = getFocusTotalSeconds();
            remainingSeconds = 0;
        }
    }

    void refreshRestCounters() {
        long elapsedMillis = Math.max(0L, SystemClock.elapsedRealtime() - restStartElapsedRealtime);
        int restElapsedSeconds = (int) (elapsedMillis / 1000L);
        restRemainingSeconds = Math.max(0, getRestTotalSeconds() - restElapsedSeconds);
    }

    boolean isTimerKnown() {
        return focusStartTime > 0L || taskId > 0;
    }

    boolean isTimerActive() {
        return timerState == FocusTimerContract.STATE_FOCUSING
                || timerState == FocusTimerContract.STATE_PAUSED
                || timerState == FocusTimerContract.STATE_RESTING;
    }

    boolean isFocusRunning() {
        return timerState == FocusTimerContract.STATE_FOCUSING;
    }

    boolean isFocusPaused() {
        return timerState == FocusTimerContract.STATE_PAUSED;
    }

    boolean isResting() {
        return timerState == FocusTimerContract.STATE_RESTING;
    }

    boolean isFocusFinished() {
        return elapsedSeconds >= getFocusTotalSeconds();
    }

    boolean isRestFinished() {
        return restRemainingSeconds <= 0;
    }

    int getFocusTotalSeconds() {
        return Math.max(1, focusMinutes) * 60;
    }

    int getRestTotalSeconds() {
        return Math.max(1, restMinutes) * 60;
    }

    private int calculateFocusElapsedSeconds() {
        if (focusStartElapsedRealtime <= 0L) {
            return Math.max(0, elapsedSeconds);
        }

        long now = timerState == FocusTimerContract.STATE_PAUSED && focusPauseStartedElapsedRealtime > 0L
                ? focusPauseStartedElapsedRealtime
                : SystemClock.elapsedRealtime();
        long elapsedMillis = now - focusStartElapsedRealtime - focusPausedMillis;
        return (int) (Math.max(0L, elapsedMillis) / 1000L);
    }
}
