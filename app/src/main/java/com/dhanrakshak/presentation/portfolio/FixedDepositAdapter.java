package com.dhanrakshak.presentation.portfolio;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.FixedDeposit;
import com.dhanrakshak.databinding.ItemDepositBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for Fixed Deposit list.
 */
public class FixedDepositAdapter extends ListAdapter<FixedDeposit, FixedDepositAdapter.FdViewHolder> {

    private final OnItemClickListener clickListener;
    private final OnDeleteListener deleteListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onClick(FixedDeposit fd);
    }

    public interface OnDeleteListener {
        void onDelete(FixedDeposit fd);
    }

    public FixedDepositAdapter(OnItemClickListener clickListener, OnDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    private static final DiffUtil.ItemCallback<FixedDeposit> DIFF_CALLBACK = new DiffUtil.ItemCallback<FixedDeposit>() {
        @Override
        public boolean areItemsTheSame(@NonNull FixedDeposit oldItem, @NonNull FixedDeposit newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FixedDeposit oldItem, @NonNull FixedDeposit newItem) {
            return oldItem.getPrincipal() == newItem.getPrincipal();
        }
    };

    @NonNull
    @Override
    public FdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepositBinding binding = ItemDepositBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FdViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FdViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FdViewHolder extends RecyclerView.ViewHolder {
        private final ItemDepositBinding binding;

        FdViewHolder(ItemDepositBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FixedDeposit fd) {
            binding.textBankName.setText(fd.getBankName());
            binding.textType.setText("Fixed Deposit");
            binding.textPrincipal.setText("Principal: " + currencyFormat.format(fd.getPrincipal()));
            binding.textInterestRate.setText(String.format("%.2f%% p.a.", fd.getInterestRate()));
            binding.textMaturityDate.setText("Matures: " + dateFormat.format(new Date(fd.getMaturityDate())));
            binding.textMaturityAmount.setText("Maturity: " + currencyFormat.format(fd.getMaturityAmount()));
            binding.textCurrentValue.setText("Current: " + currencyFormat.format(fd.getCurrentValue()));

            // Days to maturity
            long daysToMaturity = (fd.getMaturityDate() - System.currentTimeMillis()) / (24 * 60 * 60 * 1000);
            if (daysToMaturity > 0) {
                binding.textDaysRemaining.setText(daysToMaturity + " days remaining");
                binding.textDaysRemaining.setTextColor(
                        binding.getRoot().getContext().getResources().getColor(R.color.credit_green, null));
            } else {
                binding.textDaysRemaining.setText("Matured");
                binding.textDaysRemaining.setTextColor(
                        binding.getRoot().getContext().getResources().getColor(R.color.chart_gold, null));
            }

            binding.getRoot().setOnClickListener(v -> clickListener.onClick(fd));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(fd));
        }
    }
}
