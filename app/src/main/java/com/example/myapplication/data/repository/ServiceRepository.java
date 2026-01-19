package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Service;
import java.util.List;
import retrofit2.Call;

public class ServiceRepository {
    private final ApiService apiService;

    public ServiceRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public Call<List<Service>> getServices() {
        return apiService.getServices();
    }
}
