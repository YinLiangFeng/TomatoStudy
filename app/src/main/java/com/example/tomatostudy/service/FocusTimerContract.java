package com.example.tomatostudy.service;

/**
 由专注页面（FocusActivity）与计时器服务（FocusTimerService）共享的专注计时器协议。
 本文件仅定义操作类型、附加数据、事件及状态标识。
 不包含计时运算、通知代码、数据库代码或安卓生命周期相关逻辑。
 当两个类需要统一使用同一个意图（Intent）字段名时，统一在此处定义。
 */
public final class FocusTimerContract {

    public static final String ACTION_START_FOCUS = "com.example.tomatostudy.action.START_FOCUS";
    public static final String ACTION_PAUSE_FOCUS = "com.example.tomatostudy.action.PAUSE_FOCUS";
    public static final String ACTION_RESUME_FOCUS = "com.example.tomatostudy.action.RESUME_FOCUS";
    public static final String ACTION_COMPLETE_FOCUS = "com.example.tomatostudy.action.COMPLETE_FOCUS";
    public static final String ACTION_STOP_REST = "com.example.tomatostudy.action.STOP_REST";
    public static final String ACTION_REQUEST_SNAPSHOT = "com.example.tomatostudy.action.REQUEST_SNAPSHOT";
    public static final String ACTION_STATE_CHANGED = "com.example.tomatostudy.action.STATE_CHANGED";

    public static final String EXTRA_TIMER_STATE = "timer_state";
    public static final String EXTRA_ELAPSED_SECONDS = "elapsed_seconds";
    public static final String EXTRA_REMAINING_SECONDS = "remaining_seconds";
    public static final String EXTRA_REST_REMAINING_SECONDS = "rest_remaining_seconds";
    public static final String EXTRA_FOCUS_START_TIME = "focus_start_time";
    public static final String EXTRA_FOCUS_START_ELAPSED_REALTIME = "focus_start_elapsed_realtime";
    public static final String EXTRA_FOCUS_PAUSED_MILLIS = "focus_paused_millis";
    public static final String EXTRA_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME = "focus_pause_started_elapsed_realtime";
    public static final String EXTRA_REST_START_ELAPSED_REALTIME = "rest_start_elapsed_realtime";
    public static final String EXTRA_FOCUS_RECORD_SAVED = "focus_record_saved";
    public static final String EXTRA_COMPLETED_BY_TIMER = "completed_by_timer";
    public static final String EXTRA_SAVE_SUCCESS = "save_success";
    public static final String EXTRA_EVENT = "event";
    public static final String EXTRA_FINISHED_REASON = "finished_reason";
    public static final String EXTRA_FROM_TIMER_NOTIFICATION = "from_timer_notification";

    public static final String EVENT_FOCUS_COMPLETED = "focus_completed";
    public static final String EVENT_REST_COMPLETED = "rest_completed";
    public static final String EVENT_REST_STOPPED = "rest_stopped";

    public static final String FINISHED_REASON_NONE = "";
    public static final String FINISHED_REASON_REST_COMPLETED = "rest_completed";
    public static final String FINISHED_REASON_REST_STOPPED = "rest_stopped";
    public static final String FINISHED_REASON_FOCUS_SAVE_FAILED = "focus_save_failed";

    public static final int STATE_FOCUSING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_FINISHED = 3;
    public static final int STATE_RESTING = 4;

    private FocusTimerContract() {
    }
}
