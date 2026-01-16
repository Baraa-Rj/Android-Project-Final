
package com.example.myapplication.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.models.Service;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services = new ArrayList<>();
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onBookClick(Service service);
    }

    public ServiceAdapter(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void setServices(List<Service> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView serviceName;
        private final TextView serviceDescription;
        private final TextView serviceDuration;
        private final TextView servicePrice;
        private final MaterialButton bookButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            serviceDuration = itemView.findViewById(R.id.serviceDuration);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            bookButton = itemView.findViewById(R.id.bookButton);
        }

        public void bind(Service service) {
            serviceName.setText(service.getName());
            serviceDescription.setText(service.getDescription());
            serviceDuration.setText(String.format(Locale.getDefault(), "%d min", service.getDuration()));
            servicePrice.setText(String.format(Locale.getDefault(), "$%.2f", service.getPrice()));

            bookButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(service);
                }
            });
        }
    }
}