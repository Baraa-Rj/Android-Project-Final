package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class Employee {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("status")
    private String status; // AVAILABLE, BUSY, OFFLINE

    @SerializedName("rating")
    private Double rating;

    @SerializedName("total_jobs")
    private int totalJobs;

    // Constructors
    public Employee() {}

    public Employee(int id, int userId, String name, String email, String phone,
                    String status, Double rating, int totalJobs) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.rating = rating;
        this.totalJobs = totalJobs;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public int getTotalJobs() { return totalJobs; }
    public void setTotalJobs(int totalJobs) { this.totalJobs = totalJobs;
    }
}
