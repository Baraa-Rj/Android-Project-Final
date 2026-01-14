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
}
