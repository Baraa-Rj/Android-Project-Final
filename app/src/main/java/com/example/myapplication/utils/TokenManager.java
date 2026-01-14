package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";

    public TokenManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token, int userId, String userEmail) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.putInt(USER_ID_KEY, userId);
        editor.putString(USER_EMAIL_KEY, userEmail);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public int getUserId() {
        return sharedPreferences.getInt(USER_ID_KEY, -1);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(USER_EMAIL_KEY, null);
    }

    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_KEY);
        editor.remove(USER_ID_KEY);
        editor.remove(USER_EMAIL_KEY);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(USER_ID_KEY, user.getId());
        editor.putString(USER_EMAIL_KEY, user.getEmail());
        editor.apply();
    }
}