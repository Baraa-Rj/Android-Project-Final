package com.example.myapplication.ui.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.models.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManagerServiceAdapter extends RecyclerView.Adapter<ManagerServiceAdapter.ServiceViewHolder> {

    private List<Service> services = new ArrayList<>();
    private OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onEditService(Service service);
        void onDeleteService(Service service);
    }

    public ManagerServiceAdapter(OnServiceActionListener listener) {
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
                .inflate(R.layout.item_service_manager, parent, false);
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
        private final TextView servicePrice;
        private final TextView serviceDuration;
        private final ImageButton editServiceButton;
        private final ImageButton deleteServiceButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            serviceDuration = itemView.findViewById(R.id.serviceDuration);
            editServiceButton = itemView.findViewById(R.id.editServiceButton);
            deleteServiceButton = itemView.findViewById(R.id.deleteServiceButton);
        }

        public void bind(Service service) {
            serviceName.setText(service.getName());

            String description = service.getDescription() != null && !service.getDescription().isEmpty()
                    ? service.getDescription()
                    : "No description";
            serviceDescription.setText(description);

            servicePrice.setText(String.format(Locale.getDefault(), "$%.2f", service.getPrice()));
            serviceDuration.setText("• " + service.getDuration() + " min");

            editServiceButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditService(service);
                }
            });

            deleteServiceButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteService(service);
                }
            });
        }
    }
}
