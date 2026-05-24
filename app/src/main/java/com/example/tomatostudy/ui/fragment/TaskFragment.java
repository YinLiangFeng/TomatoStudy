package com.example.tomatostudy.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.Task;
import com.example.tomatostudy.ui.activity.FocusActivity;
import com.example.tomatostudy.ui.activity.TaskEditActivity;
import com.example.tomatostudy.ui.adapter.TaskAdapter;
import com.example.tomatostudy.ui.dialog.TaskActionDialogFragment;
import com.example.tomatostudy.viewmodel.TaskViewModel;

import java.util.List;
//进入待办页
//    ↓
//TaskFragment 加载 fragment_task.xml
//    ↓
//找到 RecyclerView、空状态文字
//    ↓
//创建 TaskAdapter
//    ↓
//通过 TaskViewModel 加载当前用户任务
//    ↓
//把任务交给 TaskAdapter 显示
//    ↓
//根据有没有任务，切换“列表 / 空状态”
public class TaskFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private RecyclerView taskRecyclerView;
    private TextView taskEmptyText;
    private TaskAdapter taskAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        taskEmptyText = view.findViewById(R.id.taskEmptyText);

        taskAdapter = new TaskAdapter(requireContext(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                // 点击任务卡片主体只打开任务操作入口
                showTaskActionEntry(task);
            }

            @Override
            public void onStartClick(Task task) {
                // 点击右侧“开始”按钮才进入计时流程
                startFocus(task);
            }
        });
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);
        registerTaskChangedListener();

        TextView addText = view.findViewById(R.id.headerAddText);
        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), TaskEditActivity.class);
                startActivity(intent);
            }
        });
        loadTaskCards();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (taskViewModel != null) {
            loadTaskCards();
        }
    }

    private void loadTaskCards() {
        List<Task> tasks = taskViewModel.loadTasks();
        taskAdapter.submitTasks(tasks);

        if (tasks.isEmpty()) {
            taskRecyclerView.setVisibility(View.GONE);
            taskEmptyText.setVisibility(View.VISIBLE);
            return;
        }

        taskRecyclerView.setVisibility(View.VISIBLE);
        taskEmptyText.setVisibility(View.GONE);
    }

    private void registerTaskChangedListener() {
        getParentFragmentManager().setFragmentResultListener(
                TaskActionDialogFragment.REQUEST_TASK_CHANGED,
                getViewLifecycleOwner(),
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        loadTaskCards();
                    }
                }
        );
    }

    private void showTaskActionEntry(Task task) {
        TaskActionDialogFragment
                .newInstance(task)
                .show(getParentFragmentManager(), TaskActionDialogFragment.TAG);
    }

    private void startFocus(Task task) {
        Intent intent = new Intent(requireContext(), FocusActivity.class);
        intent.putExtra(FocusActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(FocusActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(FocusActivity.EXTRA_FOCUS_MODE, task.getFocusMode());
        intent.putExtra(FocusActivity.EXTRA_FOCUS_MINUTES, task.getFocusMinutes());
        intent.putExtra(FocusActivity.EXTRA_REST_MINUTES, task.getRestMinutes());
        intent.putExtra(FocusActivity.EXTRA_BACKGROUND_RES, task.getBackgroundRes());
        startActivity(intent);
    }
}
