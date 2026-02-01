package com.dhanrakshak.presentation.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.databinding.FragmentPlannerBinding;
import com.google.android.material.tabs.TabLayout;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Planner Fragment - Integrated view for Goals, Notes, and Reminders.
 * Provides a unified time management experience within the finance app.
 */
@AndroidEntryPoint
public class PlannerFragment extends Fragment {

    private FragmentPlannerBinding binding;
    private PlannerViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentPlannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlannerViewModel.class);

        setupTabs();
        setupRecyclerView();
        setupFab();
        observeData();
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        viewModel.loadGoals();
                        break;
                    case 1:
                        viewModel.loadReminders();
                        break;
                    case 2:
                        viewModel.loadTodayTasks();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            int selectedTab = binding.tabLayout.getSelectedTabPosition();
            switch (selectedTab) {
                case 0:
                    showAddGoalDialog();
                    break;
                case 1:
                    showAddReminderDialog();
                    break;
                case 2:
                    showAddTaskDialog();
                    break;
            }
        });
    }

    private void observeData() {
        viewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            if (binding.tabLayout.getSelectedTabPosition() == 0) {
                // Update adapter with goals
                binding.emptyView.setVisibility(goals.isEmpty() ? View.VISIBLE : View.GONE);
                binding.recyclerView.setVisibility(goals.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getReminders().observe(getViewLifecycleOwner(), reminders -> {
            if (binding.tabLayout.getSelectedTabPosition() == 1) {
                binding.emptyView.setVisibility(reminders.isEmpty() ? View.VISIBLE : View.GONE);
                binding.recyclerView.setVisibility(reminders.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Load initial data
        viewModel.loadGoals();
    }

    private void showAddGoalDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_goal, null);
        builder.setView(view);

        com.google.android.material.textfield.TextInputEditText etName = view.findViewById(R.id.etName);
        com.google.android.material.textfield.TextInputEditText etAmount = view.findViewById(R.id.etAmount);
        com.google.android.material.button.MaterialButton btnDate = view.findViewById(R.id.btnDate);
        com.google.android.material.chip.ChipGroup chipGroupCategory = view.findViewById(R.id.chipGroupCategory);

        final long[] selectedDate = { System.currentTimeMillis() };

        btnDate.setOnClickListener(v -> {
            java.util.Calendar constraints = java.util.Calendar.getInstance();
            constraints.set(java.util.Calendar.HOUR_OF_DAY, 0);

            com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                    .datePicker()
                    .setTitleText("Select Target Date")
                    .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate[0] = selection;
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy",
                        java.util.Locale.getDefault());
                btnDate.setText(sdf.format(new java.util.Date(selection)));
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString();
            String amountStr = etAmount.getText().toString();

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String category = "CUSTOM";

            int checkedId = chipGroupCategory.getCheckedChipId();
            if (checkedId != -1) {
                com.google.android.material.chip.Chip chip = view.findViewById(checkedId);
                category = chip.getText().toString().toUpperCase();
            }

            com.dhanrakshak.data.local.entity.FinancialGoal goal = new com.dhanrakshak.data.local.entity.FinancialGoal(
                    name, category, amount, selectedDate[0]);

            viewModel.addGoal(goal);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddReminderDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_reminder, null);
        builder.setView(view);

        com.google.android.material.textfield.TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        com.google.android.material.textfield.TextInputEditText etDesc = view.findViewById(R.id.etDesc);
        com.google.android.material.button.MaterialButton btnDate = view.findViewById(R.id.btnDate);
        com.google.android.material.button.MaterialButton btnTime = view.findViewById(R.id.btnTime);
        com.google.android.material.chip.ChipGroup chipGroup = view.findViewById(R.id.chipGroupFrequency);

        final java.util.Calendar calendar = java.util.Calendar.getInstance();

        btnDate.setOnClickListener(v -> {
            com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                    .datePicker()
                    .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                java.util.Calendar selectedCal = java.util.Calendar.getInstance();
                selectedCal.setTimeInMillis(selection);
                calendar.set(java.util.Calendar.YEAR, selectedCal.get(java.util.Calendar.YEAR));
                calendar.set(java.util.Calendar.MONTH, selectedCal.get(java.util.Calendar.MONTH));
                calendar.set(java.util.Calendar.DAY_OF_MONTH, selectedCal.get(java.util.Calendar.DAY_OF_MONTH));

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM",
                        java.util.Locale.getDefault());
                btnDate.setText(sdf.format(new java.util.Date(calendar.getTimeInMillis())));
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        btnTime.setOnClickListener(v -> {
            com.google.android.material.timepicker.MaterialTimePicker timePicker = new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                    .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                    .setHour(calendar.get(java.util.Calendar.HOUR_OF_DAY))
                    .setMinute(calendar.get(java.util.Calendar.MINUTE))
                    .build();

            timePicker.addOnPositiveButtonClickListener(view1 -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(java.util.Calendar.MINUTE, timePicker.getMinute());

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a",
                        java.util.Locale.getDefault());
                btnTime.setText(sdf.format(new java.util.Date(calendar.getTimeInMillis())));
            });

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String desc = etDesc.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            String frequency = "ONCE";
            int checkedId = chipGroup.getCheckedChipId();
            if (checkedId != -1) {
                com.google.android.material.chip.Chip chip = view.findViewById(checkedId);
                frequency = chip.getText().toString().toUpperCase();
            }

            com.dhanrakshak.data.local.entity.Reminder reminder = new com.dhanrakshak.data.local.entity.Reminder(title,
                    "CUSTOM", calendar.getTimeInMillis(), frequency);
            reminder.setDescription(desc);

            viewModel.addReminder(reminder);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddTaskDialog() {
        // Reuse reminder dialog but set default to TODAY
        showAddReminderDialog();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
