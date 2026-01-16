package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class Service {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("duration")
    private int duration;

    @SerializedName("is_active")
    private boolean isActive;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getDuration() { return duration; }
    public boolean isActive() { return isActive; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setActive(boolean active) { isActive = active; }
}