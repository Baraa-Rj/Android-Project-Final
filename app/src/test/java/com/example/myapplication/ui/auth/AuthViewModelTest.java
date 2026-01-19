package com.example.myapplication.ui.auth;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.myapplication.data.models.AuthResponse;
import com.example.myapplication.data.models.LoginRequest;
import com.example.myapplication.data.models.RegisterRequest;
import com.example.myapplication.data.repository.AuthRepo;
import com.example.myapplication.utils.TokenManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class AuthViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private AuthRepo authRepo;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private Call<AuthResponse> call;

    @Mock
    private Observer<AuthResponse> authResponseObserver;

    @Mock
    private Observer<String> errorObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    private AuthViewModel viewModel;

    @Before
    public void setup() {
        // Create ViewModel with mocked dependencies using package-private constructor
        viewModel = new AuthViewModel(application, authRepo, tokenManager);

        // Observe LiveData
        viewModel.getAuthResponseLiveData().observeForever(authResponseObserver);
        viewModel.getErrorLiveData().observeForever(errorObserver);
        viewModel.getLoadingLiveData().observeForever(loadingObserver);
    }

    @Test
    public void testLoginSuccess() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        AuthResponse expectedResponse = new AuthResponse();
        expectedResponse.setToken("test-token");
        expectedResponse.setId(1);
        expectedResponse.setEmail(email);

        when(authRepo.loginUser(any(LoginRequest.class))).thenReturn(call);

        // When
        viewModel.login(email, password);

        // Capture the callback
        ArgumentCaptor<Callback<AuthResponse>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());

        // Simulate successful response
        callbackCaptor.getValue().onResponse(call, Response.success(expectedResponse));

        // Then
        verify(loadingObserver, times(3)).onChanged(any(Boolean.class)); // false (initial), true, false
        verify(authResponseObserver).onChanged(expectedResponse);
        verify(tokenManager).saveToken("test-token", 1, email);
        verify(errorObserver, never()).onChanged(any());
    }

    @Test
    public void testLoginFailure_InvalidCredentials() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        String errorJson = "{\"detail\":\"Incorrect email or password\"}";
        ResponseBody errorBody = ResponseBody.create(
                MediaType.parse("application/json"),
                errorJson
        );

        when(authRepo.loginUser(any(LoginRequest.class))).thenReturn(call);

        // When
        viewModel.login(email, password);

        // Capture the callback
        ArgumentCaptor<Callback<AuthResponse>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());

        // Simulate error response
        callbackCaptor.getValue().onResponse(call, Response.error(401, errorBody));

        // Then
        verify(loadingObserver, times(3)).onChanged(any(Boolean.class)); // false (initial), true, false
        verify(errorObserver).onChanged("Incorrect email or password.");
        verify(authResponseObserver, never()).onChanged(any());
    }

    @Test
    public void testLoginFailure_NetworkError() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        IOException networkException = new IOException("Unable to resolve host");

        when(authRepo.loginUser(any(LoginRequest.class))).thenReturn(call);

        // When
        viewModel.login(email, password);

        // Capture the callback
        ArgumentCaptor<Callback<AuthResponse>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());

        // Simulate network failure
        callbackCaptor.getValue().onFailure(call, networkException);

        // Then
        verify(loadingObserver, times(3)).onChanged(any(Boolean.class)); // false (initial), true, false
        verify(errorObserver).onChanged("Cannot connect to server. Please check your connection.");
        verify(authResponseObserver, never()).onChanged(any());
    }

    @Test
    public void testRegisterSuccess() {
        // Given
        String name = "Test User";
        String email = "test@example.com";
        String phone = "1234567890";
        String password = "password123";
        AuthResponse expectedResponse = new AuthResponse();
        expectedResponse.setToken("test-token");
        expectedResponse.setId(1);
        expectedResponse.setEmail(email);

        when(authRepo.registerUser(any(RegisterRequest.class))).thenReturn(call);

        // When
        viewModel.register(name, email, phone, password);

        // Capture the callback
        ArgumentCaptor<Callback<AuthResponse>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());

        // Simulate successful response
        callbackCaptor.getValue().onResponse(call, Response.success(expectedResponse));

        // Then
        verify(loadingObserver, times(3)).onChanged(any(Boolean.class)); // false (initial), true, false
        verify(authResponseObserver).onChanged(expectedResponse);
        verify(tokenManager).saveToken("test-token", 1, email);
        verify(errorObserver, never()).onChanged(any());
    }

    @Test
    public void testRegisterFailure_EmailAlreadyExists() {
        // Given
        String name = "Test User";
        String email = "existing@example.com";
        String phone = "1234567890";
        String password = "password123";
        String errorJson = "{\"detail\":\"Email already registered\"}";
        ResponseBody errorBody = ResponseBody.create(
                MediaType.parse("application/json"),
                errorJson
        );

        when(authRepo.registerUser(any(RegisterRequest.class))).thenReturn(call);

        // When
        viewModel.register(name, email, phone, password);

        // Capture the callback
        ArgumentCaptor<Callback<AuthResponse>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());

        // Simulate error response
        callbackCaptor.getValue().onResponse(call, Response.error(400, errorBody));

        // Then
        verify(loadingObserver, times(3)).onChanged(any(Boolean.class)); // false (initial), true, false
        verify(errorObserver).onChanged("Invalid request. Please check your input.");
        verify(authResponseObserver, never()).onChanged(any());
    }

    @Test
    public void testLogout() {
        // When
        viewModel.logout();

        // Then
        verify(tokenManager).clearToken();
    }

    @Test
    public void testIsLoggedIn_True() {
        // Given
        when(tokenManager.isLoggedIn()).thenReturn(true);

        // When
        boolean result = viewModel.isLoggedIn();

        // Then
        assertTrue(result);
        verify(tokenManager).isLoggedIn();
    }

    @Test
    public void testIsLoggedIn_False() {
        // Given
        when(tokenManager.isLoggedIn()).thenReturn(false);

        // When
        boolean result = viewModel.isLoggedIn();

        // Then
        assertFalse(result);
        verify(tokenManager).isLoggedIn();
    }

    @Test
    public void testLoadingState_StartAndStop() {
        // Given
        when(authRepo.loginUser(any(LoginRequest.class))).thenReturn(call);
        AuthResponse response = new AuthResponse();
        response.setToken("token");
        response.setId(1);
        response.setEmail("test@example.com");

        // When
        viewModel.login("test@example.com", "password");

        // Capture callback
        ArgumentCaptor<Callback<AuthResponse>> callbackCaptor =
                ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(callbackCaptor.capture());
        callbackCaptor.getValue().onResponse(call, Response.success(response));

        // Then
        ArgumentCaptor<Boolean> loadingCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(loadingObserver, times(3)).onChanged(loadingCaptor.capture());

        // First call should be false (initial state)
        assertFalse(loadingCaptor.getAllValues().get(0));
        // Second call should be true (loading started)
        assertTrue(loadingCaptor.getAllValues().get(1));
        // Third call should be false (loading stopped)
        assertFalse(loadingCaptor.getAllValues().get(2));
    }
}
