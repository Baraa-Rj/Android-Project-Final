package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class CarRequest {
    private String model;
    @SerializedName("plate_number")
    private String plateNumber;
    private String color;
    private Integer year;

    public CarRequest() {
    }

    public CarRequest(String model, String plateNumber, String color, Integer year) {
        this.model = model;
        this.plateNumber = plateNumber;
        this.color = color;
        this.year = year;
    }

    public String getModel() {
        return model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}