package com.example.myapplication.ui.manager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Service;
import com.example.myapplication.ui.manager.adapter.ManagerServiceAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerServicesFragment extends Fragment implements ManagerServiceAdapter.OnServiceActionListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FloatingActionButton fabAddService;
    private ManagerServiceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.servicesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        fabAddService = view.findViewById(R.id.fabAddService);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ManagerServiceAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAddService.setOnClickListener(v -> showCreateServiceDialog());

        loadServices();
    }

    private void loadServices() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).getServices().enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Service> services = response.body();
                    adapter.setServices(services);

                    if (services.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load services", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateServiceDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_service, null);
        EditText nameInput = dialogView.findViewById(R.id.serviceNameInput);
        EditText descInput = dialogView.findViewById(R.id.serviceDescriptionInput);
        EditText priceInput = dialogView.findViewById(R.id.servicePriceInput);
        EditText durationInput = dialogView.findViewById(R.id.serviceDurationInput);

        new AlertDialog.Builder(getContext())
                .setTitle("Create New Service")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descInput.getText().toString().trim();
                    String priceStr = priceInput.getText().toString().trim();
                    String durationStr = durationInput.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty() || durationStr.isEmpty()) {
                        Toast.makeText(getContext(), "Name, price, and duration are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);
                        int duration = Integer.parseInt(durationStr);
                        createService(name, description, price, duration);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid price or duration", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditServiceDialog(Service service) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_service, null);
        EditText nameInput = dialogView.findViewById(R.id.serviceNameInput);
        EditText descInput = dialogView.findViewById(R.id.serviceDescriptionInput);
        EditText priceInput = dialogView.findViewById(R.id.servicePriceInput);
        EditText durationInput = dialogView.findViewById(R.id.serviceDurationInput);

        // Pre-fill with existing data
        nameInput.setText(service.getName());
        descInput.setText(service.getDescription());
        priceInput.setText(String.valueOf(service.getPrice()));
        durationInput.setText(String.valueOf(service.getDuration()));

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Service")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descInput.getText().toString().trim();
                    String priceStr = priceInput.getText().toString().trim();
                    String durationStr = durationInput.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty() || durationStr.isEmpty()) {
                        Toast.makeText(getContext(), "Name, price, and duration are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);
                        int duration = Integer.parseInt(durationStr);
                        updateService(service.getId(), name, description, price, duration);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid price or duration", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createService(String name, String description, double price, int duration) {
        Service newService = new Service();
        newService.setName(name);
        newService.setDescription(description);
        newService.setPrice(price);
        newService.setDuration(duration);

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).createService(newService).enqueue(new Callback<Service>() {
            @Override
            public void onResponse(Call<Service> call, Response<Service> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Service created successfully", Toast.LENGTH_SHORT).show();
                    loadServices();
                } else {
                    Toast.makeText(getContext(), "Failed to create service", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Service> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateService(int serviceId, String name, String description, double price, int duration) {
        Service updatedService = new Service();
        updatedService.setId(serviceId);
        updatedService.setName(name);
        updatedService.setDescription(description);
        updatedService.setPrice(price);
        updatedService.setDuration(duration);

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).updateService(serviceId, updatedService).enqueue(new Callback<Service>() {
            @Override
            public void onResponse(Call<Service> call, Response<Service> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Service updated successfully", Toast.LENGTH_SHORT).show();
                    loadServices();
                } else {
                    Toast.makeText(getContext(), "Failed to update service", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Service> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditService(Service service) {
        showEditServiceDialog(service);
    }

    @Override
    public void onDeleteService(Service service) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Service")
                .setMessage("Delete \"" + service.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteService(service.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteService(int serviceId) {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).deleteService(serviceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Service deleted successfully", Toast.LENGTH_SHORT).show();
                    loadServices();
                } else {
                    Toast.makeText(getContext(), "Failed to delete service", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
