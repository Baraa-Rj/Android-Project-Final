package com.example.myapplication.utils;

import android.util.Log;

import com.example.myapplication.data.models.ValidationError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Response;

/**
 * Utility class for parsing FastAPI validation errors (HTTP 422)
 *
 * FastAPI returns validation errors in the following format:
 * {
 *   "detail": [
 *     {
 *       "loc": ["body", "field_name"],
 *       "msg": "error message",
 *       "type": "value_error"
 *     }
 *   ]
 * }
 *
 * Or for simple errors:
 * {
 *   "detail": "Error message"
 * }
 */
public class ValidationErrorParser {
    private static final String TAG = "ValidationErrorParser";

    /**
     * Parse error response and extract field-level validation errors
     */
    public static ValidationError parseError(Response<?> response) {
        ValidationError validationError = new ValidationError();

        if (response == null || response.errorBody() == null) {
            validationError.setGeneralError("Unknown error occurred");
            return validationError;
        }

        try {
            String errorBody = response.errorBody().string();
            JSONObject errorJson = new JSONObject(errorBody);

            if (errorJson.has("detail")) {
                Object detail = errorJson.get("detail");

                // Handle array format (validation errors)
                if (detail instanceof JSONArray) {
                    JSONArray errorsArray = (JSONArray) detail;
                    for (int i = 0; i < errorsArray.length(); i++) {
                        JSONObject error = errorsArray.getJSONObject(i);

                        // Extract field name from "loc" array (e.g., ["body", "email"])
                        String fieldName = extractFieldName(error);

                        // Extract error message
                        String message = error.optString("msg", "Invalid value");

                        validationError.addFieldError(fieldName, message);
                        Log.d(TAG, "Field error: " + fieldName + " -> " + message);
                    }
                }
                // Handle string format (simple error)
                else if (detail instanceof String) {
                    validationError.setGeneralError(detail.toString());
                    Log.d(TAG, "General error: " + detail);
                }
            } else {
                validationError.setGeneralError("Server error");
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse error response", e);
            validationError.setGeneralError(getDefaultErrorMessage(response.code()));
        }

        return validationError;
    }

    /**
     * Extract field name from FastAPI "loc" array
     * Example: ["body", "email"] -> "email"
     * Example: ["body", "scheduled_time"] -> "scheduled_time"
     */
    private static String extractFieldName(JSONObject error) {
        try {
            JSONArray loc = error.getJSONArray("loc");
            // Field name is typically the last element in the loc array
            if (loc.length() > 0) {
                return loc.getString(loc.length() - 1);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to extract field name from loc", e);
        }
        return "unknown";
    }

    /**
     * Get default error message based on HTTP status code
     */
    private static String getDefaultErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Invalid request";
            case 401:
                return "Unauthorized. Please login again";
            case 422:
                return "Validation error";
            case 500:
                return "Server error. Please try again later";
            default:
                return "Unknown error occurred";
        }
    }

    /**
     * Map API field names to UI field names
     * This handles the snake_case to camelCase conversion
     */
    public static String mapApiFieldToUiField(String apiField) {
        switch (apiField) {
            case "scheduled_time":
                return "scheduledTime";
            case "plate_number":
                return "plateNumber";
            case "user_id":
                return "userId";
            case "service_id":
                return "serviceId";
            case "car_id":
                return "carId";
            default:
                return apiField;
        }
    }
}
