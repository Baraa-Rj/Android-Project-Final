package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class RescheduleRequest {
    @SerializedName("scheduled_time")
    private String scheduledTime;

    public RescheduleRequest(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
