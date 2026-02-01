package com.dhanrakshak.presentation.work;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.JobTask;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.datepicker.MaterialDatePicker;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WorkDashboardFragment extends Fragment implements JobTaskAdapter.OnTaskActionListener {

    private WorkViewModel viewModel;
    private JobTaskAdapter adapter;

    // UI References
    private TextView textEfficiency, textBehindSchedule, textHoursToday;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_work_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(WorkViewModel.class);

        setupViews(view);
        observeData();
    }

    private void setupViews(View view) {
        // Summary Cards
        textEfficiency = view.findViewById(R.id.textEfficiency);
        textBehindSchedule = view.findViewById(R.id.textBehindSchedule);
        textHoursToday = view.findViewById(R.id.textHoursToday);

        // RecyclerView
        RecyclerView recycler = view.findViewById(R.id.recyclerTasks);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new JobTaskAdapter(this);
        recycler.setAdapter(adapter);

        // FAB
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> showAddTaskDialog());
    }

    private void observeData() {
        viewModel.getActiveTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
        });

        viewModel.getHoursLoggedToday().observe(getViewLifecycleOwner(), hours -> {
            textHoursToday.setText(String.format("%.1fh", hours));
        });

        viewModel.getOverallEfficiency().observe(getViewLifecycleOwner(), status -> {
            textEfficiency.setText(status);
        });

        viewModel.getTotalBehindSchedule().observe(getViewLifecycleOwner(), hours -> {
            if (hours > 0) {
                textBehindSchedule.setText(String.format("%.1fh Behind", hours));
                textBehindSchedule.setTextColor(getResources().getColor(R.color.debit_red, null)); // Re-using red
            } else {
                textBehindSchedule.setText("On Schedule");
                textBehindSchedule.setTextColor(getResources().getColor(R.color.credit_green, null)); // Re-using green
            }
        });
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_job_task, null);
        TextInputEditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        TextInputEditText inputHours = dialogView.findViewById(R.id.inputHours);
        // Date picker logic simplified for brevity, assume explicit button click opens
        // Material Date Picker

        final long[] selectedDeadline = { System.currentTimeMillis() + 86400000 }; // Default +1 day

        dialogView.findViewById(R.id.btnSelectDate).setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
            picker.addOnPositiveButtonClickListener(selection -> selectedDeadline[0] = selection);
            picker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = inputTitle.getText().toString();
                    String hoursStr = inputHours.getText().toString();
                    if (!title.isEmpty() && !hoursStr.isEmpty()) {
                        double hours = Double.parseDouble(hoursStr);
                        JobTask task = new JobTask(title, "", selectedDeadline[0], hours, "MEDIUM"); // Default Medium
                        // Can read Priority RadioGroup here if needed
                        viewModel.addTask(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onLogTimeClicked(JobTask task) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_log_hours, null);
        TextInputEditText inputLogHours = dialogView.findViewById(R.id.inputLogHours);
        TextInputEditText inputNotes = dialogView.findViewById(R.id.inputNotes);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Time for: " + task.getTitle())
                .setView(dialogView)
                .setPositiveButton("Log", (dialog, which) -> {
                    String hoursStr = inputLogHours.getText().toString();
                    String notes = inputNotes.getText().toString();
                    if (!hoursStr.isEmpty()) {
                        double hours = Double.parseDouble(hoursStr);
                        viewModel.logHours(task.getId(), hours, notes);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
