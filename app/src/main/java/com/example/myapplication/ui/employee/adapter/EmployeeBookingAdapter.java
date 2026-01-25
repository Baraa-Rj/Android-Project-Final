package com.example.myapplication.ui.employee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.models.Booking;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeBookingAdapter extends RecyclerView.Adapter<EmployeeBookingAdapter.BookingViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public EmployeeBookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView serviceName;
        private final TextView statusChip;
        private final TextView carInfo;
        private final TextView bookingDateTime;
        private final TextView bookingLocation;
        private final TextView bookingPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            statusChip = itemView.findViewById(R.id.statusChip);
            carInfo = itemView.findViewById(R.id.carInfo);
            bookingDateTime = itemView.findViewById(R.id.bookingDateTime);
            bookingLocation = itemView.findViewById(R.id.bookingLocation);
            bookingPrice = itemView.findViewById(R.id.bookingPrice);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookingClick(bookings.get(position));
                }
            });
        }

        public void bind(Booking booking) {
            String service = booking.getServiceName() != null
                    ? booking.getServiceName()
                    : "Service #" + booking.getServiceId();
            serviceName.setText(service);

            String car = booking.getCarModel() != null
                    ? booking.getCarModel()
                    : "Car #" + booking.getCarId();
            carInfo.setText(car);

            String status = booking.getStatus();
            statusChip.setText(capitalizeFirst(status));
            statusChip.setBackgroundColor(getStatusColor(status));

            bookingDateTime.setText(booking.getScheduledTime());

            String location = booking.getLocation() != null
                    ? booking.getLocation()
                    : "Location not set";
            bookingLocation.setText(location);

            bookingPrice.setText(String.format(Locale.getDefault(), "$%.2f", booking.getTotalPrice()));
        }

        private int getStatusColor(String status) {
            if (status == null)
                return ContextCompat.getColor(itemView.getContext(), R.color.status_pending);

            switch (status.toLowerCase()) {
                case "pending":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_pending);
                case "assigned":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_assigned);
                case "in_progress":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_in_progress);
                case "completed":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_completed);
                case "cancelled":
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_cancelled);
                default:
                    return ContextCompat.getColor(itemView.getContext(), R.color.status_pending);
            }
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty())
                return "";
            return text.substring(0, 1).toUpperCase() + text.substring(1).replace("_", " ");
        }
    }
}
