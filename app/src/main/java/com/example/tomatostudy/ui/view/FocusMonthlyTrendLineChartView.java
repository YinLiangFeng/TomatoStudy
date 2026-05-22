package com.example.tomatostudy.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.tomatostudy.database.model.FocusDailyTrendItem;

import java.util.ArrayList;
import java.util.List;

public class FocusMonthlyTrendLineChartView extends View {

    private final List<FocusDailyTrendItem> items = new ArrayList<>();
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private int monthNumber = 1;

    public FocusMonthlyTrendLineChartView(Context context) {
        super(context);
        init();
    }

    public FocusMonthlyTrendLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusMonthlyTrendLineChartView(Context context,
                                          @Nullable AttributeSet attrs,
                                          int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setItems(List<FocusDailyTrendItem> newItems, int newMonthNumber) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        monthNumber = Math.max(1, newMonthNumber);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) {
            return;
        }

        float left = getPaddingLeft() + dpToPx(42);
        float top = getPaddingTop() + dpToPx(24);
        float right = getWidth() - getPaddingRight() - dpToPx(10);
        float bottom = getHeight() - getPaddingBottom() - dpToPx(36);
        if (right <= left || bottom <= top) {
            return;
        }

        int maxMinutes = getMaxMinutes();
        if (maxMinutes <= 0) {
            return;
        }

        int axisMaxMinutes = getAxisMaxMinutes(maxMinutes);
        drawGrid(canvas, left, top, right, bottom, axisMaxMinutes);
        drawTrend(canvas, left, top, right, bottom, axisMaxMinutes);
        drawPoints(canvas, left, top, right, bottom, axisMaxMinutes, maxMinutes);
        drawDateLabels(canvas, left, right, bottom);
    }

    private void init() {
        axisPaint.setColor(Color.parseColor("#C8D6E6"));
        axisPaint.setStrokeWidth(dpToPx(1));

        gridPaint.setColor(Color.parseColor("#E8F0F8"));
        gridPaint.setStrokeWidth(dpToPx(1));

        linePaint.setColor(Color.parseColor("#4D86C6"));
        linePaint.setStrokeWidth(dpToPx(2.5f));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setColor(Color.parseColor("#D9EAF8"));
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAlpha(180);

        pointPaint.setColor(Color.parseColor("#4D86C6"));
        pointPaint.setStyle(Paint.Style.FILL);

        pointInnerPaint.setColor(Color.WHITE);
        pointInnerPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#426F9E"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(spToPx(12));
        labelPaint.setTypeface(Typeface.DEFAULT_BOLD);

        valuePaint.setColor(Color.parseColor("#303030"));
        valuePaint.setTextSize(spToPx(11));
        valuePaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void drawGrid(Canvas canvas,
                          float left,
                          float top,
                          float right,
                          float bottom,
                          int axisMaxMinutes) {
        int tickCount = axisMaxMinutes <= 5 ? axisMaxMinutes : 4;
        for (int i = 0; i <= tickCount; i++) {
            int minutes = Math.round(axisMaxMinutes * (tickCount - i) / (float) tickCount);
            float y = top + (bottom - top) * i / tickCount;
            canvas.drawLine(left, y, right, y, gridPaint);

            valuePaint.setTextAlign(Paint.Align.RIGHT);
            Paint.FontMetrics metrics = valuePaint.getFontMetrics();
            canvas.drawText(minutes + "\u5206\u949f",
                    left - dpToPx(8),
                    y - (metrics.ascent + metrics.descent) / 2f,
                    valuePaint);
        }

        canvas.drawLine(left, top, left, bottom, axisPaint);
        canvas.drawLine(left, bottom, right, bottom, axisPaint);
    }

    private void drawTrend(Canvas canvas,
                           float left,
                           float top,
                           float right,
                           float bottom,
                           int axisMaxMinutes) {
        linePath.reset();
        fillPath.reset();

        for (int i = 0; i < items.size(); i++) {
            FocusDailyTrendItem item = items.get(i);
            float x = getItemX(i, left, right);
            float y = getItemY(item.getDurationMinutes(), top, bottom, axisMaxMinutes);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, bottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        float lastX = getItemX(items.size() - 1, left, right);
        fillPath.lineTo(lastX, bottom);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }

    private void drawPoints(Canvas canvas,
                            float left,
                            float top,
                            float right,
                            float bottom,
                            int axisMaxMinutes,
                            int maxMinutes) {
        int activeCount = getActiveCount();
        float lastValueLabelRight = -Float.MAX_VALUE;
        for (int i = 0; i < items.size(); i++) {
            FocusDailyTrendItem item = items.get(i);
            int minutes = item.getDurationMinutes();
            float x = getItemX(i, left, right);
            float y = getItemY(minutes, top, bottom, axisMaxMinutes);

            if (minutes <= 0) {
                canvas.drawCircle(x, y, dpToPx(2.3f), pointPaint);
                continue;
            }

            canvas.drawCircle(x, y, dpToPx(4.2f), pointPaint);
            canvas.drawCircle(x, y, dpToPx(1.8f), pointInnerPaint);

            if (activeCount <= 12 || minutes == maxMinutes) {
                String text = minutes + "\u5206\u949f";
                float textWidth = valuePaint.measureText(text);
                float textLeft = x - textWidth / 2f;
                if (textLeft > lastValueLabelRight + dpToPx(6)) {
                    valuePaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(text, x, y - dpToPx(9), valuePaint);
                    lastValueLabelRight = x + textWidth / 2f;
                }
            }
        }
    }

    private void drawDateLabels(Canvas canvas, float left, float right, float bottom) {
        int step = getDateLabelStep(items.size());
        float lastLabelRight = -Float.MAX_VALUE;
        Paint.FontMetrics metrics = labelPaint.getFontMetrics();
        float baseline = bottom + dpToPx(23) - (metrics.ascent + metrics.descent) / 2f;

        for (int i = 0; i < items.size(); i++) {
            FocusDailyTrendItem item = items.get(i);
            boolean shouldDraw = i == 0
                    || i == items.size() - 1
                    || (item.getDayOfMonth() % step == 0 && i < items.size() - 2);
            if (!shouldDraw) {
                continue;
            }

            float x = getItemX(i, left, right);
            String label = monthNumber + "-" + item.getDayOfMonth();
            float labelWidth = labelPaint.measureText(label);
            float labelLeft = x - labelWidth / 2f;
            if (labelLeft <= lastLabelRight + dpToPx(6) && i != items.size() - 1) {
                continue;
            }

            canvas.drawText(label, x, baseline, labelPaint);
            lastLabelRight = x + labelWidth / 2f;
        }
    }

    private int getMaxMinutes() {
        int maxMinutes = 0;
        for (FocusDailyTrendItem item : items) {
            maxMinutes = Math.max(maxMinutes, item.getDurationMinutes());
        }
        return maxMinutes;
    }

    private int getActiveCount() {
        int count = 0;
        for (FocusDailyTrendItem item : items) {
            if (item.getDurationMinutes() > 0) {
                count++;
            }
        }
        return count;
    }

    private int getAxisMaxMinutes(int maxMinutes) {
        if (maxMinutes <= 3) {
            return 3;
        }
        if (maxMinutes <= 10) {
            return 10;
        }
        if (maxMinutes <= 30) {
            return ((maxMinutes + 4) / 5) * 5;
        }
        return ((maxMinutes + 9) / 10) * 10;
    }

    private int getDateLabelStep(int itemCount) {
        if (itemCount <= 7) {
            return 1;
        }
        if (itemCount <= 15) {
            return 2;
        }
        return 5;
    }

    private float getItemX(int index, float left, float right) {
        if (items.size() <= 1) {
            return left + (right - left) / 2f;
        }
        return left + (right - left) * index / (items.size() - 1);
    }

    private float getItemY(int minutes, float top, float bottom, int axisMaxMinutes) {
        return bottom - (bottom - top) * minutes / axisMaxMinutes;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
