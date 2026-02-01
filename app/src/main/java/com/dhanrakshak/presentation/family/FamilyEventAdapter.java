package com.dhanrakshak.presentation.family;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.FamilyEvent;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FamilyEventAdapter extends RecyclerView.Adapter<FamilyEventAdapter.ViewHolder> {

    private List<FamilyEvent> events = new ArrayList<>();

    public void setEvents(List<FamilyEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FamilyEvent event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvTime, tvNotes;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            cardView = (MaterialCardView) itemView;
        }

        public void bind(FamilyEvent event) {
            tvTitle.setText(event.title);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
            tvDate.setText(dateFormat.format(new Date(event.date)));

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String start = timeFormat.format(new Date(event.startTime));
            String end = timeFormat.format(new Date(event.endTime));
            tvTime.setText(start + " - " + end);

            if (event.notes != null && !event.notes.isEmpty()) {
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText(event.notes);
            } else {
                tvNotes.setVisibility(View.GONE);
            }
        }
    }
}
