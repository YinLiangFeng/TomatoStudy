package com.example.tomatostudy.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.tomatostudy.database.model.FocusTimePeriodItem;

import java.util.ArrayList;
import java.util.List;

public class FocusTimePeriodBarChartView extends View {

    private final List<FocusTimePeriodItem> items = new ArrayList<>();
    private final List<FocusTimePeriodItem> displayItems = new ArrayList<>();
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    public FocusTimePeriodBarChartView(Context context) {
        super(context);
        init();
    }

    public FocusTimePeriodBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusTimePeriodBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setItems(List<FocusTimePeriodItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        buildDisplayItems();
        if (displayItems.isEmpty()) {
            return;
        }

        float left = getPaddingLeft() + dpToPx(42);
        float top = getPaddingTop() + dpToPx(22);
        float right = getWidth() - getPaddingRight() - dpToPx(10);
        float bottom = getHeight() - getPaddingBottom() - dpToPx(32);
        if (right <= left || bottom <= top) {
            return;
        }

        int maxMinutes = getMaxMinutes();
        if (maxMinutes <= 0) {
            return;
        }

        drawGrid(canvas, left, top, right, bottom, maxMinutes);
        drawBars(canvas, left, top, right, bottom, maxMinutes);
    }

    private void init() {
        axisPaint.setColor(Color.parseColor("#C8D6E6"));
        axisPaint.setStrokeWidth(dpToPx(1));

        gridPaint.setColor(Color.parseColor("#E8F0F8"));
        gridPaint.setStrokeWidth(dpToPx(1));

        barPaint.setColor(Color.parseColor("#4D86C6"));
        barPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#426F9E"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(spToPx(12));
        labelPaint.setTypeface(Typeface.DEFAULT_BOLD);

        valuePaint.setColor(Color.parseColor("#303030"));
        valuePaint.setTextSize(spToPx(11));
        valuePaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void drawGrid(Canvas canvas, float left, float top, float right, float bottom, int maxMinutes) {
        int halfMinutes = Math.max(1, maxMinutes / 2);
        drawHorizontalGuide(canvas, left, right, top, maxMinutes);
        drawHorizontalGuide(canvas, left, right, top + (bottom - top) / 2f, halfMinutes);
        drawHorizontalGuide(canvas, left, right, bottom, 0);
        canvas.drawLine(left, top, left, bottom, axisPaint);
        canvas.drawLine(left, bottom, right, bottom, axisPaint);
    }

    private void drawHorizontalGuide(Canvas canvas, float left, float right, float y, int minutes) {
        canvas.drawLine(left, y, right, y, gridPaint);
        valuePaint.setTextAlign(Paint.Align.RIGHT);
        Paint.FontMetrics metrics = valuePaint.getFontMetrics();
        canvas.drawText(minutes + "分钟", left - dpToPx(8), y - (metrics.ascent + metrics.descent) / 2f, valuePaint);
    }

    private void drawBars(Canvas canvas, float left, float top, float right, float bottom, int maxMinutes) {
        float chartWidth = right - left;
        float itemWidth = chartWidth / displayItems.size();
        float barWidth = displayItems.size() == 1
                ? Math.min(dpToPx(220), chartWidth * 0.68f)
                : Math.min(dpToPx(38), itemWidth * 0.52f);

        for (int i = 0; i < displayItems.size(); i++) {
            FocusTimePeriodItem item = displayItems.get(i);
            float centerX = left + itemWidth * i + itemWidth / 2f;
            int minutes = item.getDurationMinutes();
            if (minutes > 0) {
                float barHeight = (bottom - top) * minutes / maxMinutes;
                barHeight = Math.max(barHeight, dpToPx(6));

                float barTop = bottom - barHeight;
                barRect.set(centerX - barWidth / 2f, barTop, centerX + barWidth / 2f, bottom);
                canvas.drawRoundRect(barRect, dpToPx(7), dpToPx(7), barPaint);
                valuePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(minutes), centerX, barTop - dpToPx(6), valuePaint);
            }

            Paint.FontMetrics metrics = labelPaint.getFontMetrics();
            float labelBaseline = bottom + dpToPx(22) - (metrics.ascent + metrics.descent) / 2f;
            canvas.drawText(item.getPeriodName(), centerX, labelBaseline, labelPaint);
        }
    }

    private void buildDisplayItems() {
        displayItems.clear();
        List<FocusTimePeriodItem> activeItems = new ArrayList<>();
        for (FocusTimePeriodItem item : items) {
            if (item.getHour() >= 0 && item.getDurationMinutes() > 0) {
                activeItems.add(item);
            }
        }

        if (activeItems.isEmpty()) {
            return;
        }

        if (activeItems.size() == 1) {
            displayItems.add(activeItems.get(0));
            return;
        }

        int minHour = 23;
        int maxHour = 0;
        for (FocusTimePeriodItem item : activeItems) {
            minHour = Math.min(minHour, item.getHour());
            maxHour = Math.max(maxHour, item.getHour());
        }
        int step = maxHour - minHour <= 6 ? 1 : 2;
        boolean[] showHour = new boolean[24];

        for (int hour = minHour; hour <= maxHour; hour += step) {
            showHour[hour] = true;
        }
        showHour[maxHour] = true;
        for (FocusTimePeriodItem item : activeItems) {
            showHour[item.getHour()] = true;
        }

        for (int hour = minHour; hour <= maxHour; hour++) {
            if (showHour[hour]) {
                displayItems.add(new FocusTimePeriodItem(hour, getDurationAtHour(hour)));
            }
        }
    }

    private int getDurationAtHour(int hour) {
        for (FocusTimePeriodItem item : items) {
            if (item.getHour() == hour) {
                return item.getDurationMinutes();
            }
        }
        return 0;
    }

    private int getMaxMinutes() {
        int maxMinutes = 0;
        for (FocusTimePeriodItem item : items) {
            maxMinutes = Math.max(maxMinutes, item.getDurationMinutes());
        }
        return maxMinutes;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
