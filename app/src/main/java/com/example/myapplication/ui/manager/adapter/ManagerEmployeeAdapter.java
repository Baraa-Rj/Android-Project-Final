package com.example.myapplication.ui.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.models.User;
import java.util.ArrayList;
import java.util.List;

public class ManagerEmployeeAdapter extends RecyclerView.Adapter<ManagerEmployeeAdapter.EmployeeViewHolder> {

    private List<User> employees = new ArrayList<>();
    private OnEmployeeActionListener listener;

    public interface OnEmployeeActionListener {
        void onEditEmployee(User employee);
        void onDeleteEmployee(User employee);
    }

    public ManagerEmployeeAdapter(OnEmployeeActionListener listener) {
        this.listener = listener;
    }

    public void setEmployees(List<User> employees) {
        this.employees = employees;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        User employee = employees.get(position);
        holder.bind(employee);
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final TextView employeeName;
        private final TextView employeeEmail;
        private final TextView employeePhone;
        private final ImageButton editEmployeeButton;
        private final ImageButton deleteEmployeeButton;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            employeeName = itemView.findViewById(R.id.employeeName);
            employeeEmail = itemView.findViewById(R.id.employeeEmail);
            employeePhone = itemView.findViewById(R.id.employeePhone);
            editEmployeeButton = itemView.findViewById(R.id.editEmployeeButton);
            deleteEmployeeButton = itemView.findViewById(R.id.deleteEmployeeButton);
        }

        public void bind(User employee) {
            employeeName.setText(employee.getName() != null ? employee.getName() : "Unknown");
            employeeEmail.setText(employee.getEmail() != null ? employee.getEmail() : "No email");
            employeePhone.setText(employee.getPhone() != null ? employee.getPhone() : "No phone");

            editEmployeeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditEmployee(employee);
                }
            });

            deleteEmployeeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteEmployee(employee);
                }
            });
        }
    }
}
