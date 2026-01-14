package com.example.myapplication.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.models.AuthResponse;
import com.example.myapplication.data.models.LoginRequest;
import com.example.myapplication.data.models.RegisterRequest;
import com.example.myapplication.data.repository.AuthRepo;
import com.example.myapplication.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepo authRepo;
    private final TokenManager tokenManager;
    private final MutableLiveData<AuthResponse> authResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepo = new AuthRepo(application);
        this.tokenManager = new TokenManager(application);
    }

    public LiveData<AuthResponse> getAuthResponseLiveData() {
        return authResponseLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        LoginRequest request = new LoginRequest(email, password);
        authRepo.loginUser(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    tokenManager.saveToken(
                        authResponse.getToken(),
                        authResponse.getUser().getId(),
                        authResponse.getUser().getEmail()
                    );
                    authResponseLiveData.setValue(authResponse);
                } else {
                    errorLiveData.setValue("Login failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Login failed: " + t.getMessage());
            }
        });
    }

    public void register(String name, String email, String phone, String password) {
        loadingLiveData.setValue(true);
        RegisterRequest request = new RegisterRequest(name, email, phone, password);
        authRepo.registerUser(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    tokenManager.saveToken(
                        authResponse.getToken(),
                        authResponse.getUser().getId(),
                        authResponse.getUser().getEmail()
                    );
                    authResponseLiveData.setValue(authResponse);
                } else {
                    errorLiveData.setValue("Registration failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Registration failed: " + t.getMessage());
            }
        });
    }

    public void logout() {
        tokenManager.clearToken();
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

}