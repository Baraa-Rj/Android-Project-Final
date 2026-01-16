package com.example.myapplication.data.api;

import android.content.Context;
import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import com.example.myapplication.utils.TokenManager;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(@NonNull Context context) {
        this.tokenManager = TokenManager.getInstance(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            return chain.proceed(originalRequest);
        }
        Request modifiedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(modifiedRequest);
    }
}
