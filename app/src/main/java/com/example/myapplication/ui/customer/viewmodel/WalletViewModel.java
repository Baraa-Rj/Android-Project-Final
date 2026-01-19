package com.example.myapplication.ui.customer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.models.Transaction;
import com.example.myapplication.data.models.WalletBalance;
import com.example.myapplication.data.repository.WalletRepository;

import java.util.List;

/**
 * WalletViewModel handles wallet-related UI logic using WalletRepository (Volley).
 * Demonstrates callback-based async pattern with LiveData for UI updates.
 */
public class WalletViewModel extends AndroidViewModel {
    private static final String TAG = "WalletViewModel";

    private final WalletRepository walletRepository;
    private final MutableLiveData<WalletBalance> walletBalanceLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> depositSuccessLiveData = new MutableLiveData<>();

    public WalletViewModel(@NonNull Application application) {
        super(application);
        this.walletRepository = new WalletRepository(application);
        Log.d(TAG, "WalletViewModel initialized");
    }

    public LiveData<WalletBalance> getWalletBalanceLiveData() {
        return walletBalanceLiveData;
    }

    public LiveData<List<Transaction>> getTransactionsLiveData() {
        return transactionsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<Boolean> getDepositSuccessLiveData() {
        return depositSuccessLiveData;
    }

    public void loadWalletBalance() {
        Log.d(TAG, "Loading wallet balance...");
        loadingLiveData.setValue(true);

        walletRepository.getWalletBalance(TAG, new WalletRepository.BalanceCallback() {
            @Override
            public void onSuccess(WalletBalance balance) {
                loadingLiveData.setValue(false);
                walletBalanceLiveData.setValue(balance);
                Log.d(TAG, "Wallet balance loaded: $" + balance.getBalance());
            }

            @Override
            public void onError(String errorMessage) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(errorMessage);
                Log.e(TAG, "Error loading balance: " + errorMessage);
            }
        });
    }

    public void loadTransactions() {
        Log.d(TAG, "Loading transactions...");
        loadingLiveData.setValue(true);

        walletRepository.getTransactions(TAG, new WalletRepository.TransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                loadingLiveData.setValue(false);
                transactionsLiveData.setValue(transactions);
                Log.d(TAG, "Loaded " + transactions.size() + " transactions");
            }

            @Override
            public void onError(String errorMessage) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(errorMessage);
                Log.e(TAG, "Error loading transactions: " + errorMessage);
            }
        });
    }

    public void depositFunds(double amount, String description) {
        Log.d(TAG, "Depositing funds: $" + amount);
        loadingLiveData.setValue(true);

        walletRepository.depositFunds(amount, description, TAG, new WalletRepository.DepositCallback() {
            @Override
            public void onSuccess(double newBalance, String message) {
                loadingLiveData.setValue(false);
                depositSuccessLiveData.setValue(true);
                Log.d(TAG, "Deposit successful. New balance: $" + newBalance);

                // Reload balance and transactions to reflect changes
                loadWalletBalance();
                loadTransactions();
            }

            @Override
            public void onError(String errorMessage) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(errorMessage);
                Log.e(TAG, "Error depositing funds: " + errorMessage);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ViewModel is being cleared");

        // Cancel any pending Volley requests
        walletRepository.cancelRequests(TAG);
    }
}
