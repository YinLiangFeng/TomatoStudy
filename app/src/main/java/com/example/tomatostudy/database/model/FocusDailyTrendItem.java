package com.example.tomatostudy.database.model;

public class FocusDailyTrendItem {

    private final int dayOfMonth;
    private final int durationMinutes;

    public FocusDailyTrendItem(int dayOfMonth, int durationMinutes) {
        this.dayOfMonth = dayOfMonth;
        this.durationMinutes = durationMinutes;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
