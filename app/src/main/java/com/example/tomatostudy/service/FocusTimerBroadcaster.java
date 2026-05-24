package com.example.tomatostudy.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.tomatostudy.ui.activity.FocusActivity;

/**
 向专注活动页面广播服务状态
 本类维护状态变更的意图附加数据，将附加参数统一存放于此
 避免计时器服务内堆砌大量参数赋值代码
 */
class FocusTimerBroadcaster {

    private final Context context;

    FocusTimerBroadcaster(Context context) {
        this.context = context.getApplicationContext();
    }

    void broadcastState(FocusTimerState state, String event, boolean saveSuccess) {
        broadcastState(state, event, saveSuccess, false);
    }

    void broadcastState(FocusTimerState state,
                        String event,
                        boolean saveSuccess,
                        boolean completedByTimer) {
        Intent intent = new Intent(FocusTimerContract.ACTION_STATE_CHANGED);
        intent.setPackage(context.getPackageName());
        intent.putExtra(FocusActivity.EXTRA_TASK_ID, state.taskId);
        intent.putExtra(FocusActivity.EXTRA_TASK_TITLE, state.taskTitle);
        intent.putExtra(FocusActivity.EXTRA_FOCUS_MODE, state.focusMode);
        intent.putExtra(FocusActivity.EXTRA_FOCUS_MINUTES, state.focusMinutes);
        intent.putExtra(FocusActivity.EXTRA_REST_MINUTES, state.restMinutes);
        intent.putExtra(FocusActivity.EXTRA_BACKGROUND_RES, state.backgroundRes);
        intent.putExtra(FocusTimerContract.EXTRA_TIMER_STATE, state.timerState);
        intent.putExtra(FocusTimerContract.EXTRA_ELAPSED_SECONDS, state.elapsedSeconds);
        intent.putExtra(FocusTimerContract.EXTRA_REMAINING_SECONDS, state.remainingSeconds);
        intent.putExtra(FocusTimerContract.EXTRA_REST_REMAINING_SECONDS, state.restRemainingSeconds);
        intent.putExtra(FocusTimerContract.EXTRA_FOCUS_START_TIME, state.focusStartTime);
        intent.putExtra(
                FocusTimerContract.EXTRA_FOCUS_START_ELAPSED_REALTIME,
                state.focusStartElapsedRealtime
        );
        intent.putExtra(FocusTimerContract.EXTRA_FOCUS_PAUSED_MILLIS, state.focusPausedMillis);
        intent.putExtra(
                FocusTimerContract.EXTRA_FOCUS_PAUSE_STARTED_ELAPSED_REALTIME,
                state.focusPauseStartedElapsedRealtime
        );
        intent.putExtra(FocusTimerContract.EXTRA_REST_START_ELAPSED_REALTIME, state.restStartElapsedRealtime);
        intent.putExtra(FocusTimerContract.EXTRA_FOCUS_RECORD_SAVED, state.focusRecordSaved);
        intent.putExtra(FocusTimerContract.EXTRA_SAVE_SUCCESS, saveSuccess);
        intent.putExtra(FocusTimerContract.EXTRA_COMPLETED_BY_TIMER, completedByTimer);
        intent.putExtra(FocusTimerContract.EXTRA_FINISHED_REASON, state.finishedReason);
        if (!TextUtils.isEmpty(event)) {
            intent.putExtra(FocusTimerContract.EXTRA_EVENT, event);
        }
        context.sendBroadcast(intent);
    }
}
