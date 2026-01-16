package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Service;
import com.example.myapplication.ui.customer.adapter.ServiceAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerServicesFragment extends Fragment implements ServiceAdapter.OnServiceClickListener {

    private RecyclerView servicesRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private ServiceAdapter serviceAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        loadServices();
    }

    private void initViews(View view) {
        servicesRecyclerView = view.findViewById(R.id.servicesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        apiService = RetrofitClient.getApiService(requireContext());
    }

    private void setupRecyclerView() {
        serviceAdapter = new ServiceAdapter(this);
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        servicesRecyclerView.setAdapter(serviceAdapter);
    }

    private void loadServices() {
        showLoading(true);

        apiService.getServices().enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(@NonNull Call<List<Service>> call, @NonNull Response<List<Service>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Service> services = response.body();
                    if (services.isEmpty()) {
                        showEmpty(true);
                    } else {
                        showEmpty(false);
                        serviceAdapter.setServices(services);
                    }
                } else {
                    showError("Failed to load services");
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        servicesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        servicesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookClick(Service service) {
        // TODO: Navigate to BookingActivity with service data
        Toast.makeText(requireContext(), "Book: " + service.getName(), Toast.LENGTH_SHORT).show();
    }
}