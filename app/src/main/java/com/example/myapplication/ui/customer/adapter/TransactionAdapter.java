package com.example.myapplication.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.models.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(0, transaction); // Add to beginning
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView transactionIcon;
        private final TextView transactionDescription;
        private final TextView transactionDate;
        private final TextView transactionAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionIcon = itemView.findViewById(R.id.transactionIcon);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
        }

        public void bind(Transaction transaction) {
            // Set description
            transactionDescription.setText(transaction.getDescription());

            // Set date
            transactionDate.setText(formatDate(transaction.getCreatedAt()));

            // Set amount with sign and color based on type
            if (transaction.isCredit()) {
                transactionAmount.setText(String.format(Locale.US, "+$%.2f", transaction.getAmount()));
                transactionAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_completed));
                transactionIcon.setImageResource(R.drawable.ic_add);
                transactionIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.status_completed));
            } else {
                transactionAmount.setText(String.format(Locale.US, "-$%.2f", transaction.getAmount()));
                transactionAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error));
                transactionIcon.setImageResource(R.drawable.ic_remove);
                transactionIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.error));
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }

            try {
                // Parse ISO format from API
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = inputFormat.parse(dateString);

                // Format for display
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                return outputFormat.format(date);
            } catch (ParseException e) {
                // Try alternative format
                try {
                    SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    Date date = altFormat.parse(dateString);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    return outputFormat.format(date);
                } catch (ParseException ex) {
                    return dateString;
                }
            }
        }
    }
}
