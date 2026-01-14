package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.AuthResponse;
import com.example.myapplication.data.models.LoginRequest;
import com.example.myapplication.data.models.RegisterRequest;
import com.example.myapplication.data.models.User;
import retrofit2.Call;

public class AuthRepo {
    private final ApiService apiService;

    public AuthRepo(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public Call<AuthResponse> loginUser(LoginRequest loginRequest) {
        return apiService.login(loginRequest);
    }

    public Call<AuthResponse> registerUser(RegisterRequest registerRequest) {
        return apiService.register(registerRequest);
    }

    public Call<User> getCurrentUser() {
        return apiService.getCurrentUser();
    }

}
