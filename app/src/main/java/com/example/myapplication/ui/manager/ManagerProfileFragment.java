package com.example.myapplication.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.MainActivity;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.User;
import com.example.myapplication.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerProfileFragment extends Fragment {
    private TextView nameText;
    private TextView emailText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        loadUserInfo();

        logoutButton.setOnClickListener(v -> {
            TokenManager tokenManager = new TokenManager(requireContext());
            tokenManager.clearToken();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void loadUserInfo() {
        TokenManager tokenManager = new TokenManager(requireContext());
        int userId = tokenManager.getUserId();
        String userEmail = tokenManager.getUserEmail();

        if (userEmail != null) {
            emailText.setText(userEmail);
        }

        RetrofitClient.getApiService(requireContext()).getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    nameText.setText(user.getName());
                    emailText.setText(user.getEmail());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Keep default values
            }
        });
    }
}
