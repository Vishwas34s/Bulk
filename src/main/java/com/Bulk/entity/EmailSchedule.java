package com.Bulk.entity;

import java.time.LocalTime;
import java.util.List;

public class EmailSchedule {
    private LocalTime scheduledTime;
    private List<String> days;

    public EmailSchedule(LocalTime scheduledTime, List<String> days) {
        this.scheduledTime = scheduledTime;
        this.days = days;
    }

    // Getters and setters
    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }
}
