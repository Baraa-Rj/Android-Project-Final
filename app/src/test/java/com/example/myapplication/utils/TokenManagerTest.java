package com.example.myapplication.utils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TokenManagerTest {

    @Mock
    private Context context;

    @Mock
    private SharedPreferences sharedPreferences;

    @Mock
    private SharedPreferences.Editor editor;

    private TokenManager tokenManager;

    @Before
    public void setup() {
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);

        tokenManager = new TokenManager(context);
    }

    @Test
    public void testSaveToken_Success() {
        // Given
        String token = "test-token-123";
        int userId = 42;
        String email = "test@example.com";

        // When
        tokenManager.saveToken(token, userId, email);

        // Then
        verify(editor).putString("auth_token", token);
        verify(editor).putInt("user_id", userId);
        verify(editor).putString("user_email", email);
        verify(editor).apply();
    }

    @Test
    public void testGetToken_ReturnsStoredToken() {
        // Given
        String expectedToken = "stored-token";
        when(sharedPreferences.getString("auth_token", null)).thenReturn(expectedToken);

        // When
        String actualToken = tokenManager.getToken();

        // Then
        assertEquals(expectedToken, actualToken);
    }

    @Test
    public void testGetToken_ReturnsNullWhenNotStored() {
        // Given
        when(sharedPreferences.getString("auth_token", null)).thenReturn(null);

        // When
        String actualToken = tokenManager.getToken();

        // Then
        assertNull(actualToken);
    }

    @Test
    public void testGetUserId_ReturnsStoredUserId() {
        // Given
        int expectedUserId = 123;
        when(sharedPreferences.getInt("user_id", -1)).thenReturn(expectedUserId);

        // When
        int actualUserId = tokenManager.getUserId();

        // Then
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    public void testGetUserId_ReturnsNegativeOneWhenNotStored() {
        // Given
        when(sharedPreferences.getInt("user_id", -1)).thenReturn(-1);

        // When
        int actualUserId = tokenManager.getUserId();

        // Then
        assertEquals(-1, actualUserId);
    }

    @Test
    public void testGetUserEmail_ReturnsStoredEmail() {
        // Given
        String expectedEmail = "stored@example.com";
        when(sharedPreferences.getString("user_email", null)).thenReturn(expectedEmail);

        // When
        String actualEmail = tokenManager.getUserEmail();

        // Then
        assertEquals(expectedEmail, actualEmail);
    }

    @Test
    public void testGetUserEmail_ReturnsNullWhenNotStored() {
        // Given
        when(sharedPreferences.getString("user_email", null)).thenReturn(null);

        // When
        String actualEmail = tokenManager.getUserEmail();

        // Then
        assertNull(actualEmail);
    }

    @Test
    public void testIsLoggedIn_ReturnsTrueWhenTokenExists() {
        // Given
        when(sharedPreferences.getString("auth_token", null)).thenReturn("some-token");

        // When
        boolean isLoggedIn = tokenManager.isLoggedIn();

        // Then
        assertTrue(isLoggedIn);
    }

    @Test
    public void testIsLoggedIn_ReturnsFalseWhenTokenDoesNotExist() {
        // Given
        when(sharedPreferences.getString("auth_token", null)).thenReturn(null);

        // When
        boolean isLoggedIn = tokenManager.isLoggedIn();

        // Then
        assertFalse(isLoggedIn);
    }

    @Test
    public void testIsLoggedIn_ReturnsFalseWhenTokenIsEmpty() {
        // Given
        when(sharedPreferences.getString("auth_token", null)).thenReturn("");

        // When
        boolean isLoggedIn = tokenManager.isLoggedIn();

        // Then
        assertFalse(isLoggedIn);
    }

    @Test
    public void testClearToken_RemovesAllStoredData() {
        // When
        tokenManager.clearToken();

        // Then
        verify(editor).remove("auth_token");
        verify(editor).remove("user_id");
        verify(editor).remove("user_email");
        verify(editor).apply();
    }

    @Test
    public void testSaveToken_WithNullValues() {
        // When
        tokenManager.saveToken(null, 0, null);

        // Then
        verify(editor).putString("auth_token", null);
        verify(editor).putInt("user_id", 0);
        verify(editor).putString("user_email", null);
        verify(editor).apply();
    }
}
