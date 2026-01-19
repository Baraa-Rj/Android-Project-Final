package com.example.myapplication.data.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents field-level validation errors from the API
 */
public class ValidationError {
    private final Map<String, String> fieldErrors = new HashMap<>();
    private String generalError;

    public ValidationError() {
    }

    public void addFieldError(String field, String message) {
        fieldErrors.put(field, message);
    }

    public void setGeneralError(String message) {
        this.generalError = message;
    }

    public String getFieldError(String field) {
        return fieldErrors.get(field);
    }

    public Map<String, String> getAllFieldErrors() {
        return new HashMap<>(fieldErrors);
    }

    public String getGeneralError() {
        return generalError;
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    public boolean hasGeneralError() {
        return generalError != null && !generalError.isEmpty();
    }

    public boolean hasErrors() {
        return hasFieldErrors() || hasGeneralError();
    }
}
