package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("car_id")
    private int carId;

    @SerializedName("service_id")
    private int serviceId;

    @SerializedName("team_id")
    private Integer teamId;

    @SerializedName("location")
    private String location;

    @SerializedName("scheduled_time")
    private String scheduledTime;

    @SerializedName("status")
    private String status;

    @SerializedName("total_price")
    private double totalPrice;

    @SerializedName("notes")
    private String notes;

    // For display purposes (joined data)
    private String serviceName;
    private String carModel;

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getCarId() { return carId; }
    public int getServiceId() { return serviceId; }
    public Integer getTeamId() { return teamId; }
    public String getLocation() { return location; }
    public String getScheduledTime() { return scheduledTime; }
    public String getStatus() { return status; }
    public double getTotalPrice() { return totalPrice; }
    public String getNotes() { return notes; }
    public String getServiceName() { return serviceName; }
    public String getCarModel() { return carModel; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setCarId(int carId) { this.carId = carId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public void setLocation(String location) { this.location = location; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
}
