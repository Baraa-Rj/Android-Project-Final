package com.example.myapplication.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.R;
import com.example.myapplication.data.api.VolleyClient;
import com.example.myapplication.utils.TokenManager;

import org.json.JSONException;

import java.util.Locale;
import java.util.Map;

public class CustomerHomeFragment extends Fragment {
    private static final String TAG = "CustomerHomeFragment";

    private TextView welcomeText;
    private TextView userNameText;
    private CardView walletCard;
    private TextView walletBalanceText;
    private CardView bookNowCard;
    private CardView myCarsCard;
    private CardView upcomingBookingCard;
    private TextView upcomingServiceName;
    private TextView upcomingBookingDate;
    private TextView upcomingBookingStatus;
    private TokenManager tokenManager;
    private VolleyClient volleyClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = TokenManager.getInstance(getContext());
        volleyClient = VolleyClient.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupUserInfo();
        setupClickListeners();
    }

    private void initViews(View view) {
    welcomeText = view.findViewById(R.id.welcomeText);
    userNameText = view.findViewById(R.id.userNameText);
    walletCard = view.findViewById(R.id.walletCard);
    walletBalanceText = view.findViewById(R.id.walletBalanceText);
    bookNowCard = view.findViewById(R.id.bookNowCard);
    myCarsCard = view.findViewById(R.id.myCarsCard);
    upcomingBookingCard = view.findViewById(R.id.upcomingBookingCard);
    upcomingServiceName = view.findViewById(R.id.upcomingServiceName);
    upcomingBookingDate = view.findViewById(R.id.upcomingBookingDate);
    upcomingBookingStatus = view.findViewById(R.id.upcomingBookingStatus);
    }
    private void setupUserInfo() {
        String userEmail = tokenManager.getUserEmail();
        welcomeText.setText("Welcome Back!");
        userNameText.setText(userEmail != null ? userEmail : "Guest");
        // Will be updated by loadWalletBalance()
        walletBalanceText.setText("Loading...");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh wallet balance and upcoming booking when returning to this fragment
        loadWalletBalance();
        loadUpcomingBooking();
    }

    /**
     * Fetch wallet balance from API using Volley
     */
    private void loadWalletBalance() {
        String url = VolleyClient.BASE_URL + "/api/wallet/balance";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        double balance = response.getDouble("balance");
                        walletBalanceText.setText(String.format(Locale.US, "$%.2f", balance));
                        Log.d(TAG, "Wallet balance loaded: $" + balance);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing balance: " + e.getMessage());
                        walletBalanceText.setText("$0.00");
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading balance: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                    walletBalanceText.setText("$0.00");
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
     * Fetch upcoming booking from API using Volley
     */
    private void loadUpcomingBooking() {
        String url = VolleyClient.BASE_URL + "/api/bookings";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            // Get the first booking (most recent)
                            org.json.JSONObject booking = response.getJSONObject(0);

                            String serviceName = booking.optString("service_name", "Service");
                            String scheduledTime = booking.optString("scheduled_time", "");
                            String status = booking.optString("status", "pending");

                            // Display booking info
                            upcomingServiceName.setText(serviceName);
                            upcomingBookingDate.setText(formatBookingDate(scheduledTime));
                            upcomingBookingStatus.setText(status.toUpperCase(Locale.US));
                            upcomingBookingStatus.setVisibility(View.VISIBLE);

                            Log.d(TAG, "Upcoming booking loaded: " + serviceName);
                        } else {
                            // No bookings found
                            upcomingServiceName.setText("No upcoming bookings");
                            upcomingBookingDate.setText("Book your first wash!");
                            upcomingBookingStatus.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing booking: " + e.getMessage());
                        showDefaultBookingState();
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading booking: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                    showDefaultBookingState();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return volleyClient.getAuthHeaders();
            }
        };

        volleyClient.addToRequestQueue(request, TAG);
    }

    private String formatBookingDate(String scheduledTime) {
        if (scheduledTime == null || scheduledTime.isEmpty()) {
            return "Date not set";
        }
        // Format: "2024-01-25T14:30:00" -> "Jan 25, 2:30 PM"
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, h:mm a", Locale.US);
            java.util.Date date = inputFormat.parse(scheduledTime);
            return date != null ? outputFormat.format(date) : scheduledTime;
        } catch (Exception e) {
            return scheduledTime;
        }
    }

    private void showDefaultBookingState() {
        upcomingServiceName.setText("No upcoming bookings");
        upcomingBookingDate.setText("Book your first wash!");
        upcomingBookingStatus.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        walletCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WalletActivity.class);
            startActivity(intent);
        });

        bookNowCard.setOnClickListener(v -> {
            // Navigate to Services tab in parent activity
            if (getActivity() instanceof CustomerActivity) {
                ((CustomerActivity) getActivity()).navigateToServices();
            }
        });

        myCarsCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CarListActivity.class);
            startActivity(intent);
        });

        upcomingBookingCard.setOnClickListener(v -> {
            // Navigate to Bookings tab in parent activity
            if (getActivity() instanceof CustomerActivity) {
                ((CustomerActivity) getActivity()).navigateToBookings();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel pending Volley requests to prevent memory leaks
        if (volleyClient != null) {
            volleyClient.cancelRequests(TAG);
        }
    }
}
