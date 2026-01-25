package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class EmployeeStats {
    @SerializedName("total_jobs")
    private int totalJobs;

    @SerializedName("completed_today")
    private int completedToday;

    @SerializedName("in_progress")
    private int inProgress;

    @SerializedName("earnings_today")
    private double earningsToday;

    @SerializedName("total_earnings")
    private double totalEarnings;

    @SerializedName("average_rating")
    private Double averageRating;

    // Getters
    public int getTotalJobs() { return totalJobs; }
    public int getCompletedToday() { return completedToday; }
    public int getInProgress() { return inProgress; }
    public double getEarningsToday() { return earningsToday; }
    public double getTotalEarnings() { return totalEarnings; }
    public Double getAverageRating() { return averageRating; }
}
