package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Transaction model for wallet transactions.
 * Supports both Retrofit (GSON annotations) and Volley (manual parsing).
 */
public class Transaction {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("booking_id")
    private Integer bookingId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("type")
    private String type; // "credit" or "debit"

    @SerializedName("description")
    private String description;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("service_name")
    private String serviceName; // From joined booking data

    public Transaction() {
    }

    /**
     * Parse Transaction from JSONObject (for Volley responses)
     */
    public static Transaction fromJson(JSONObject json) throws JSONException {
        Transaction transaction = new Transaction();
        transaction.id = json.getInt("id");
        transaction.userId = json.getInt("user_id");
        transaction.bookingId = json.isNull("booking_id") ? null : json.getInt("booking_id");
        transaction.amount = json.getDouble("amount");
        transaction.type = json.getString("type");
        transaction.description = json.optString("description", "");
        transaction.createdAt = json.optString("created_at", "");
        transaction.serviceName = json.optString("service_name", null);
        return transaction;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public Integer getBookingId() { return bookingId; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public String getServiceName() { return serviceName; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    /**
     * Check if this is a credit (money added)
     */
    public boolean isCredit() {
        return "credit".equalsIgnoreCase(type);
    }

    /**
     * Check if this is a debit (money spent)
     */
    public boolean isDebit() {
        return "debit".equalsIgnoreCase(type);
    }
}
