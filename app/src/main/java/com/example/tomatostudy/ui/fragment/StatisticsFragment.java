package com.example.tomatostudy.ui.fragment;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.FocusDailyTrendItem;
import com.example.tomatostudy.database.model.FocusDurationItem;
import com.example.tomatostudy.database.model.FocusTimePeriodItem;
import com.example.tomatostudy.ui.view.FocusDurationPieChartView;
import com.example.tomatostudy.ui.view.FocusMonthlyTrendLineChartView;
import com.example.tomatostudy.ui.view.FocusTimePeriodBarChartView;
import com.example.tomatostudy.util.TimeZoneUtils;
import com.example.tomatostudy.util.AppExecutors;
import com.example.tomatostudy.viewmodel.StatisticsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private final SimpleDateFormat dateFormat = createDateFormat();
    private final SimpleDateFormat monthFormat = createMonthFormat();
    private final Calendar selectedDate = Calendar.getInstance(TimeZoneUtils.CHINA_TIME_ZONE, Locale.CHINA);
    private final Calendar selectedMonth = Calendar.getInstance(TimeZoneUtils.CHINA_TIME_ZONE, Locale.CHINA);
    private final Calendar selectedTrendMonth = Calendar.getInstance(TimeZoneUtils.CHINA_TIME_ZONE, Locale.CHINA);
    private final int[] pieColors = new int[]{
            Color.parseColor("#3B6FAD"),
            Color.parseColor("#F2A65A"),
            Color.parseColor("#62B58F"),
            Color.parseColor("#D96C75"),
            Color.parseColor("#7E70C8"),
            Color.parseColor("#55A7B6")
    };

    private StatisticsViewModel statisticsViewModel;
    private TextView totalFocusCountText;
    private TextView totalFocusDurationText;
    private TextView averageDailyFocusText;
    private TextView dailyFocusDateText;
    private TextView dailyFocusCountText;
    private TextView dailyFocusDurationText;
    private FocusDurationPieChartView focusDurationPieChartView;
    private LinearLayout focusDurationLegendLayout;
    private LinearLayout focusDurationContentLayout;
    private TextView focusDurationEmptyText;
    private TextView focusDurationTotalText;
    private TextView timePeriodMonthText;
    private FocusTimePeriodBarChartView timePeriodBarChartView;
    private TextView timePeriodEmptyText;
    private TextView monthlyTrendMonthText;
    private FocusMonthlyTrendLineChartView monthlyTrendLineChartView;
    private TextView monthlyTrendEmptyText;
    private NestedScrollView statisticsScrollView;
    private boolean skipNextResumeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);
        statisticsScrollView = view.findViewById(R.id.statisticsScrollView);
        statisticsScrollView.setNestedScrollingEnabled(false);
        totalFocusCountText = view.findViewById(R.id.totalFocusCountText);
        totalFocusDurationText = view.findViewById(R.id.totalFocusDurationText);
        averageDailyFocusText = view.findViewById(R.id.averageDailyFocusText);
        dailyFocusDateText = view.findViewById(R.id.dailyFocusDateText);
        dailyFocusCountText = view.findViewById(R.id.dailyFocusCountText);
        dailyFocusDurationText = view.findViewById(R.id.dailyFocusDurationText);
        focusDurationPieChartView = view.findViewById(R.id.focusDurationPieChartView);
        focusDurationLegendLayout = view.findViewById(R.id.focusDurationLegendLayout);
        focusDurationContentLayout = view.findViewById(R.id.focusDurationContentLayout);
        focusDurationEmptyText = view.findViewById(R.id.focusDurationEmptyText);
        focusDurationTotalText = view.findViewById(R.id.focusDurationTotalText);
        timePeriodMonthText = view.findViewById(R.id.timePeriodMonthText);
        timePeriodBarChartView = view.findViewById(R.id.timePeriodBarChartView);
        timePeriodEmptyText = view.findViewById(R.id.timePeriodEmptyText);
        monthlyTrendMonthText = view.findViewById(R.id.monthlyTrendMonthText);
        monthlyTrendLineChartView = view.findViewById(R.id.monthlyTrendLineChartView);
        monthlyTrendEmptyText = view.findViewById(R.id.monthlyTrendEmptyText);
        TextView dailyPreviousText = view.findViewById(R.id.dailyPreviousText);
        TextView dailyNextText = view.findViewById(R.id.dailyNextText);
        TextView timePeriodPreviousText = view.findViewById(R.id.timePeriodPreviousText);
        TextView timePeriodNextText = view.findViewById(R.id.timePeriodNextText);
        TextView monthlyTrendPreviousText = view.findViewById(R.id.monthlyTrendPreviousText);
        TextView monthlyTrendNextText = view.findViewById(R.id.monthlyTrendNextText);

        dailyPreviousText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate.add(Calendar.DAY_OF_MONTH, -1);
                loadDailyFocus();
            }
        });
        dailyNextText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                loadDailyFocus();
            }
        });
        timePeriodPreviousText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMonth.add(Calendar.MONTH, -1);
                loadMonthlyTimePeriodDistribution();
            }
        });
        timePeriodNextText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMonth.add(Calendar.MONTH, 1);
                loadMonthlyTimePeriodDistribution();
            }
        });
        monthlyTrendPreviousText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTrendMonth.add(Calendar.MONTH, -1);
                loadMonthlyFocusTrend();
            }
        });
        monthlyTrendNextText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTrendMonth.add(Calendar.MONTH, 1);
                loadMonthlyFocusTrend();
            }
        });

        skipNextResumeRefresh = true;
        loadAllStatistics(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (statisticsViewModel != null) {
            if (skipNextResumeRefresh) {
                skipNextResumeRefresh = false;
                return;
            }
            loadAllStatistics(true);
        }
    }

    private void loadAllStatistics(boolean keepScrollPosition) {
        int scrollY = statisticsScrollView == null ? 0 : statisticsScrollView.getScrollY();
        loadTotalFocus();
        loadDailyFocus();
        loadMonthlyTimePeriodDistribution();
        loadMonthlyFocusTrend();
        if (keepScrollPosition && statisticsScrollView != null) {
            statisticsScrollView.post(new Runnable() {
                @Override
                public void run() {
                    statisticsScrollView.scrollTo(0, scrollY);
                }
            });
        }
    }

    private void loadTotalFocus() {
        statisticsViewModel.loadTotalFocusDataAsync(new AppExecutors.Callback<StatisticsViewModel.TotalFocusData>() {
            @Override
            public void onComplete(StatisticsViewModel.TotalFocusData data) {
                if (!isFragmentReady()) {
                    return;
                }
                totalFocusCountText.setText(String.valueOf(data.getTotalCount()));
                totalFocusDurationText.setText(formatHourMinute(data.getTotalMinutes()));
                averageDailyFocusText.setText(formatHourMinute(data.getAverageDailyMinutes()));
            }
        });
    }

    private void loadDailyFocus() {
        final String date = dateFormat.format(selectedDate.getTime());
        dailyFocusDateText.setText(date);
        statisticsViewModel.loadDailyFocusDataAsync(date, new AppExecutors.Callback<StatisticsViewModel.DailyFocusData>() {
            @Override
            public void onComplete(StatisticsViewModel.DailyFocusData data) {
                if (!isFragmentReady() || !date.equals(dateFormat.format(selectedDate.getTime()))) {
                    return;
                }
                dailyFocusCountText.setText(String.valueOf(data.getDailyCount()));
                dailyFocusDurationText.setText(getString(R.string.statistics_minute_format, data.getDailyMinutes()));
            }
        });
        loadFocusDurationDistribution(date);
    }
//加载专注时长分布数据
    private void loadFocusDurationDistribution(final String date) {
        statisticsViewModel.loadDailyTaskDurationDistributionAsync(date, new AppExecutors.Callback<List<FocusDurationItem>>() {
            @Override
            public void onComplete(List<FocusDurationItem> items) {
                if (!isFragmentReady() || !date.equals(dateFormat.format(selectedDate.getTime()))) {
                    return;
                }
                if (items.isEmpty()) {
                    focusDurationPieChartView.setSegments(new ArrayList<FocusDurationPieChartView.Segment>());
                    focusDurationLegendLayout.removeAllViews();
                    focusDurationContentLayout.setVisibility(View.GONE);
                    focusDurationEmptyText.setVisibility(View.VISIBLE);
                    return;
                }

                List<FocusDurationPieChartView.Segment> segments = new ArrayList<>();
                focusDurationLegendLayout.removeAllViews();
                int totalMinutes = 0;
                for (int i = 0; i < items.size(); i++) {
                    FocusDurationItem item = items.get(i);
                    int color = pieColors[i % pieColors.length];
                    totalMinutes += item.getDurationMinutes();
                    segments.add(new FocusDurationPieChartView.Segment(
                            item.getTaskTitle(),
                            item.getDurationMinutes(),
                            color
                    ));
                    addLegendRow(item, color, i);
                }

                focusDurationPieChartView.setSegments(segments);
                focusDurationTotalText.setText(getString(R.string.statistics_distribution_total_format, totalMinutes));
                focusDurationEmptyText.setVisibility(View.GONE);
                focusDurationContentLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadMonthlyTimePeriodDistribution() {
        final long monthStartTime = getMonthStartTime(selectedMonth);
        final long nextMonthStartTime = getNextMonthStartTime(selectedMonth);
        timePeriodMonthText.setText(monthFormat.format(selectedMonth.getTime()));
        statisticsViewModel.loadMonthlyTimePeriodDistributionAsync(
                monthStartTime,
                nextMonthStartTime,
                new AppExecutors.Callback<List<FocusTimePeriodItem>>() {
                    @Override
                    public void onComplete(List<FocusTimePeriodItem> items) {
                        if (!isFragmentReady() || monthStartTime != getMonthStartTime(selectedMonth)) {
                            return;
                        }
                        timePeriodBarChartView.setItems(items);
                        if (hasFocusMinutes(items)) {
                            timePeriodBarChartView.setVisibility(View.VISIBLE);
                            timePeriodEmptyText.setVisibility(View.GONE);
                            return;
                        }

                        timePeriodBarChartView.setVisibility(View.GONE);
                        timePeriodEmptyText.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void loadMonthlyFocusTrend() {
        final long monthStartTime = getMonthStartTime(selectedTrendMonth);
        final long nextMonthStartTime = getNextMonthStartTime(selectedTrendMonth);
        final int daysInMonth = selectedTrendMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        final int month = selectedTrendMonth.get(Calendar.MONTH) + 1;
        monthlyTrendMonthText.setText(monthFormat.format(selectedTrendMonth.getTime()));
        statisticsViewModel.loadMonthlyFocusTrendAsync(
                monthStartTime,
                nextMonthStartTime,
                daysInMonth,
                new AppExecutors.Callback<List<FocusDailyTrendItem>>() {
                    @Override
                    public void onComplete(List<FocusDailyTrendItem> items) {
                        if (!isFragmentReady() || monthStartTime != getMonthStartTime(selectedTrendMonth)) {
                            return;
                        }
                        monthlyTrendLineChartView.setItems(items, month);
                        if (hasFocusTrendMinutes(items)) {
                            monthlyTrendLineChartView.setVisibility(View.VISIBLE);
                            monthlyTrendEmptyText.setVisibility(View.GONE);
                            return;
                        }

                        monthlyTrendLineChartView.setVisibility(View.GONE);
                        monthlyTrendEmptyText.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private boolean isFragmentReady() {
        return isAdded() && getView() != null;
    }

    private void addLegendRow(FocusDurationItem item, int color, int index) {
        LinearLayout rowLayout = new LinearLayout(requireContext());
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (index > 0) {
            rowParams.topMargin = dpToPx(8);
        }

        View colorView = new View(requireContext());
        GradientDrawable colorDrawable = new GradientDrawable();
        colorDrawable.setColor(color);
        colorDrawable.setCornerRadius(dpToPx(3));
        colorView.setBackground(colorDrawable);
        LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(dpToPx(10), dpToPx(10));
        rowLayout.addView(colorView, colorParams);

        TextView titleText = new TextView(requireContext());
        titleText.setSingleLine(true);
        titleText.setEllipsize(TextUtils.TruncateAt.END);
        titleText.setText(item.getTaskTitle());
        titleText.setTextColor(Color.parseColor("#303030"));
        titleText.setTextSize(13);
        titleText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        titleParams.leftMargin = dpToPx(8);
        rowLayout.addView(titleText, titleParams);

        TextView minutesText = new TextView(requireContext());
        minutesText.setText(getString(R.string.statistics_minute_format, item.getDurationMinutes()));
        minutesText.setTextColor(Color.parseColor("#303030"));
        minutesText.setTextSize(12);
        minutesText.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams minutesParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        minutesParams.leftMargin = dpToPx(8);
        rowLayout.addView(minutesText, minutesParams);

        focusDurationLegendLayout.addView(rowLayout, rowParams);
    }

    private String formatHourMinute(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return getString(R.string.statistics_hour_minute_format, hours, minutes);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private long getMonthStartTime(Calendar monthCalendar) {
        Calendar calendar = (Calendar) monthCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getNextMonthStartTime(Calendar monthCalendar) {
        Calendar calendar = (Calendar) monthCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTimeInMillis();
    }

    private boolean hasFocusMinutes(List<FocusTimePeriodItem> items) {
        for (FocusTimePeriodItem item : items) {
            if (item.getDurationMinutes() > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFocusTrendMinutes(List<FocusDailyTrendItem> items) {
        for (FocusDailyTrendItem item : items) {
            if (item.getDurationMinutes() > 0) {
                return true;
            }
        }
        return false;
    }

    private SimpleDateFormat createDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        format.setTimeZone(TimeZoneUtils.CHINA_TIME_ZONE);
        return format;
    }

    private SimpleDateFormat createMonthFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年 MM月", Locale.CHINA);
        format.setTimeZone(TimeZoneUtils.CHINA_TIME_ZONE);
        return format;
    }
}
