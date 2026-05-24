package com.example.tomatostudy.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.tomatostudy.database.model.FocusRecord;
import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.database.model.User;
import com.example.tomatostudy.repository.FocusRepository;
import com.example.tomatostudy.repository.UserRepository;
import com.example.tomatostudy.util.TimeZoneUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 专注计时器的数据持久化类。
 本文件负责两项存储工作：在 SharedPreferences 中保存 / 恢复服务快照，
 以及将已完成的专注记录写入 SQLite。
 它不负责判断计时器何时结束，也不展示界面或通知。
 */
class FocusTimerStore {

    private static final String PREFS_NAME = "focus_timer_service_state";
    private static final String PREF_TASK_ID = "task_id";
    private static final String PREF_TASK_TITLE = "task_title";
    private static final String PREF_FOCUS_MODE = "focus_mode";
    private static final String PREF_FOCUS_MINUTES = "focus_minutes";
    private static final String PREF_REST_MINUTES = "rest_minutes";
    private static final String PREF_BACKGROUND_RES = "background_res";
    private static final String PREF_TIMER_STATE = "timer_state";
    private static final String PREF_ELAPSED_SECONDS = "elapsed_seconds";
    private static final String PREF_REMAINING_SECONDS = "remaining_seconds";
    private static final String PREF_REST_REMAINING_SECONDS = "rest_remaining_seconds";
    private static final String PREF_FOCUS_START_TIME = "focus_start_time";
    private static final String PREF_FOCUS_START_ELAPSED_REALTIME = "focus_start_elapsed_realtime";
    private static final String PREF_FOCUS_PAUSED_MILLIS = "focus_paused_millis";
    private static final String PREF_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME = "focus_pause_started_elapsed_realtime";
    private static final String PREF_REST_START_ELAPSED_REALTIME = "rest_start_elapsed_realtime";
    private static final String PREF_FOCUS_RECORD_SAVED = "focus_record_saved";
    private static final String PREF_FINISHED_REASON = "finished_reason";

    private final Context context;
    private final FocusRepository focusRepository;
    private final UserRepository userRepository;

    FocusTimerStore(Context context) {
        this.context = context.getApplicationContext();
        focusRepository = new FocusRepository(this.context);
        userRepository = new UserRepository(this.context);
    }

    void persistSnapshot(FocusTimerState state) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_TASK_ID, state.taskId)
                .putString(PREF_TASK_TITLE, state.taskTitle)
                .putString(PREF_FOCUS_MODE, state.focusMode)
                .putInt(PREF_FOCUS_MINUTES, state.focusMinutes)
                .putInt(PREF_REST_MINUTES, state.restMinutes)
                .putString(PREF_BACKGROUND_RES, state.backgroundRes)
                .putInt(PREF_TIMER_STATE, state.timerState)
                .putInt(PREF_ELAPSED_SECONDS, state.elapsedSeconds)
                .putInt(PREF_REMAINING_SECONDS, state.remainingSeconds)
                .putInt(PREF_REST_REMAINING_SECONDS, state.restRemainingSeconds)
                .putLong(PREF_FOCUS_START_TIME, state.focusStartTime)
                .putLong(PREF_FOCUS_START_ELAPSED_REALTIME, state.focusStartElapsedRealtime)
                .putLong(PREF_FOCUS_PAUSED_MILLIS, state.focusPausedMillis)
                .putLong(PREF_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME, state.focusPauseStartedElapsedRealtime)
                .putLong(PREF_REST_START_ELAPSED_REALTIME, state.restStartElapsedRealtime)
                .putBoolean(PREF_FOCUS_RECORD_SAVED, state.focusRecordSaved)
                .putString(PREF_FINISHED_REASON, state.finishedReason)
                .apply();
    }

    void restoreSnapshot(FocusTimerState state) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        state.taskId = prefs.getInt(PREF_TASK_ID, 0);
        state.taskTitle = prefs.getString(PREF_TASK_TITLE, "");
        state.focusMode = prefs.getString(PREF_FOCUS_MODE, Task.FOCUS_MODE_FORWARD);
        state.focusMinutes = prefs.getInt(PREF_FOCUS_MINUTES, 25);
        state.restMinutes = prefs.getInt(PREF_REST_MINUTES, 5);
        state.backgroundRes = prefs.getString(PREF_BACKGROUND_RES, "");
        state.timerState = prefs.getInt(PREF_TIMER_STATE, FocusTimerContract.STATE_FINISHED);
        state.elapsedSeconds = prefs.getInt(PREF_ELAPSED_SECONDS, 0);
        state.remainingSeconds = prefs.getInt(PREF_REMAINING_SECONDS, state.getFocusTotalSeconds());
        state.restRemainingSeconds = prefs.getInt(PREF_REST_REMAINING_SECONDS, state.getRestTotalSeconds());
        state.focusStartTime = prefs.getLong(PREF_FOCUS_START_TIME, 0L);
        state.focusStartElapsedRealtime = prefs.getLong(PREF_FOCUS_START_ELAPSED_REALTIME, 0L);
        state.focusPausedMillis = prefs.getLong(PREF_FOCUS_PAUSED_MILLIS, 0L);
        state.focusPauseStartedElapsedRealtime = prefs.getLong(PREF_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME, 0L);
        state.restStartElapsedRealtime = prefs.getLong(PREF_REST_START_ELAPSED_REALTIME, 0L);
        state.focusRecordSaved = prefs.getBoolean(PREF_FOCUS_RECORD_SAVED, false);
        state.finishedReason = prefs.getString(
                PREF_FINISHED_REASON,
                FocusTimerContract.FINISHED_REASON_NONE
        );
        state.normalizeTaskInfo();
    }

    boolean saveFocusRecord(FocusTimerState state, boolean completedByTimer) {
        if (state.focusRecordSaved) {
            return true;
        }

        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null || state.taskId <= 0 || state.focusStartTime <= 0L) {
            return false;
        }

        int durationSeconds = completedByTimer
                ? Math.min(state.getFocusTotalSeconds(), Math.max(0, state.elapsedSeconds))
                : Math.max(0, state.elapsedSeconds);
        if (durationSeconds <= 0) {
            return false;
        }

        long endTime = System.currentTimeMillis();
        FocusRecord record = new FocusRecord();
        record.setUserId(currentUser.getId());
        record.setTaskId(state.taskId);
        record.setTaskTitle(state.taskTitle);
        record.setStartTime(state.focusStartTime);
        record.setEndTime(endTime);
        record.setDurationMinutes(Math.max(1, (durationSeconds + 59) / 60));
        record.setCompleted(true);
        record.setCreatedDate(formatDate(endTime));

        state.focusRecordSaved = focusRepository.saveFocusRecord(record) > 0;
        return state.focusRecordSaved;
    }

    private String formatDate(long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        dateFormat.setTimeZone(TimeZoneUtils.CHINA_TIME_ZONE);
        return dateFormat.format(new Date(timeMillis));
    }
}
