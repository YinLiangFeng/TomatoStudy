package com.example.tomatostudy.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);

        void onStartClick(Task task);
    }

    private final int[][] cardColors = new int[][]{
            {Color.rgb(11, 34, 72), Color.rgb(4, 17, 39)},
            {Color.rgb(42, 43, 40), Color.rgb(20, 20, 18)},
            {Color.rgb(127, 223, 225), Color.rgb(112, 207, 208)},
            {Color.rgb(145, 183, 186), Color.rgb(126, 166, 169)},
            {Color.rgb(212, 198, 232), Color.rgb(187, 188, 226)},
            {Color.rgb(195, 151, 183), Color.rgb(164, 134, 179)},
            {Color.rgb(16, 16, 18), Color.rgb(0, 0, 0)},
            {Color.rgb(126, 168, 195), Color.rgb(85, 124, 154)}
    };

    private final Context context;
    private final LayoutInflater inflater;
    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskClickListener listener;

    public TaskAdapter(Context context, OnTaskClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void submitTasks(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position), position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText;
        private final TextView modeText;
        private final TextView startText;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.taskCardTitleText);
            modeText = itemView.findViewById(R.id.taskCardModeText);
            startText = itemView.findViewById(R.id.taskCardStartText);
        }

        void bind(final Task task, int position) {
            titleText.setText(task.getTitle());
            modeText.setText(getFocusModeText(task));
            itemView.setBackground(createCardBackground(position));

            // 点击任务卡片主体只进入任务操作入口，不能直接开始计时。
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onTaskClick(task);
                    }
                }
            });

            // 点击右侧“开始”按钮才进入专注计时流程，和卡片主体点击分开处理。
            startText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onStartClick(task);
                    }
                }
            });
        }
    }

    private String getFocusModeText(Task task) {
        if (Task.FOCUS_MODE_COUNTDOWN.equals(task.getFocusMode())) {
            return context.getString(R.string.focus_mode_countdown);
        }
        return context.getString(R.string.focus_mode_forward);
    }

    private GradientDrawable createCardBackground(int position) {
        int[] colors = cardColors[position % cardColors.length];
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                colors
        );
        drawable.setCornerRadius(dp(14));
        return drawable;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()
        );
    }
}
