package com.dhanrakshak.presentation.insights;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.ai.insights.AiFinanceInsightsEngine.InvestmentSuggestion;
import com.dhanrakshak.databinding.ItemSuggestionBinding;

/**
 * Adapter for AI investment suggestions.
 */
public class SuggestionsAdapter extends ListAdapter<InvestmentSuggestion, SuggestionsAdapter.SuggestionViewHolder> {

    public SuggestionsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<InvestmentSuggestion> DIFF_CALLBACK = new DiffUtil.ItemCallback<InvestmentSuggestion>() {
        @Override
        public boolean areItemsTheSame(@NonNull InvestmentSuggestion oldItem, @NonNull InvestmentSuggestion newItem) {
            return oldItem.title.equals(newItem.title);
        }

        @Override
        public boolean areContentsTheSame(@NonNull InvestmentSuggestion oldItem,
                @NonNull InvestmentSuggestion newItem) {
            return oldItem.title.equals(newItem.title) &&
                    oldItem.description.equals(newItem.description);
        }
    };

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSuggestionBinding binding = ItemSuggestionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SuggestionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private final ItemSuggestionBinding binding;

        SuggestionViewHolder(ItemSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(InvestmentSuggestion suggestion) {
            binding.textTitle.setText(suggestion.title);
            binding.textDescription.setText(suggestion.description);
            binding.textAction.setText(suggestion.actionItem);

            // Priority badge color
            int priorityColor;
            switch (suggestion.priority) {
                case "HIGH":
                    priorityColor = binding.getRoot().getContext().getResources()
                            .getColor(R.color.debit_red, null);
                    break;
                case "MEDIUM":
                    priorityColor = binding.getRoot().getContext().getResources()
                            .getColor(R.color.chart_gold, null);
                    break;
                default:
                    priorityColor = binding.getRoot().getContext().getResources()
                            .getColor(R.color.credit_green, null);
            }
            binding.textPriority.setText(suggestion.priority);
            binding.textPriority.setTextColor(priorityColor);
        }
    }
}
