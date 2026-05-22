package com.example.tomatostudy.database.model;

public class FocusTimePeriodItem {

    private final String periodName;
    private final int hour;
    private final int durationMinutes;

    public FocusTimePeriodItem(String periodName, int durationMinutes) {
        this.periodName = periodName;
        this.hour = -1;
        this.durationMinutes = durationMinutes;
    }

    public FocusTimePeriodItem(int hour, int durationMinutes) {
        this.periodName = hour + "点";
        this.hour = hour;
        this.durationMinutes = durationMinutes;
    }

    public String getPeriodName() {
        return periodName;
    }

    public int getHour() {
        return hour;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
