package com.dhanrakshak.presentation.work;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.JobTask;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class JobTaskAdapter extends ListAdapter<JobTask, JobTaskAdapter.TaskViewHolder> {

    private final OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onLogTimeClicked(JobTask task);
    }

    public JobTaskAdapter(OnTaskActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<JobTask> DIFF_CALLBACK = new DiffUtil.ItemCallback<JobTask>() {
        @Override
        public boolean areItemsTheSame(@NonNull JobTask oldItem, @NonNull JobTask newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull JobTask oldItem, @NonNull JobTask newItem) {
            return oldItem.getHoursSpent() == newItem.getHoursSpent() &&
                    oldItem.getTitle().equals(newItem.getTitle());
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, priority, deadline, hoursLeft;
        LinearProgressIndicator progressBar;
        MaterialButton btnLogTime;

        TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            priority = itemView.findViewById(R.id.textPriority);
            deadline = itemView.findViewById(R.id.textDeadline);
            hoursLeft = itemView.findViewById(R.id.textHoursLeft);
            progressBar = itemView.findViewById(R.id.progressTask);
            btnLogTime = itemView.findViewById(R.id.btnLogTime);
        }

        void bind(JobTask task) {
            title.setText(task.getTitle());
            priority.setText(task.getPriority());

            // Priority Color
            int priorityColor = Color.GRAY;
            if ("HIGH".equals(task.getPriority()))
                priorityColor = Color.RED;
            else if ("MEDIUM".equals(task.getPriority()))
                priorityColor = Color.parseColor("#ff9800"); // Orange
            else if ("LOW".equals(task.getPriority()))
                priorityColor = Color.parseColor("#4caf50"); // Green

            priority.setBackgroundColor(priorityColor);

            // Deadline Formatting
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
            deadline.setText("Due: " + sdf.format(new java.util.Date(task.getDeadlineDate())));

            // Progress & Hours
            double remaining = Math.max(0, task.getAllocatedHours() - task.getHoursSpent());
            hoursLeft.setText(String.format(java.util.Locale.US, "%.1fh left", remaining));

            progressBar.setMax(100);
            progressBar.setProgress(task.getProgressPercentage());

            // Overtime indicator
            if (task.getHoursSpent() > task.getAllocatedHours()) {
                hoursLeft.setText(String.format(java.util.Locale.US, "+%.1fh overdue",
                        task.getHoursSpent() - task.getAllocatedHours()));
                hoursLeft.setTextColor(Color.RED);
                progressBar.setIndicatorColor(Color.RED);
            } else {
                hoursLeft.setTextColor(Color.DKGRAY);
                progressBar.setIndicatorColor(Color.BLUE); // Or theme primary
            }

            btnLogTime.setOnClickListener(v -> listener.onLogTimeClicked(task));
        }
    }
}
