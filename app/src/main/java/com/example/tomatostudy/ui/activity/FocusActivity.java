package com.example.tomatostudy.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.viewmodel.FocusViewModel;
import com.google.android.material.button.MaterialButton;

public class FocusActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_FOCUS_MODE = "focus_mode";
    public static final String EXTRA_FOCUS_MINUTES = "focus_minutes";
    public static final String EXTRA_REST_MINUTES = "rest_minutes";
    public static final String EXTRA_BACKGROUND_RES = "background_res";

    private static final int STATE_FOCUSING = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_FINISHED = 3;
    private static final int STATE_RESTING = 4;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimerOneSecond();
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
    private boolean focusRecordSaved;

    private FocusViewModel focusViewModel;
    private TextView pageTitleText;
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

        focusViewModel = new ViewModelProvider(this).get(FocusViewModel.class);
        readTaskInfo();
        bindViews();
        startFocusTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void readTaskInfo() {
        taskId = getIntent().getIntExtra(EXTRA_TASK_ID, 0);
        taskTitle = getIntent().getStringExtra(EXTRA_TASK_TITLE);
        focusMode = getIntent().getStringExtra(EXTRA_FOCUS_MODE);
        focusMinutes = getIntent().getIntExtra(EXTRA_FOCUS_MINUTES, 25);
        restMinutes = getIntent().getIntExtra(EXTRA_REST_MINUTES, 5);
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
        remainingSeconds = focusMinutes * 60;
        restRemainingSeconds = restMinutes * 60;
    }

    private void bindViews() {
        pageTitleText = findViewById(R.id.focusPageTitleText);
        TextView taskTitleText = findViewById(R.id.focusTaskTitleText);
        statusText = findViewById(R.id.focusStatusText);
        timeText = findViewById(R.id.focusTimeText);
        modeText = findViewById(R.id.focusModeText);
        parameterText = findViewById(R.id.focusParameterText);
        backgroundText = findViewById(R.id.focusBackgroundText);
        pauseButton = findViewById(R.id.focusPauseButton);
        finishButton = findViewById(R.id.focusFinishButton);

        taskTitleText.setText(taskTitle);
        timeText.setText(getInitialTimeText());
        modeText.setText(getFocusModeText());
        parameterText.setText(getString(R.string.focus_parameter_detail, focusMinutes, restMinutes));
        backgroundText.setText(TextUtils.isEmpty(backgroundRes)
                ? getString(R.string.focus_default_background)
                : getString(R.string.focus_background_detail, backgroundRes));

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
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void updateTimerOneSecond() {
        if (timerState == STATE_RESTING) {
            updateRestTimerOneSecond();
            return;
        }
        if (timerState != STATE_FOCUSING) {
            return;
        }

        elapsedSeconds++;
        if (Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)) {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                remainingSeconds = 0;
                timeText.setText(formatSeconds(remainingSeconds));
                finishFocus(true);
                return;
            }
            timeText.setText(formatSeconds(remainingSeconds));
        } else {
            timeText.setText(formatSeconds(elapsedSeconds));
        }

        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void togglePauseState() {
        if (timerState == STATE_RESTING) {
            return;
        }
        if (timerState == STATE_FINISHED) {
            return;
        }

        if (timerState == STATE_FOCUSING) {
            timerState = STATE_PAUSED;
            timerHandler.removeCallbacks(timerRunnable);
            statusText.setText(R.string.focus_status_paused);
            pauseButton.setText(R.string.focus_continue_button);
            return;
        }

        timerState = STATE_FOCUSING;
        statusText.setText(R.string.focus_status_running);
        pauseButton.setText(R.string.focus_pause_button);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void showFinishConfirmDialog() {
        if (timerState == STATE_RESTING) {
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

    private void finishFocus(boolean completedByCountdown) {
        timerState = STATE_FINISHED;
        timerHandler.removeCallbacks(timerRunnable);

        boolean saveSuccess = saveCurrentFocusRecord();

        int tipRes = !saveSuccess ? R.string.focus_record_save_fail_tip : completedByCountdown
                ? R.string.focus_countdown_finished_tip
                : R.string.focus_finished_tip;
        Toast.makeText(this, tipRes, Toast.LENGTH_SHORT).show();
        if (saveSuccess) {
            startRestCountdown();
        } else {
            finish();
        }
    }

    private void startRestCountdown() {
        timerState = STATE_RESTING;
        restRemainingSeconds = restMinutes * 60;
        pageTitleText.setText(R.string.rest_page_title);
        statusText.setText(R.string.rest_status_running);
        timeText.setText(formatSeconds(restRemainingSeconds));
        modeText.setText(R.string.rest_mode_title);
        parameterText.setText(getString(R.string.rest_parameter_detail, restMinutes));
        backgroundText.setText(R.string.rest_background_tip);
        pauseButton.setEnabled(false);
        pauseButton.setText(R.string.rest_button_placeholder);
        finishButton.setText(R.string.rest_finish_button);

        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void updateRestTimerOneSecond() {
        restRemainingSeconds--;
        if (restRemainingSeconds <= 0) {
            restRemainingSeconds = 0;
            timeText.setText(formatSeconds(restRemainingSeconds));
            finishRestCountdown();
            return;
        }

        timeText.setText(formatSeconds(restRemainingSeconds));
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void finishRestCountdown() {
        finishRestAndReturnHome(true);
    }

    private void finishRestAndReturnHome(boolean finishedByTimer) {
        timerState = STATE_FINISHED;
        timerHandler.removeCallbacks(timerRunnable);
        int tipRes = finishedByTimer ? R.string.rest_finished_tip : R.string.rest_stop_tip;
        Toast.makeText(this, tipRes, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean saveCurrentFocusRecord() {
        if (focusRecordSaved) {
            return true;
        }

        long endTime = System.currentTimeMillis();
        int durationMinutes = calculateDurationMinutes();

        // 结束专注时把当前任务、本次开始结束时间和专注分钟数保存到 focus_record，后续统计页和任务弹窗都从这里读取数据。
        boolean success = focusViewModel.saveFocusRecord(
                taskId,
                taskTitle,
                focusStartTime,
                endTime,
                durationMinutes,
                true
        );
        focusRecordSaved = success;
        return success;
    }

    private int calculateDurationMinutes() {
        if (elapsedSeconds <= 0) {
            return 0;
        }
        return Math.max(1, (elapsedSeconds + 59) / 60);
    }

    private String getInitialTimeText() {
        if (Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)) {
            return formatSeconds(remainingSeconds);
        }
        return formatSeconds(0);
    }

    private String getFocusModeText() {
        if (Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)) {
            return getString(R.string.focus_mode_countdown);
        }
        return getString(R.string.focus_mode_forward);
    }

    private String formatSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
