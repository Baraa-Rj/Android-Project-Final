package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.R;
import com.example.myapplication.data.api.VolleyClient;
import com.example.myapplication.data.models.Transaction;
import com.example.myapplication.data.models.WalletBalance;
import com.example.myapplication.ui.customer.adapter.TransactionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * WalletActivity demonstrates using Volley for HTTP requests
 * instead of Retrofit (used elsewhere in the app).
 *
 * Key Volley concepts demonstrated:
 * - JsonObjectRequest for single object responses
 * - JsonArrayRequest for array responses
 * - Request queue management
 * - Manual JSON parsing
 * - Error handling with VolleyError
 */
public class WalletActivity extends AppCompatActivity {
    private static final String TAG = "WalletActivity";

    // Views
    private Toolbar toolbar;
    private TextView balanceText;
    private TextView totalCreditsText;
    private TextView totalDebitsText;
    private MaterialButton addFundsButton;
    private RecyclerView transactionsRecyclerView;
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    // Data
    private VolleyClient volleyClient;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        // Load data
        loadWalletBalance();
        loadTransactions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        balanceText = findViewById(R.id.balanceText);
        totalCreditsText = findViewById(R.id.totalCreditsText);
        totalDebitsText = findViewById(R.id.totalDebitsText);
        addFundsButton = findViewById(R.id.addFundsButton);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);

        volleyClient = VolleyClient.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter();
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(transactionAdapter);
    }

    private void setupClickListeners() {
        addFundsButton.setOnClickListener(v -> showAddFundsDialog());
    }

    /**
     * Load wallet balance using Volley JsonObjectRequest
     */
    private void loadWalletBalance() {
        String url = VolleyClient.BASE_URL + "/api/wallet/balance";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        WalletBalance balance = WalletBalance.fromJson(response);
                        updateBalanceUI(balance);
                        Log.d(TAG, "Balance loaded: $" + balance.getBalance());
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing balance: " + e.getMessage());
                        Toast.makeText(this, "Error loading balance", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading balance: " + error.getMessage());
                    Toast.makeText(this, "Failed to load balance", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return volleyClient.getAuthHeaders();
            }
        };

        volleyClient.addToRequestQueue(request, TAG);
    }

    /**
     * Load transactions using Volley JsonArrayRequest
     */
    private void loadTransactions() {
        showLoading(true);
        String url = VolleyClient.BASE_URL + "/api/wallet/transactions";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    showLoading(false);
                    try {
                        List<Transaction> transactions = parseTransactions(response);
                        if (transactions.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                            transactionAdapter.setTransactions(transactions);
                        }
                        Log.d(TAG, "Loaded " + transactions.size() + " transactions");
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing transactions: " + e.getMessage());
                        Toast.makeText(this, "Error loading transactions", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading transactions: " + error.getMessage());
                    Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return volleyClient.getAuthHeaders();
            }
        };

        volleyClient.addToRequestQueue(request, TAG);
    }

    /**
     * Parse JSON array into list of Transaction objects
     * This is done manually with Volley (unlike Retrofit which uses GSON automatically)
     */
    private List<Transaction> parseTransactions(JSONArray jsonArray) throws JSONException {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            transactions.add(Transaction.fromJson(jsonObject));
        }
        return transactions;
    }

    private void updateBalanceUI(WalletBalance balance) {
        balanceText.setText(String.format(Locale.US, "$%.2f", balance.getBalance()));
        totalCreditsText.setText(String.format(Locale.US, "$%.2f", balance.getTotalCredits()));
        totalDebitsText.setText(String.format(Locale.US, "$%.2f", balance.getTotalDebits()));
    }

    private void showAddFundsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_funds, null);

        TextInputEditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        MaterialButton amount10 = dialogView.findViewById(R.id.amount10);
        MaterialButton amount25 = dialogView.findViewById(R.id.amount25);
        MaterialButton amount50 = dialogView.findViewById(R.id.amount50);
        MaterialButton amount100 = dialogView.findViewById(R.id.amount100);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton addButton = dialogView.findViewById(R.id.addButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Quick amount buttons
        amount10.setOnClickListener(v -> amountEditText.setText("10"));
        amount25.setOnClickListener(v -> amountEditText.setText("25"));
        amount50.setOnClickListener(v -> amountEditText.setText("50"));
        amount100.setOnClickListener(v -> amountEditText.setText("100"));

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        addButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText() != null ?
                    amountEditText.getText().toString().trim() : "";

            if (amountStr.isEmpty()) {
                amountEditText.setError("Enter an amount");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    amountEditText.setError("Amount must be positive");
                    return;
                }
                if (amount > 1000) {
                    amountEditText.setError("Maximum is $1000");
                    return;
                }

                depositFunds(amount, dialog);
            } catch (NumberFormatException e) {
                amountEditText.setError("Invalid amount");
            }
        });

        dialog.show();
    }

    /**
     * Deposit funds using Volley POST request with JSON body
     */
    private void depositFunds(double amount, AlertDialog dialog) {
        String url = VolleyClient.BASE_URL + "/api/wallet/deposit";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("amount", amount);
            requestBody.put("description", "Added funds to wallet");
        } catch (JSONException e) {
            e.printStackTrace();
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

                            // Update UI
                            balanceText.setText(String.format(Locale.US, "$%.2f", newBalance));
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                            // Reload transactions to show the new one
                            loadTransactions();
                            loadWalletBalance();

                            dialog.dismiss();
                            Log.d(TAG, "Deposit successful: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing deposit response: " + e.getMessage());
                        Toast.makeText(this, "Error processing deposit", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error depositing funds: " + error.getMessage());
                    Toast.makeText(this, "Failed to add funds", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return volleyClient.getAuthHeaders();
            }
        };

        volleyClient.addToRequestQueue(request, TAG);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        transactionsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending requests
        volleyClient.cancelRequests(TAG);
    }
}
