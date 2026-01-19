package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String role;

    @SerializedName("access_token")
    private String token;

    @SerializedName("token_type")
    private String tokenType;

    public AuthResponse() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    // Setters for testing
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
