package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class BookingRequest {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("car_id")
    private int carId;

    @SerializedName("service_id")
    private int serviceId;

    @SerializedName("team_id")
    private Integer teamId;

    @SerializedName("vehicle_id")
    private Integer vehicleId;

    @SerializedName("location")
    private String location;

    @SerializedName("location_lat")
    private Double locationLat;

    @SerializedName("location_lng")
    private Double locationLng;

    @SerializedName("scheduled_time")
    private String scheduledTime;

    @SerializedName("total_price")
    private double totalPrice;

    @SerializedName("notes")
    private String notes;

    public BookingRequest() {
    }

    public BookingRequest(int userId, int carId, int serviceId, String location,
                          String scheduledTime, double totalPrice, String notes) {
        this.userId = userId;
        this.carId = carId;
        this.serviceId = serviceId;
        this.location = location;
        this.scheduledTime = scheduledTime;
        this.totalPrice = totalPrice;
        this.notes = notes;
    }

    // Getters
    public int getUserId() { return userId; }
    public int getCarId() { return carId; }
    public int getServiceId() { return serviceId; }
    public Integer getTeamId() { return teamId; }
    public Integer getVehicleId() { return vehicleId; }
    public String getLocation() { return location; }
    public Double getLocationLat() { return locationLat; }
    public Double getLocationLng() { return locationLng; }
    public String getScheduledTime() { return scheduledTime; }
    public double getTotalPrice() { return totalPrice; }
    public String getNotes() { return notes; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setCarId(int carId) { this.carId = carId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    public void setLocation(String location) { this.location = location; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setNotes(String notes) { this.notes = notes; }
}
