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

import java.util.ArrayList;
import java.util.List;

public class FocusDurationPieChartView extends View {

    private final List<Segment> segments = new ArrayList<>();
    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint taskTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint minuteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF pieBounds = new RectF();
    private int totalMinutes;

    public FocusDurationPieChartView(Context context) {
        super(context);
        init();
    }

    public FocusDurationPieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusDurationPieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setSegments(List<Segment> newSegments) {
        segments.clear();
        totalMinutes = 0;

        if (newSegments != null) {
            for (Segment segment : newSegments) {
                if (segment.getMinutes() > 0) {
                    segments.add(segment);
                    totalMinutes += segment.getMinutes();
                }
            }
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float contentLeft = getPaddingLeft();
        float contentTop = getPaddingTop();
        float contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (contentWidth <= 0 || contentHeight <= 0) {
            return;
        }

        float calloutSpace = dpToPx(72);
        float radius = Math.min((contentWidth - calloutSpace * 2f) / 2f, contentHeight / 2f);
        if (radius <= 0) {
            return;
        }

        float centerX = contentLeft + contentWidth / 2f;
        float centerY = contentTop + contentHeight / 2f;
        pieBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        if (segments.isEmpty() || totalMinutes <= 0) {
            canvas.drawCircle(centerX, centerY, radius - dpToPx(6), emptyPaint);
            return;
        }

        float startAngle = -90f;
        for (Segment segment : segments) {
            float sweepAngle = 360f * segment.getMinutes() / totalMinutes;
            slicePaint.setColor(segment.getColor());
            canvas.drawArc(pieBounds, startAngle, sweepAngle, true, slicePaint);
            startAngle += sweepAngle;
        }

        startAngle = -90f;
        for (Segment segment : segments) {
            float sweepAngle = 360f * segment.getMinutes() / totalMinutes;
            drawSegmentText(canvas, centerX, centerY, radius, startAngle, sweepAngle, segment);
            startAngle += sweepAngle;
        }
    }

    private void init() {
        emptyPaint.setColor(Color.parseColor("#D6E7F7"));
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setStrokeWidth(dpToPx(12));

        taskTextPaint.setColor(Color.parseColor("#303030"));
        taskTextPaint.setTextAlign(Paint.Align.CENTER);
        taskTextPaint.setTextSize(spToPx(13));
        taskTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        minuteTextPaint.setColor(Color.parseColor("#303030"));
        minuteTextPaint.setTextSize(spToPx(12));
        minuteTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        linePaint.setColor(Color.parseColor("#303030"));
        linePaint.setStrokeWidth(dpToPx(1));
        linePaint.setStyle(Paint.Style.STROKE);
    }

    private void drawSegmentText(Canvas canvas,
                                 float centerX,
                                 float centerY,
                                 float radius,
                                 float startAngle,
                                 float sweepAngle,
                                 Segment segment) {
        float middleAngle = startAngle + sweepAngle / 2f;
        double radians = Math.toRadians(middleAngle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        drawTaskLabel(canvas, centerX, centerY, radius, cos, sin, segment.getLabel());
        drawMinuteCallout(canvas, centerX, centerY, radius, cos, sin, segment.getMinutes());
    }

    private void drawTaskLabel(Canvas canvas,
                               float centerX,
                               float centerY,
                               float radius,
                               float cos,
                               float sin,
                               String label) {
        String text = fitText(label, taskTextPaint, radius * 0.72f);
        Paint.FontMetrics metrics = taskTextPaint.getFontMetrics();
        float textX = centerX + cos * radius * 0.44f;
        float textY = centerY + sin * radius * 0.44f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(text, textX, textY, taskTextPaint);
    }

    private void drawMinuteCallout(Canvas canvas,
                                   float centerX,
                                   float centerY,
                                   float radius,
                                   float cos,
                                   float sin,
                                   int minutes) {
        float side = cos >= 0 ? 1f : -1f;
        float startX = centerX + cos * radius * 0.74f;
        float startY = centerY + sin * radius * 0.74f;
        float elbowX = centerX + cos * (radius + dpToPx(6));
        float elbowY = centerY + sin * (radius + dpToPx(6));
        float endX = elbowX + side * dpToPx(14);
        float endY = elbowY;

        canvas.drawLine(startX, startY, elbowX, elbowY, linePaint);
        canvas.drawLine(elbowX, elbowY, endX, endY, linePaint);

        String minuteText = minutes + "分钟";
        minuteTextPaint.setTextAlign(side > 0 ? Paint.Align.LEFT : Paint.Align.RIGHT);
        Paint.FontMetrics metrics = minuteTextPaint.getFontMetrics();
        float textX = endX + side * dpToPx(4);
        float textY = endY - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(minuteText, textX, textY, minuteTextPaint);
    }

    private String fitText(String text, Paint paint, float maxWidth) {
        if (text == null) {
            return "";
        }

        if (paint.measureText(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        String result = text;
        while (result.length() > 0 && paint.measureText(result + ellipsis) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + ellipsis;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    public static class Segment {

        private final String label;
        private final int minutes;
        private final int color;

        public Segment(String label, int minutes, int color) {
            this.label = label;
            this.minutes = minutes;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public int getMinutes() {
            return minutes;
        }

        public int getColor() {
            return color;
        }
    }
}
