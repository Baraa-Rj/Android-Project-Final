package com.example.myapplication.data.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * WalletBalance model for wallet balance response.
 * Uses manual JSON parsing (for Volley) instead of GSON annotations.
 */
public class WalletBalance {
    private int userId;
    private double balance;
    private double totalCredits;
    private double totalDebits;

    public WalletBalance() {
    }

    /**
     * Parse WalletBalance from JSONObject (for Volley responses)
     */
    public static WalletBalance fromJson(JSONObject json) throws JSONException {
        WalletBalance walletBalance = new WalletBalance();
        walletBalance.userId = json.getInt("user_id");
        walletBalance.balance = json.getDouble("balance");
        walletBalance.totalCredits = json.getDouble("total_credits");
        walletBalance.totalDebits = json.getDouble("total_debits");
        return walletBalance;
    }

    // Getters
    public int getUserId() { return userId; }
    public double getBalance() { return balance; }
    public double getTotalCredits() { return totalCredits; }
    public double getTotalDebits() { return totalDebits; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setTotalCredits(double totalCredits) { this.totalCredits = totalCredits; }
    public void setTotalDebits(double totalDebits) { this.totalDebits = totalDebits; }
}
