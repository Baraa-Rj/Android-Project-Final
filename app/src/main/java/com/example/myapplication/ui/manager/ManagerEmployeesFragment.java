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
import com.example.myapplication.data.models.RegisterRequest;
import com.example.myapplication.data.models.AuthResponse;
import com.example.myapplication.data.models.User;
import com.example.myapplication.ui.manager.adapter.ManagerEmployeeAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerEmployeesFragment extends Fragment implements ManagerEmployeeAdapter.OnEmployeeActionListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FloatingActionButton fabAddEmployee;
    private ManagerEmployeeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_employees, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.employeesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        fabAddEmployee = view.findViewById(R.id.fabAddEmployee);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ManagerEmployeeAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAddEmployee.setOnClickListener(v -> showCreateEmployeeDialog());

        loadEmployees();
    }

    private void loadEmployees() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<User> allUsers = response.body();

                    // Filter to show only employees
                    List<User> employees = new ArrayList<>();
                    for (User user : allUsers) {
                        if (user.isEmployee()) {
                            employees.add(user);
                        }
                    }

                    adapter.setEmployees(employees);

                    if (employees.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load employees", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateEmployeeDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_employee, null);
        EditText nameInput = dialogView.findViewById(R.id.employeeNameInput);
        EditText emailInput = dialogView.findViewById(R.id.employeeEmailInput);
        EditText phoneInput = dialogView.findViewById(R.id.employeePhoneInput);
        EditText passwordInput = dialogView.findViewById(R.id.employeePasswordInput);

        new AlertDialog.Builder(getContext())
                .setTitle("Create New Employee")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();

                    if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                        Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createEmployee(name, email, phone, password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditEmployeeDialog(User employee) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_employee, null);
        EditText nameInput = dialogView.findViewById(R.id.employeeNameInput);
        EditText phoneInput = dialogView.findViewById(R.id.employeePhoneInput);

        // Pre-fill with existing data
        nameInput.setText(employee.getName());
        phoneInput.setText(employee.getPhone());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Employee")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(getContext(), "Name and phone are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateEmployee(employee, name, phone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createEmployee(String name, String email, String phone, String password) {
        RegisterRequest newEmployee = new RegisterRequest(name, email, phone, password, "employee");

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).register(newEmployee).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Employee created successfully", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                } else {
                    Toast.makeText(getContext(), "Failed to create employee", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmployee(User employee, String name, String phone) {
        // Update only the name and phone, keeping all other fields
        employee.setName(name);
        employee.setPhone(phone);

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).updateUser(employee.getId(), employee).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Employee updated successfully", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                } else {
                    Toast.makeText(getContext(), "Failed to update employee", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditEmployee(User employee) {
        showEditEmployeeDialog(employee);
    }

    @Override
    public void onDeleteEmployee(User employee) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Employee")
                .setMessage("Delete \"" + employee.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEmployee(employee.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEmployee(int employeeId) {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).deleteUser(employeeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Employee deleted successfully", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                } else {
                    Toast.makeText(getContext(), "Failed to delete employee", Toast.LENGTH_SHORT).show();
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
