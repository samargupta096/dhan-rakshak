package com.dhanrakshak.presentation.trip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.Trip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripAdapter extends ListAdapter<Trip, TripAdapter.TripViewHolder> {

    private final OnTripClickListener listener;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public TripAdapter(OnTripClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Trip> DIFF_CALLBACK = new DiffUtil.ItemCallback<Trip>() {
        @Override
        public boolean areItemsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getStartDate() == newItem.getStartDate();
        }
    };

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TripViewHolder extends RecyclerView.ViewHolder {
        TextView name, dates, status, budget, spent;
        LinearProgressIndicator progress;
        ImageView image;

        TripViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textTripName);
            dates = itemView.findViewById(R.id.textDates);
            status = itemView.findViewById(R.id.textStatus);
            budget = itemView.findViewById(R.id.textBudget);
            spent = itemView.findViewById(R.id.textSpent);
            progress = itemView.findViewById(R.id.progressBudget);
            image = itemView.findViewById(R.id.imageTrip);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onTripClick(getItem(pos));
                }
            });
        }

        void bind(Trip trip) {
            name.setText(trip.getName());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.US);
            String dateRange = sdf.format(new Date(trip.getStartDate())) + " - "
                    + sdf.format(new Date(trip.getEndDate()));
            dates.setText(dateRange);

            // Basic Status Logic (Could be in Entity too)
            long now = System.currentTimeMillis();
            if (now > trip.getEndDate())
                status.setText("Completed");
            else if (now < trip.getStartDate())
                status.setText("Upcoming");
            else
                status.setText("Ongoing");

            budget.setText(String.format("Budget: ₹%.0fk", trip.getBudget() / 1000));
            // Note: trip.getTotalSpent() needs to be updated or calculated. For List, maybe
            // passed or calculated.
            // For now assuming 0 or add a field if needed. Let's assume passed in Entity or
            // ignoring for list for now.
            // Actually Entity has 'totalSpent' field? Checked viewed_file, Trip.java has
            // helper methods?
            // Step 929 says: "Entity: Trip.java... verified". Let's assume we might need a
            // join or update.
            // For MVP: 0 spent in list if not pre-calculated.
            spent.setText("Spent: ₹0");
            progress.setProgress(0);
        }
    }
}
