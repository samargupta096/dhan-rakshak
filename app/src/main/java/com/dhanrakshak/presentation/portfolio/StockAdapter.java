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
 * Adapter for Stock list.
 */
public class StockAdapter extends ListAdapter<Asset, StockAdapter.StockViewHolder> {

    private final OnItemClickListener clickListener;
    private final OnDeleteListener deleteListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public interface OnItemClickListener {
        void onClick(Asset asset);
    }

    public interface OnDeleteListener {
        void onDelete(Asset asset);
    }

    public StockAdapter(OnItemClickListener clickListener, OnDeleteListener deleteListener) {
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
            return oldItem.getCurrentValue() == newItem.getCurrentValue() &&
                    oldItem.getQuantity() == newItem.getQuantity();
        }
    };

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAssetBinding binding = ItemAssetBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StockViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class StockViewHolder extends RecyclerView.ViewHolder {
        private final ItemAssetBinding binding;

        StockViewHolder(ItemAssetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Asset stock) {
            binding.textName.setText(stock.getName());
            binding.textIdentifier.setText(stock.getIdentifier() + " • " +
                    String.format("%.2f shares", stock.getQuantity()));

            // Current value
            binding.textValue.setText(currencyFormat.format(stock.getCurrentValue()));

            // Profit/Loss
            double profitLoss = stock.getProfitLoss();
            double profitLossPercent = stock.getProfitLossPercentage();
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

            // Average price
            binding.textAvgPrice.setText("Avg: " + currencyFormat.format(stock.getAveragePrice()));

            // Click listeners
            binding.getRoot().setOnClickListener(v -> clickListener.onClick(stock));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(stock));
        }
    }
}
