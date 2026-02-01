package com.dhanrakshak.presentation.insights;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.ai.insights.AiFinanceInsightsEngine.WhatIfScenario;
import com.dhanrakshak.databinding.ItemWhatIfBinding;

/**
 * Adapter for what-if investment scenarios.
 */
public class WhatIfAdapter extends ListAdapter<WhatIfScenario, WhatIfAdapter.WhatIfViewHolder> {

    public WhatIfAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<WhatIfScenario> DIFF_CALLBACK = new DiffUtil.ItemCallback<WhatIfScenario>() {
        @Override
        public boolean areItemsTheSame(@NonNull WhatIfScenario oldItem, @NonNull WhatIfScenario newItem) {
            return oldItem.title.equals(newItem.title);
        }

        @Override
        public boolean areContentsTheSame(@NonNull WhatIfScenario oldItem, @NonNull WhatIfScenario newItem) {
            return oldItem.title.equals(newItem.title);
        }
    };

    @NonNull
    @Override
    public WhatIfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWhatIfBinding binding = ItemWhatIfBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WhatIfViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WhatIfViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class WhatIfViewHolder extends RecyclerView.ViewHolder {
        private final ItemWhatIfBinding binding;

        WhatIfViewHolder(ItemWhatIfBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WhatIfScenario scenario) {
            binding.textTitle.setText(scenario.title);

            // Build results text
            StringBuilder resultsText = new StringBuilder();
            for (String result : scenario.results) {
                resultsText.append("• ").append(result).append("\n");
            }
            binding.textResults.setText(resultsText.toString().trim());

            // Category badge color
            int categoryColor;
            switch (scenario.category) {
                case "EQUITY":
                    categoryColor = binding.getRoot().getContext().getResources()
                            .getColor(R.color.chart_stocks, null);
                    break;
                case "DEBT":
                    categoryColor = binding.getRoot().getContext().getResources()
                            .getColor(R.color.chart_epf, null);
                    break;
                default:
                    categoryColor = binding.getRoot().getContext().getResources()
                            .getColor(R.color.secondary, null);
            }
            binding.textCategory.setText(scenario.category);
            binding.textCategory.setTextColor(categoryColor);
        }
    }
}
