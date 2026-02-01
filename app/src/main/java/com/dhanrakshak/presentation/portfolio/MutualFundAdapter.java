package com.dhanrakshak.presentation.portfolio;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.databinding.ItemAssetBinding;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Adapter for Mutual Fund list.
 */
public class MutualFundAdapter extends ListAdapter<Asset, MutualFundAdapter.MfViewHolder> {

    private final OnItemClickListener clickListener;
    private final OnDeleteListener deleteListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public interface OnItemClickListener {
        void onClick(Asset asset);
    }

    public interface OnDeleteListener {
        void onDelete(Asset asset);
    }

    public MutualFundAdapter(OnItemClickListener clickListener, OnDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    private static final DiffUtil.ItemCallback<Asset> DIFF_CALLBACK = new DiffUtil.ItemCallback<Asset>() {
        @Override
        public boolean areItemsTheSame(@NonNull Asset oldItem, @NonNull Asset newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Asset oldItem, @NonNull Asset newItem) {
            return oldItem.getCurrentValue() == newItem.getCurrentValue();
        }
    };

    @NonNull
    @Override
    public MfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAssetBinding binding = ItemAssetBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MfViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MfViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MfViewHolder extends RecyclerView.ViewHolder {
        private final ItemAssetBinding binding;

        MfViewHolder(ItemAssetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Asset mf) {
            binding.textName.setText(mf.getName());
            binding.textIdentifier.setText(String.format("%.4f units", mf.getQuantity()));

            binding.textValue.setText(currencyFormat.format(mf.getCurrentValue()));

            double profitLoss = mf.getProfitLoss();
            double profitLossPercent = mf.getProfitLossPercentage();
            String plText = String.format("%s (%.2f%%)",
                    currencyFormat.format(Math.abs(profitLoss)), Math.abs(profitLossPercent));

            if (profitLoss >= 0) {
                binding.textProfitLoss.setText("+" + plText);
                binding.textProfitLoss.setTextColor(
                        binding.getRoot().getContext().getResources().getColor(R.color.credit_green, null));
            } else {
                binding.textProfitLoss.setText("-" + plText);
                binding.textProfitLoss.setTextColor(
                        binding.getRoot().getContext().getResources().getColor(R.color.debit_red, null));
            }

            binding.textAvgPrice.setText("Avg NAV: " + currencyFormat.format(mf.getAveragePrice()));

            binding.getRoot().setOnClickListener(v -> clickListener.onClick(mf));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(mf));
        }
    }
}
