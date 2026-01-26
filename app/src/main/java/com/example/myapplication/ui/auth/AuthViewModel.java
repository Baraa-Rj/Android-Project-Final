package com.example.myapplication.ui.auth;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.models.AuthResponse;
import com.example.myapplication.data.models.LoginRequest;
import com.example.myapplication.data.models.RegisterRequest;
import com.example.myapplication.data.repository.AuthRepo;
import com.example.myapplication.utils.TokenManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AuthViewModel";

    private final AuthRepo authRepo;
    private final TokenManager tokenManager;
    private final MutableLiveData<AuthResponse> authResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    // Keep track of ongoing calls for cleanup
    private Call<AuthResponse> currentCall;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        Log.d(TAG, "AuthViewModel: Initializing ViewModel");
        this.authRepo = new AuthRepo(application);
        this.tokenManager = new TokenManager(application);
    }

    // Package-private constructor for testing
    AuthViewModel(@NonNull Application application, AuthRepo authRepo, TokenManager tokenManager) {
        super(application);
        this.authRepo = authRepo;
        this.tokenManager = tokenManager;
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

    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();

                // Try to parse as JSON, but handle plain text responses
                JSONObject errorJson = null;
                try {
                    errorJson = new JSONObject(errorBody);
                } catch (JSONException e) {
                    // Not JSON - check if it's plain text error message
                    if (errorBody != null && !errorBody.trim().isEmpty()) {
                        Log.d(TAG, "parseErrorMessage: Plain text error: " + errorBody);
                        // Return plain text for common server errors
                        if (errorBody.contains("Internal Server Error")) {
                            return "Server error. Please try again later.";
                        }
                        return errorBody.trim();
                    }
                }

                // Check if it has a "detail" field
                if (errorJson != null && errorJson.has("detail")) {
                    Object detail = errorJson.get("detail");

                    // Handle array format (validation errors from FastAPI)
                    if (detail instanceof JSONArray) {
                        JSONArray detailArray = (JSONArray) detail;
                        if (detailArray.length() > 0) {
                            JSONObject firstError = detailArray.getJSONObject(0);
                            String field = "";
                            String message = "";

                            // Get the field name from "loc" array
                            if (firstError.has("loc")) {
                                JSONArray loc = firstError.getJSONArray("loc");
                                if (loc.length() > 1) {
                                    field = loc.getString(loc.length() - 1);
                                }
                            }

                            // Get the error message
                            if (firstError.has("msg")) {
                                message = firstError.getString("msg");
                            }

                            // Return user-friendly messages based on field and error
                            if (field.equals("email")) {
                                if (message.contains("not a valid email")) {
                                    return "Please enter a valid email address.";
                                }
                                return "Invalid email: " + message;
                            } else if (field.equals("password")) {
                                return "Invalid password: " + message;
                            } else if (!message.isEmpty()) {
                                return message;
                            }
                        }
                    }
                    // Handle string format (custom error messages)
                    else if (detail instanceof String) {
                        String detailStr = (String) detail;

                        // Make error messages more user-friendly
                        if (detailStr.contains("Email already registered")) {
                            return "This email is already registered. Please login or use a different email.";
                        } else if (detailStr.contains("Incorrect email or password")) {
                            return "Incorrect email or password. Please try again.";
                        } else {
                            return detailStr;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parseErrorMessage: Failed to parse error", e);
        }

        // Fallback to HTTP status messages
        switch (response.code()) {
            case 400:
                return "Invalid request. Please check your input.";
            case 401:
                return "Incorrect email or password.";
            case 422:
                return "Please check your input and try again.";
            case 500:
                return "Server error. Please try again later.";
            default:
                return "Something went wrong. Please try again.";
        }
    }

    public void login(String email, String password) {
        Log.d(TAG, "login: Attempting login for email: " + email);
        loadingLiveData.setValue(true);
        LoginRequest request = new LoginRequest(email, password);
        currentCall = authRepo.loginUser(request);
        currentCall.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Log.d(TAG, "login: Login successful for user: " + authResponse.getEmail());
                    tokenManager.saveToken(
                            authResponse.getToken(),
                            authResponse.getId(),
                            authResponse.getEmail(),
                            authResponse.getRole(),
                            authResponse.getTeamId());
                    authResponseLiveData.setValue(authResponse);
                } else {
                    String errorMsg = parseErrorMessage(response);
                    Log.e(TAG, "login: Login failed: " + errorMsg);
                    errorLiveData.setValue(errorMsg);
                }
                currentCall = null;
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loadingLiveData.setValue(false);
                String errorMessage = "Connection failed. Please check your internet connection.";
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMessage = "Cannot connect to server. Please check your connection.";
                }
                Log.e(TAG, "login: Network failure: " + t.getMessage());
                errorLiveData.setValue(errorMessage);
                currentCall = null;
            }
        });
    }

    public void register(String name, String email, String phone, String password) {
        Log.d(TAG, "register: Attempting registration for email: " + email);
        loadingLiveData.setValue(true);
        RegisterRequest request = new RegisterRequest(name, email, phone, password);
        currentCall = authRepo.registerUser(request);
        currentCall.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Log.d(TAG, "register: Registration successful for user: " + authResponse.getEmail());
                    tokenManager.saveToken(
                            authResponse.getToken(),
                            authResponse.getId(),
                            authResponse.getEmail(),
                            authResponse.getRole(),
                            authResponse.getTeamId());
                    authResponseLiveData.setValue(authResponse);
                } else {
                    String errorMsg = parseErrorMessage(response);
                    Log.e(TAG, "register: Registration failed: " + errorMsg);
                    errorLiveData.setValue(errorMsg);
                }
                currentCall = null;
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loadingLiveData.setValue(false);
                String errorMessage = "Connection failed. Please check your internet connection.";
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMessage = "Cannot connect to server. Please check your connection.";
                }
                Log.e(TAG, "register: Network failure: " + t.getMessage());
                errorLiveData.setValue(errorMessage);
                currentCall = null;
            }
        });
    }

    public void logout() {
        Log.d(TAG, "logout: Clearing user token");
        tokenManager.clearToken();
    }

    public boolean isLoggedIn() {
        boolean loggedIn = tokenManager.isLoggedIn();
        Log.d(TAG, "isLoggedIn: " + loggedIn);
        return loggedIn;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared: ViewModel is being cleared");

        // Cancel any ongoing network calls to prevent memory leaks
        if (currentCall != null && !currentCall.isCanceled()) {
            Log.d(TAG, "onCleared: Canceling ongoing network call");
            currentCall.cancel();
            currentCall = null;
        }
    }

}