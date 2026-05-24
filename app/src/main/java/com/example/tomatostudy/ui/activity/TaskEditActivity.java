package com.example.tomatostudy.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.util.AppExecutors;
import com.example.tomatostudy.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class TaskEditActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final int INVALID_TASK_ID = -1;

    private TaskViewModel taskViewModel;
    private Task editingTask;
    private boolean editMode;

    private TextView titleText;
    private TextInputEditText taskTitleEditText;
    private TextInputEditText taskDescriptionEditText;
    private TextInputEditText taskFocusMinutesEditText;
    private TextInputEditText taskRestMinutesEditText;
    private TextInputEditText taskPriorityEditText;
    private TextInputEditText taskCollectionEditText;
    private TextInputEditText taskReminderEditText;
    private TextInputEditText taskBackgroundEditText;
    private RadioGroup focusModeGroup;
    private RadioButton forwardModeRadio;
    private RadioButton countdownModeRadio;
    private MaterialButton saveButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        bindViews();
        setupHeader();
        loadEditTaskIfNeeded();
        setupSaveButton();
    }

    private void bindViews() {
        titleText = findViewById(R.id.taskEditTitleText);
        taskTitleEditText = findViewById(R.id.taskTitleEditText);
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText);
        taskFocusMinutesEditText = findViewById(R.id.taskFocusMinutesEditText);
        taskRestMinutesEditText = findViewById(R.id.taskRestMinutesEditText);
        taskPriorityEditText = findViewById(R.id.taskPriorityEditText);
        taskCollectionEditText = findViewById(R.id.taskCollectionEditText);
        taskReminderEditText = findViewById(R.id.taskReminderEditText);
        taskBackgroundEditText = findViewById(R.id.taskBackgroundEditText);
        focusModeGroup = findViewById(R.id.taskFocusModeGroup);
        forwardModeRadio = findViewById(R.id.forwardModeRadio);
        countdownModeRadio = findViewById(R.id.countdownModeRadio);
        saveButton = findViewById(R.id.taskSaveButton);

        taskFocusMinutesEditText.setText(String.valueOf(25));
        taskRestMinutesEditText.setText(String.valueOf(5));
        taskPriorityEditText.setText(String.valueOf(0));
        taskCollectionEditText.setText("default");
    }

    private void setupHeader() {
        TextView backText = findViewById(R.id.taskEditBackText);
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadEditTaskIfNeeded() {
        int taskId = getIntent().getIntExtra(EXTRA_TASK_ID, INVALID_TASK_ID);
        editMode = taskId != INVALID_TASK_ID;

        if (!editMode) {
            titleText.setText(R.string.task_edit_add_title);
            saveButton.setText(R.string.task_save_button);
            return;
        }

        titleText.setText(R.string.task_edit_edit_title);
        saveButton.setText(R.string.task_update_button);
        saveButton.setEnabled(false);
        taskViewModel.getTaskByIdAsync(taskId, new AppExecutors.Callback<Task>() {
            @Override
            public void onComplete(Task task) {
                if (isDestroyed()) {
                    return;
                }
                editingTask = task;
                if (editingTask == null) {
                    Toast.makeText(TaskEditActivity.this, R.string.task_load_fail_tip, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                fillTaskToForm(editingTask);
                saveButton.setEnabled(true);
            }
        });
    }

    private void fillTaskToForm(Task task) {
        taskTitleEditText.setText(task.getTitle());
        taskDescriptionEditText.setText(task.getDescription());
        taskFocusMinutesEditText.setText(String.valueOf(task.getFocusMinutes()));
        taskRestMinutesEditText.setText(String.valueOf(task.getRestMinutes()));
        taskPriorityEditText.setText(String.valueOf(task.getPriority()));
        taskCollectionEditText.setText(task.getCollectionName());
        taskReminderEditText.setText(task.getReminderTime());
        taskBackgroundEditText.setText(task.getBackgroundRes());

        if (Task.FOCUS_MODE_COUNTDOWN.equals(task.getFocusMode())) {
            countdownModeRadio.setChecked(true);
        } else {
            forwardModeRadio.setChecked(true);
        }
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
    }

    private void saveTask() {
        String title = getText(taskTitleEditText);
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, R.string.task_title_empty_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        int focusMinutes = parsePositiveInt(taskFocusMinutesEditText, 25);
        int restMinutes = parsePositiveInt(taskRestMinutesEditText, 5);
        int priority = parseNonNegativeInt(taskPriorityEditText, 0);
        if (focusMinutes <= 0 || restMinutes <= 0 || priority < 0) {
            Toast.makeText(this, R.string.task_number_invalid_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = editingTask == null ? new Task() : editingTask;
        // 新增和编辑共用同一张表单：有旧任务时保留 id、创建时间等字段，只覆盖用户本次修改的内容。
        task.setTitle(title);
        task.setDescription(getText(taskDescriptionEditText));
        task.setFocusMode(getSelectedFocusMode());
        task.setFocusMinutes(focusMinutes);
        task.setRestMinutes(restMinutes);
        task.setPriority(priority);
        task.setCollectionName(getTextOrDefault(taskCollectionEditText, "default"));
        task.setReminderTime(getText(taskReminderEditText));
        task.setBackgroundRes(getText(taskBackgroundEditText));

        saveButton.setEnabled(false);
        taskViewModel.saveTaskAsync(task, new AppExecutors.Callback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                if (isDestroyed()) {
                    return;
                }
                if (Boolean.TRUE.equals(success)) {
                    int messageRes = editMode ? R.string.task_update_success_tip : R.string.task_save_success_tip;
                    Toast.makeText(TaskEditActivity.this, messageRes, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    saveButton.setEnabled(true);
                    Toast.makeText(TaskEditActivity.this, R.string.task_save_fail_tip, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getSelectedFocusMode() {
        if (focusModeGroup.getCheckedRadioButtonId() == R.id.countdownModeRadio) {
            return Task.FOCUS_MODE_COUNTDOWN;
        }
        return Task.FOCUS_MODE_FORWARD;
    }

    private int parsePositiveInt(TextInputEditText editText, int defaultValue) {
        String value = getText(editText);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parseNonNegativeInt(TextInputEditText editText, int defaultValue) {
        String value = getText(editText);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String getTextOrDefault(TextInputEditText editText, String defaultValue) {
        String value = getText(editText);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
