package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.utils.TokenManager;

public class CustomerHomeFragment extends Fragment {
    private TextView welcomeText;
    private TextView userNameText;
    private CardView walletCard;
    private TextView walletBalanceText;
    private CardView bookNowCard;
    private CardView myCarsCard;
    private CardView upcomingBookingCard;
    private TokenManager tokenManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = TokenManager.getInstance(getContext());
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
    }
    private void setupUserInfo() {
        String userEmail = tokenManager.getUserEmail();
        welcomeText.setText("Welcome Back!");
        userNameText.setText(userEmail != null ? userEmail : "Guest");
        walletBalanceText.setText("$100.00");
    }
    private void setupClickListeners() {
        walletCard.setOnClickListener(v -> {
            // Handle wallet card click
        });

        bookNowCard.setOnClickListener(v -> {
            // Handle book now card click
        });

        myCarsCard.setOnClickListener(v -> {
            // Handle my cars card click
        });

        upcomingBookingCard.setOnClickListener(v -> {
            // Handle upcoming booking card click
        });
    }
}
