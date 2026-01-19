package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.customer.adapter.TransactionAdapter;
import com.example.myapplication.ui.customer.viewmodel.WalletViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

/**
 * WalletActivity demonstrates using Volley for HTTP requests via WalletViewModel.
 * The ViewModel abstracts the Volley implementation details and provides
 * a clean LiveData interface for the UI layer.
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
    private WalletViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupObservers();

        // Load data
        viewModel.loadWalletBalance();
        viewModel.loadTransactions();
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

        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);
    }

    private void setupObservers() {
        viewModel.getWalletBalanceLiveData().observe(this, balance -> {
            if (balance != null) {
                balanceText.setText(String.format(Locale.US, "$%.2f", balance.getBalance()));
                totalCreditsText.setText(String.format(Locale.US, "$%.2f", balance.getTotalCredits()));
                totalDebitsText.setText(String.format(Locale.US, "$%.2f", balance.getTotalDebits()));
            }
        });

        viewModel.getTransactionsLiveData().observe(this, transactions -> {
            if (transactions != null) {
                if (transactions.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    transactionAdapter.setTransactions(transactions);
                }
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null) {
                showLoading(isLoading);
            }
        });

        viewModel.getDepositSuccessLiveData().observe(this, success -> {
            if (success != null && success) {
                if (currentDialog != null) {
                    currentDialog.dismiss();
                    currentDialog = null;
                }
            }
        });
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

    private void showAddFundsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_funds, null);

        TextInputEditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        MaterialButton amount10 = dialogView.findViewById(R.id.amount10);
        MaterialButton amount25 = dialogView.findViewById(R.id.amount25);
        MaterialButton amount50 = dialogView.findViewById(R.id.amount50);
        MaterialButton amount100 = dialogView.findViewById(R.id.amount100);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton addButton = dialogView.findViewById(R.id.addButton);

        currentDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Quick amount buttons
        amount10.setOnClickListener(v -> amountEditText.setText("10"));
        amount25.setOnClickListener(v -> amountEditText.setText("25"));
        amount50.setOnClickListener(v -> amountEditText.setText("50"));
        amount100.setOnClickListener(v -> amountEditText.setText("100"));

        cancelButton.setOnClickListener(v -> {
            if (currentDialog != null) {
                currentDialog.dismiss();
                currentDialog = null;
            }
        });

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

                viewModel.depositFunds(amount, "Added funds to wallet");
            } catch (NumberFormatException e) {
                amountEditText.setError("Invalid amount");
            }
        });

        currentDialog.show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        transactionsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
