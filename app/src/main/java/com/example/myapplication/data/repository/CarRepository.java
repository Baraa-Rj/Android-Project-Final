package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Car;
import com.example.myapplication.data.models.CarRequest;
import java.util.List;
import retrofit2.Call;

public class CarRepository {
    private final ApiService apiService;

    public CarRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public Call<List<Car>> getCars() {
        return apiService.getCars();
    }

    public Call<Car> addCar(CarRequest carRequest) {
        return apiService.addCar(carRequest);
    }

    public Call<Car> updateCar(int carId, CarRequest carRequest) {
        return apiService.updateCar(carId, carRequest);
    }

    public Call<Void> deleteCar(int carId) {
        return apiService.deleteCar(carId);
    }
}
