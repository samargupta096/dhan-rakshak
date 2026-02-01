package com.dhanrakshak.presentation.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.SmsTransaction;
import com.dhanrakshak.databinding.ItemTransactionBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for recent transactions RecyclerView.
 */
public class RecentTransactionsAdapter
        extends ListAdapter<SmsTransaction, RecentTransactionsAdapter.TransactionViewHolder> {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    public RecentTransactionsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SmsTransaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<SmsTransaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull SmsTransaction oldItem, @NonNull SmsTransaction newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SmsTransaction oldItem, @NonNull SmsTransaction newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getType().equals(newItem.getType()) &&
                    oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SmsTransaction transaction) {
            // Merchant name
            String merchant = transaction.getMerchant();
            if (merchant == null || merchant.isEmpty()) {
                merchant = "Transaction";
            }
            binding.textMerchant.setText(merchant);

            // Category
            String category = transaction.getCategory();
            if (category == null || category.isEmpty()) {
                category = "Uncategorized";
            }
            binding.textCategory.setText(category);

            // Date
            binding.textDate.setText(dateFormat.format(new Date(transaction.getTimestamp())));

            // Amount with color based on type
            String amountText = currencyFormat.format(transaction.getAmount());
            if (transaction.isDebit()) {
                binding.textAmount.setText("-" + amountText);
                binding.textAmount.setTextColor(
                        binding.getRoot().getContext().getResources().getColor(R.color.debit_red, null));
            } else {
                binding.textAmount.setText("+" + amountText);
                binding.textAmount.setTextColor(
                        binding.getRoot().getContext().getResources().getColor(R.color.credit_green, null));
            }
        }
    }
}
