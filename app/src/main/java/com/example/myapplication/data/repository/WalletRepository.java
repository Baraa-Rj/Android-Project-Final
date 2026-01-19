package com.example.myapplication.data.repository;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.data.api.VolleyClient;
import com.example.myapplication.data.models.Transaction;
import com.example.myapplication.data.models.WalletBalance;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WalletRepository handles wallet-related operations using Volley.
 * Provides a clean interface for wallet operations with callback-based async handling.
 */
public class WalletRepository {
    private static final String TAG = "WalletRepository";
    private final VolleyClient volleyClient;

    public WalletRepository(Context context) {
        this.volleyClient = VolleyClient.getInstance(context);
    }

    /**
     * Callback interface for wallet balance operations
     */
    public interface BalanceCallback {
        void onSuccess(WalletBalance balance);
        void onError(String errorMessage);
    }

    /**
     * Callback interface for transaction list operations
     */
    public interface TransactionsCallback {
        void onSuccess(List<Transaction> transactions);
        void onError(String errorMessage);
    }

    /**
     * Callback interface for deposit operations
     */
    public interface DepositCallback {
        void onSuccess(double newBalance, String message);
        void onError(String errorMessage);
    }

    /**
     * Fetch wallet balance
     */
    public void getWalletBalance(String requestTag, BalanceCallback callback) {
        String url = VolleyClient.BASE_URL + "/api/wallet/balance";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        WalletBalance balance = WalletBalance.fromJson(response);
                        callback.onSuccess(balance);
                        Log.d(TAG, "Balance loaded: $" + balance.getBalance());
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing balance: " + e.getMessage());
                        callback.onError("Error parsing balance data");
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    Log.e(TAG, "Error loading balance: " + errorMsg);
                    callback.onError("Failed to load balance");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return volleyClient.getAuthHeaders();
            }
        };

        volleyClient.addToRequestQueue(request, requestTag);
    }

    /**
     * Fetch wallet transactions
     */
    public void getTransactions(String requestTag, TransactionsCallback callback) {
        String url = VolleyClient.BASE_URL + "/api/wallet/transactions";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<Transaction> transactions = parseTransactions(response);
                        callback.onSuccess(transactions);
                        Log.d(TAG, "Loaded " + transactions.size() + " transactions");
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing transactions: " + e.getMessage());
                        callback.onError("Error parsing transaction data");
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    Log.e(TAG, "Error loading transactions: " + errorMsg);
                    callback.onError("Failed to load transactions");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return volleyClient.getAuthHeaders();
            }
        };

        volleyClient.addToRequestQueue(request, requestTag);
    }

    /**
     * Deposit funds into wallet
     */
    public void depositFunds(double amount, String description, String requestTag, DepositCallback callback) {
        String url = VolleyClient.BASE_URL + "/api/wallet/deposit";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("amount", amount);
            requestBody.put("description", description);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating deposit request: " + e.getMessage());
            callback.onError("Failed to create deposit request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            double newBalance = response.getDouble("new_balance");
                            String message = response.getString("message");
                            callback.onSuccess(newBalance, message);
                            Log.d(TAG, "Deposit successful: " + message);
                        } else {
                            callback.onError("Deposit failed");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing deposit response: " + e.getMessage());
                        callback.onError("Error processing deposit");
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    Log.e(TAG, "Error depositing funds: " + errorMsg);
                    callback.onError("Failed to add funds");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return volleyClient.getAuthHeaders();
            }
        };

        volleyClient.addToRequestQueue(request, requestTag);
    }

    /**
     * Parse JSON array into list of Transaction objects
     */
    private List<Transaction> parseTransactions(JSONArray jsonArray) throws JSONException {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            transactions.add(Transaction.fromJson(jsonObject));
        }
        return transactions;
    }

    /**
     * Cancel pending requests for a specific tag
     */
    public void cancelRequests(String tag) {
        volleyClient.cancelRequests(tag);
    }
}
