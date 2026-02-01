package com.dhanrakshak.presentation.trip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.TripExpense;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripExpenseAdapter extends ListAdapter<TripExpense, TripExpenseAdapter.ExpenseViewHolder> {

    public TripExpenseAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TripExpense> DIFF_CALLBACK = new DiffUtil.ItemCallback<TripExpense>() {
        @Override
        public boolean areItemsTheSame(@NonNull TripExpense oldItem, @NonNull TripExpense newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TripExpense oldItem, @NonNull TripExpense newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getCategory().equals(newItem.getCategory());
        }
    };

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView category, date, amount;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.textCategory);
            date = itemView.findViewById(R.id.textDate);
            amount = itemView.findViewById(R.id.textAmount);
        }

        void bind(TripExpense expense) {
            category.setText(expense.getCategory());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.US);
            date.setText(sdf.format(new Date(expense.getDate())));

            amount.setText(String.format("₹%.0f", expense.getAmount()));
        }
    }
}
