package com.example.tomatostudy.ui.dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.ui.activity.TaskEditActivity;
import com.example.tomatostudy.viewmodel.TaskViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class TaskActionDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "TaskActionDialogFragment";
    public static final String REQUEST_TASK_CHANGED = "request_task_changed";

    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_FOCUS_MODE = "focus_mode";
    private static final String ARG_FOCUS_MINUTES = "focus_minutes";
    private static final String ARG_REST_MINUTES = "rest_minutes";
    private static final String ARG_BACKGROUND_RES = "background_res";

    private TaskViewModel taskViewModel;
    private int taskId;
    private String taskTitle;

    public static TaskActionDialogFragment newInstance(Task task) {
        TaskActionDialogFragment fragment = new TaskActionDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, task.getId());
        args.putString(ARG_TASK_TITLE, task.getTitle());
        args.putString(ARG_FOCUS_MODE, task.getFocusMode());
        args.putInt(ARG_FOCUS_MINUTES, task.getFocusMinutes());
        args.putInt(ARG_REST_MINUTES, task.getRestMinutes());
        args.putString(ARG_BACKGROUND_RES, task.getBackgroundRes());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_task_action, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return;
        }

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskId = args.getInt(ARG_TASK_ID);
        taskTitle = args.getString(ARG_TASK_TITLE, "");
        String focusMode = args.getString(ARG_FOCUS_MODE, Task.FOCUS_MODE_FORWARD);
        int focusMinutes = args.getInt(ARG_FOCUS_MINUTES, 25);
        int restMinutes = args.getInt(ARG_REST_MINUTES, 5);
        String backgroundRes = args.getString(ARG_BACKGROUND_RES, "");

        TextView titleText = view.findViewById(R.id.dialogTaskTitleText);
        TextView detailText = view.findViewById(R.id.dialogTaskDetailText);
        TextView backgroundText = view.findViewById(R.id.dialogTaskBackgroundText);
        TextView focusCountText = view.findViewById(R.id.dialogFocusCountText);
        TextView focusMinutesText = view.findViewById(R.id.dialogFocusMinutesText);

        titleText.setText(taskTitle);
        detailText.setText(getString(
                R.string.task_action_detail,
                getFocusModeText(focusMode),
                focusMinutes,
                restMinutes
        ));
        backgroundText.setText(TextUtils.isEmpty(backgroundRes)
                ? getString(R.string.task_action_default_background)
                : backgroundRes);
        focusCountText.setText(String.valueOf(taskViewModel.loadTaskFocusCount(taskId)));
        focusMinutesText.setText(String.valueOf(taskViewModel.loadTaskFocusMinutes(taskId)));

        bindPlaceholderAction(view, R.id.dialogTimerButton, R.string.task_action_timer);
        bindPlaceholderAction(view, R.id.dialogBackgroundButton, R.string.task_action_change_background);
        bindPlaceholderAction(view, R.id.dialogWhitelistButton, R.string.task_action_whitelist);
        bindPlaceholderAction(view, R.id.dialogMoveButton, R.string.task_action_move);
        bindPlaceholderAction(view, R.id.dialogHistoryButton, R.string.task_action_focus_history);
        bindPlaceholderAction(view, R.id.dialogStatisticsButton, R.string.task_action_statistics);


        bindPlaceholderAction(view, R.id.dialogEditButton, R.string.task_action_edit);
        bindPlaceholderAction(view, R.id.dialogDeleteButton, R.string.task_action_delete);

        MaterialButton editButton = view.findViewById(R.id.dialogEditButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditTask();
            }
        });

        MaterialButton deleteButton = view.findViewById(R.id.dialogDeleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmDialog();
            }
        });
    }

    private String getFocusModeText(String focusMode) {
        if (Task.FOCUS_MODE_COUNTDOWN.equals(focusMode)) {
            return getString(R.string.focus_mode_countdown);
        }
        return getString(R.string.focus_mode_forward);
    }

    private void bindPlaceholderAction(View parent, int buttonId, final int actionNameRes) {
        MaterialButton button = parent.findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.task_action_later_tip, getString(actionNameRes)),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void openEditTask() {
        Intent intent = new Intent(requireContext(), TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
        dismiss();
    }

    private void showDeleteConfirmDialog() {
        // 删除任务前先让用户确认，且这里只删除当前弹窗对应的一条任务。
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.task_delete_confirm_title)
                .setMessage(getString(R.string.task_delete_confirm_message, taskTitle))
                .setNegativeButton(R.string.task_delete_confirm_negative, null)
                .setPositiveButton(R.string.task_delete_confirm_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCurrentTask();
                    }
                })
                .show();
    }

    private void deleteCurrentTask() {
        if (taskViewModel.deleteTask(taskId)) {
            Toast.makeText(requireContext(), R.string.task_delete_success_tip, Toast.LENGTH_SHORT).show();
            Bundle result = new Bundle();
            result.putInt(ARG_TASK_ID, taskId);
            getParentFragmentManager().setFragmentResult(REQUEST_TASK_CHANGED, result);
            dismiss();
        } else {
            Toast.makeText(requireContext(), R.string.task_delete_fail_tip, Toast.LENGTH_SHORT).show();
        }
    }
}
