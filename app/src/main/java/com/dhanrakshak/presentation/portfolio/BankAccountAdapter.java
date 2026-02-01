package com.dhanrakshak.presentation.portfolio;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.databinding.ItemBankAccountBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for Bank Account list.
 */
public class BankAccountAdapter extends ListAdapter<BankAccount, BankAccountAdapter.BankViewHolder> {

    private final OnItemClickListener clickListener;
    private final OnDeleteListener deleteListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onClick(BankAccount account);
    }

    public interface OnDeleteListener {
        void onDelete(BankAccount account);
    }

    public BankAccountAdapter(OnItemClickListener clickListener, OnDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    private static final DiffUtil.ItemCallback<BankAccount> DIFF_CALLBACK = new DiffUtil.ItemCallback<BankAccount>() {
        @Override
        public boolean areItemsTheSame(@NonNull BankAccount oldItem, @NonNull BankAccount newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BankAccount oldItem, @NonNull BankAccount newItem) {
            return oldItem.getBalance() == newItem.getBalance();
        }
    };

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBankAccountBinding binding = ItemBankAccountBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BankViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class BankViewHolder extends RecyclerView.ViewHolder {
        private final ItemBankAccountBinding binding;

        BankViewHolder(ItemBankAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BankAccount account) {
            binding.textBankName.setText(account.getBankName());
            binding.textAccountType.setText(account.getAccountType() + " •••• " + account.getLastFourDigits());
            binding.textBalance.setText(currencyFormat.format(account.getBalance()));

            if (account.getLastUpdated() > 0) {
                binding.textLastUpdated.setText("Updated: " + dateFormat.format(new Date(account.getLastUpdated())));
            } else {
                binding.textLastUpdated.setText("Not updated");
            }

            binding.getRoot().setOnClickListener(v -> clickListener.onClick(account));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(account));
        }
    }
}
