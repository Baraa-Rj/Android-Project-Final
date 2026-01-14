package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class Car {
    private int id;
    @SerializedName("user_id")
    private int userId;
    private String model;
    @SerializedName("plate_number")
    private String plateNumber;
    private String color;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    private Integer year;

    public Car() {
    }

    public Car(int id, int userId, String model, String plateNumber, String color, String createdAt,
            String updatedAt, Integer year) {
        this.id = id;
        this.userId = userId;
        this.model = model;
        this.plateNumber = plateNumber;
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getModel() {
        return model;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getColor() {
        return color;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Integer getYear() {
        return year;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", userId=" + userId +
                ", model='" + model + '\'' +
                ", plateNumber='" + plateNumber + '\'' +
                ", color='" + color + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", year=" + year +
                '}';
    }
}
