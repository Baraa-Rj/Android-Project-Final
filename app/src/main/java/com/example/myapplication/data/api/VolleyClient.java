package com.example.myapplication.data.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.utils.TokenManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class for Volley HTTP client.
 * Demonstrates an alternative to Retrofit for educational purposes.
 *
 * Key differences from Retrofit:
 * - Manual JSON parsing (no automatic GSON conversion)
 * - Request-based API (add requests to queue)
 * - Built-in image loading support
 * - Simpler setup, but more verbose code
 */
public class VolleyClient {
    // Use 10.0.3.2 for Genymotion Emulator, 10.0.2.2 for Android Studio Emulator
    public static final String BASE_URL = "http://10.0.3.2:8000";

    private static VolleyClient instance;
    private RequestQueue requestQueue;
    private final Context applicationContext;
    private final TokenManager tokenManager;

    private VolleyClient(Context ctx) {
        applicationContext = ctx.getApplicationContext();
        requestQueue = getRequestQueue();
        tokenManager = TokenManager.getInstance(applicationContext);
    }

    public static synchronized VolleyClient getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyClient(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(applicationContext);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    /**
     * Add request with tag for proper lifecycle management.
     * Tagged requests can be cancelled when Activity/Fragment is destroyed.
     */
    public <T> void addToRequestQueue(Request<T> request, String tag) {
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    /**
     * Get authorization headers for authenticated requests
     */
    public Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String token = tokenManager.getToken();
        if (token != null) {
            headers.put("Authorization", "Bearer " + token);
        }

        return headers;
    }

    /**
     * Cancel all pending requests with a given tag
     */
    public void cancelRequests(String tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
