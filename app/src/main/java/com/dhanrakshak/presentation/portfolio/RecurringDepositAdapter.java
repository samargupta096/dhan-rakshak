package com.dhanrakshak.presentation.portfolio;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.RecurringDeposit;
import com.dhanrakshak.databinding.ItemDepositBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for Recurring Deposit list.
 */
public class RecurringDepositAdapter extends ListAdapter<RecurringDeposit, RecurringDepositAdapter.RdViewHolder> {

    private final OnItemClickListener clickListener;
    private final OnDeleteListener deleteListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onClick(RecurringDeposit rd);
    }

    public interface OnDeleteListener {
        void onDelete(RecurringDeposit rd);
    }

    public RecurringDepositAdapter(OnItemClickListener clickListener, OnDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    private static final DiffUtil.ItemCallback<RecurringDeposit> DIFF_CALLBACK = new DiffUtil.ItemCallback<RecurringDeposit>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecurringDeposit oldItem, @NonNull RecurringDeposit newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecurringDeposit oldItem, @NonNull RecurringDeposit newItem) {
            return oldItem.getDepositedAmount() == newItem.getDepositedAmount();
        }
    };

    @NonNull
    @Override
    public RdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepositBinding binding = ItemDepositBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RdViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RdViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RdViewHolder extends RecyclerView.ViewHolder {
        private final ItemDepositBinding binding;

        RdViewHolder(ItemDepositBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RecurringDeposit rd) {
            binding.textBankName.setText(rd.getBankName());
            binding.textType.setText("Recurring Deposit");
            binding.textPrincipal.setText("Monthly: " + currencyFormat.format(rd.getMonthlyAmount()));
            binding.textInterestRate.setText(String.format("%.2f%% p.a.", rd.getInterestRate()));
            binding.textMaturityDate.setText("Matures: " + dateFormat.format(new Date(rd.getMaturityDate())));
            binding.textMaturityAmount.setText("Maturity: " + currencyFormat.format(rd.getMaturityAmount()));
            binding.textCurrentValue.setText("Deposited: " + currencyFormat.format(rd.getDepositedAmount()));

            // Installments progress
            String progress = rd.getInstallmentsPaid() + "/" + rd.getTenureMonths() + " installments";
            binding.textDaysRemaining.setText(progress);
            binding.textDaysRemaining.setTextColor(
                    binding.getRoot().getContext().getResources().getColor(R.color.secondary, null));

            binding.getRoot().setOnClickListener(v -> clickListener.onClick(rd));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(rd));
        }
    }
}
