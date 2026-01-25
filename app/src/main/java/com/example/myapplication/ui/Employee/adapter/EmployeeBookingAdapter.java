package com.example.myapplication.ui.Employee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.models.EmployeeBooking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeBookingAdapter  extends RecyclerView.Adapter<EmployeeBookingAdapter.JobViewHolder>{
    private List<EmployeeBooking> bookings = new ArrayList<>();
    private OnJobClickListener listener;

    public interface OnJobClickListener {
        void onJobClick(EmployeeBooking booking);
    }

    public EmployeeBookingAdapter(OnJobClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<EmployeeBooking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        EmployeeBooking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class JobViewHolder extends RecyclerView.ViewHolder {
        private final TextView jobIdText;
        private final TextView serviceNameText;
        private final TextView customerNameText;
        private final TextView locationText;
        private final TextView scheduledTimeText;
        private final TextView statusChip;
        private final TextView carModelText;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobIdText = itemView.findViewById(R.id.jobIdText);
            serviceNameText = itemView.findViewById(R.id.serviceNameText);
            customerNameText = itemView.findViewById(R.id.customerNameText);
            locationText = itemView.findViewById(R.id.locationText);
            scheduledTimeText = itemView.findViewById(R.id.scheduledTimeText);
            statusChip = itemView.findViewById(R.id.statusChip);
            carModelText = itemView.findViewById(R.id.carModelText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onJobClick(bookings.get(position));
                }
            });
        }

        public void bind(EmployeeBooking booking) {
            jobIdText.setText("Job #" + booking.getId());

            String serviceName = booking.getServiceName() != null
                    ? booking.getServiceName()
                    : "Service #" + booking.getServiceId();
            serviceNameText.setText(serviceName);

            String customerName = booking.getCustomerName() != null
                    ? booking.getCustomerName()
                    : "Customer";
            customerNameText.setText(customerName);

            locationText.setText(booking.getLocation() != null ? booking.getLocation() : "Location N/A");
            scheduledTimeText.setText(formatDateTime(booking.getScheduledTime()));

            String carModel = booking.getCarModel() != null
                    ? booking.getCarModel()
                    : "Car #" + booking.getCarId();
            carModelText.setText(carModel);

            // Status chip
            String status = booking.getStatus();
            statusChip.setText(capitalizeStatus(status));
            statusChip.setBackgroundColor(getStatusColor(status));
        }

        private String formatDateTime(String isoDateTime) {
            if (isoDateTime == null || isoDateTime.isEmpty()) {
                return "N/A";
            }

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd - hh:mm a", Locale.US);
                return outputFormat.format(inputFormat.parse(isoDateTime));
            } catch (Exception e) {
                return isoDateTime;
            }
        }

        private int getStatusColor(String status) {
            if (status == null) {
                return ContextCompat.getColor(itemView.getContext(), R.color.status_pending);
            }

            switch (status.toLowerCase()) {
                case "pending":
                case "assigned":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_assigned);
                case "in_progress":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_in_progress);
                case "completed":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_completed);
                default:
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_pending);
            }
        }

        private String capitalizeStatus(String status) {
            if (status == null || status.isEmpty()) return "";
            return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase().replace("_", " ");
        }
    }

}
