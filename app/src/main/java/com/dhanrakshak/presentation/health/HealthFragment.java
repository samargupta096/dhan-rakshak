package com.dhanrakshak.presentation.health;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.InsurancePolicy;
import com.dhanrakshak.databinding.FragmentHealthBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HealthFragment extends Fragment {

    private FragmentHealthBinding binding;
    private HealthViewModel viewModel;
    private PoliciesAdapter policyAdapter;
    private HealthMetricsAdapter metricsAdapter;
    private LabReportsAdapter reportsAdapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private androidx.activity.result.ActivityResultLauncher<String> documentPickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHealthBinding.inflate(inflater, container, false);

        documentPickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        saveSelectedFile(uri);
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HealthViewModel.class);

        setupRecyclerViews();
        setupObservers();

        binding.fabAddPolicy.setOnClickListener(v -> showAddPolicyDialog());
        binding.btnAddVital.setOnClickListener(v -> showAddVitalDialog());
        binding.btnAddReport.setOnClickListener(v -> documentPickerLauncher.launch("application/pdf"));
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void saveSelectedFile(android.net.Uri sourceUri) {
        try {
            // creating a file in internal storage
            String filename = "report_" + System.currentTimeMillis() + ".pdf";
            java.io.File destFile = new java.io.File(requireContext().getFilesDir(), "reports");
            if (!destFile.exists())
                destFile.mkdirs();
            java.io.File file = new java.io.File(destFile, filename);

            // Copy
            java.io.InputStream is = requireContext().getContentResolver().openInputStream(sourceUri);
            java.io.OutputStream os = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();

            // Save to DB
            com.dhanrakshak.data.local.entity.LabReport report = new com.dhanrakshak.data.local.entity.LabReport(
                    "New Report", file.getAbsolutePath(), "PDF", System.currentTimeMillis());
            viewModel.addLabReport(report);
            Toast.makeText(requireContext(), "Report Saved", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReport(com.dhanrakshak.data.local.entity.LabReport report) {
        try {
            java.io.File file = new java.io.File(report.getFilePath());
            if (file.exists()) {
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        requireContext(), requireContext().getPackageName() + ".fileprovider", file);
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerViews() {
        policyAdapter = new PoliciesAdapter();
        binding.recyclerPolicies.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPolicies.setAdapter(policyAdapter);

        metricsAdapter = new HealthMetricsAdapter();
        binding.recyclerVitals
                .setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerVitals.setAdapter(metricsAdapter);

        reportsAdapter = new LabReportsAdapter(this::openReport);
        binding.recyclerReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerReports.setAdapter(reportsAdapter);
    }

    // ... setupObservers ...

    // Adapter for Lab Reports
    private class LabReportsAdapter extends RecyclerView.Adapter<LabReportsAdapter.ReportViewHolder> {
        private List<com.dhanrakshak.data.local.entity.LabReport> reports = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        private final Consumer<com.dhanrakshak.data.local.entity.LabReport> onClick;

        interface Consumer<T> {
            void accept(T t);
        }

        LabReportsAdapter(Consumer<com.dhanrakshak.data.local.entity.LabReport> onClick) {
            this.onClick = onClick;
        }

        void setReports(List<com.dhanrakshak.data.local.entity.LabReport> reports) {
            this.reports = reports;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lab_report, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            com.dhanrakshak.data.local.entity.LabReport report = reports.get(position);
            holder.textTitle.setText(report.getTitle());
            String meta = (report.getDoctorName() != null ? report.getDoctorName() + " • " : "") +
                    dateFormat.format(new Date(report.getTimestamp()));
            holder.textMeta.setText(meta);
            holder.itemView.setOnClickListener(v -> onClick.accept(report));
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView textTitle, textMeta;

            ReportViewHolder(View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(R.id.textReportTitle);
                textMeta = itemView.findViewById(R.id.textReportMeta);
            }
        }
    }

    private void setupObservers() {
        viewModel.getTotalMedicalExpense().observe(getViewLifecycleOwner(), amount -> {
            binding.textMedicalExpense.setText(currencyFormat.format(amount));
            // Simple logic for insight message
            if (amount > 5000) {
                binding.textHealthInsight.setText("⚠️ High medical spend this month.");
                binding.textHealthInsight.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            } else {
                binding.textHealthInsight.setText("✅ Medical expenses are within control.");
                binding.textHealthInsight.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        });

        viewModel.getTotalHealthCover().observe(getViewLifecycleOwner(), amount -> {
            binding.textTotalCover.setText(currencyFormat.format(amount));
        });

        viewModel.getTotalYearlyPremium().observe(getViewLifecycleOwner(), amount -> {
            binding.textTotalPremium.setText(currencyFormat.format(amount));
        });

        viewModel.getPolicies().observe(getViewLifecycleOwner(), policies -> {
            policyAdapter.setPolicies(policies);
        });

        viewModel.getHealthMetrics().observe(getViewLifecycleOwner(), metrics -> {
            metricsAdapter.setMetrics(metrics);
        });

        viewModel.getLabReports().observe(getViewLifecycleOwner(), reports -> {
            reportsAdapter.setReports(reports);
        });

        // Observer for Daily Health Data
        viewModel.getDailyHealthData().observe(getViewLifecycleOwner(), data -> {
            if (data == null)
                return;

            Double steps = data.getOrDefault("STEPS", 0.0);
            Double calories = data.getOrDefault("CALORIES", 0.0);
            Double sleep = data.getOrDefault("SLEEP", 0.0);

            binding.tvSteps.setText(String.format(Locale.US, "%.0f", steps));
            binding.tvCalories.setText(String.format(Locale.US, "%.0f", calories));
            binding.tvSleep.setText(String.format(Locale.US, "%.1f", sleep));

            // Default progress if max unknown, or update when goal is loaded
            binding.progressSteps.setProgress((int) (steps / 100)); // Rough %
            binding.progressCalories.setProgress((int) (calories / 30));
            binding.progressSleep.setProgress((int) (sleep * 10)); // 10hrs = 100%
        });

        // Observer for Health Goals
        viewModel.getHealthGoals().observe(getViewLifecycleOwner(), goals -> {
            for (com.dhanrakshak.data.local.entity.HealthGoal goal : goals) {
                if ("STEPS".equals(goal.metricType)) {
                    binding.tvStepsGoal.setText("Goal: " + (int) goal.targetValue);

                    double currentSteps = Double.parseDouble(binding.tvSteps.getText().toString().equals("--") ? "0"
                            : binding.tvSteps.getText().toString());
                    binding.progressSteps.setMax((int) goal.targetValue);
                    binding.progressSteps.setProgress((int) currentSteps);
                } else if ("CALORIES".equals(goal.metricType)) {
                    binding.tvCaloriesGoal.setText("Goal: " + (int) goal.targetValue);

                    double currentCal = Double.parseDouble(binding.tvCalories.getText().toString().equals("--") ? "0"
                            : binding.tvCalories.getText().toString());
                    binding.progressCalories.setMax((int) goal.targetValue);
                    binding.progressCalories.setProgress((int) currentCal);
                } else if ("SLEEP".equals(goal.metricType)) {
                    binding.tvSleepGoal.setText("Goal: " + goal.targetValue + "h");

                    double currentSleep = Double.parseDouble(binding.tvSleep.getText().toString().equals("--") ? "0"
                            : binding.tvSleep.getText().toString());
                    binding.progressSleep.setMax((int) (goal.targetValue * 10)); // scaling for progress bar
                    binding.progressSleep.setProgress((int) (currentSleep * 10));
                }
            }
        });

        binding.cardSteps.setOnClickListener(v -> showSetGoalDialog("STEPS", "Daily Steps Goal"));
        binding.cardCalories.setOnClickListener(v -> showSetGoalDialog("CALORIES", "Daily Calories Goal"));
        binding.cardSleep.setOnClickListener(v -> showSetGoalDialog("SLEEP", "Daily Sleep Goal (Hours)"));
    }

    private void showSetGoalDialog(String type, String title) {
        EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter target value");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Set Goal", (dialog, which) -> {
                    String valStr = input.getText().toString();
                    if (!valStr.isEmpty()) {
                        double val = Double.parseDouble(valStr);
                        com.dhanrakshak.data.local.entity.HealthGoal goal = new com.dhanrakshak.data.local.entity.HealthGoal(
                                type, val, "DAILY", System.currentTimeMillis());
                        viewModel.setHealthGoal(goal);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddVitalDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_vital, null);

        com.google.android.material.textfield.TextInputEditText inputName = dialogView
                .findViewById(R.id.inputMetricName);
        com.google.android.material.textfield.TextInputEditText inputValue = dialogView
                .findViewById(R.id.inputMetricValue);
        com.google.android.material.textfield.TextInputEditText inputUnit = dialogView
                .findViewById(R.id.inputMetricUnit);
        com.google.android.material.textfield.TextInputEditText inputNotes = dialogView
                .findViewById(R.id.inputMetricNotes);
        TextView textDate = dialogView.findViewById(R.id.textMetricDate);

        final Calendar calendar = Calendar.getInstance();
        textDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                textDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Vital Log")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        String name = inputName.getText().toString();
                        double value = Double.parseDouble(inputValue.getText().toString());
                        String unit = inputUnit.getText().toString();
                        String notes = inputNotes.getText().toString();

                        com.dhanrakshak.data.local.entity.HealthMetric metric = new com.dhanrakshak.data.local.entity.HealthMetric(
                                name, value, unit, calendar.getTimeInMillis());
                        metric.setNotes(notes);

                        viewModel.addHealthMetric(metric);
                        Toast.makeText(requireContext(), "Vital Logged", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ... (Keep showAddPolicyDialog)

    // Adapter for Vitals
    private class HealthMetricsAdapter extends RecyclerView.Adapter<HealthMetricsAdapter.MetricViewHolder> {
        private List<com.dhanrakshak.data.local.entity.HealthMetric> metrics = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        void setMetrics(List<com.dhanrakshak.data.local.entity.HealthMetric> metrics) {
            this.metrics = metrics;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MetricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_metric, parent, false);
            return new MetricViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MetricViewHolder holder, int position) {
            com.dhanrakshak.data.local.entity.HealthMetric metric = metrics.get(position);
            holder.textName.setText(metric.getMetricName());
            holder.textValue.setText(String.valueOf(metric.getValue()));
            holder.textUnit.setText(metric.getUnit());
            holder.textDate.setText(dateFormat.format(new Date(metric.getTimestamp())));
        }

        @Override
        public int getItemCount() {
            return metrics.size();
        }

        class MetricViewHolder extends RecyclerView.ViewHolder {
            TextView textName, textValue, textUnit, textDate;

            MetricViewHolder(View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textMetricName);
                textValue = itemView.findViewById(R.id.textMetricValue);
                textUnit = itemView.findViewById(R.id.textMetricUnit);
                textDate = itemView.findViewById(R.id.textMetricDate);
            }
        }
    }

    // ... (Keep showAddPolicyDialog and PoliciesAdapter)

    private void showAddPolicyDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_policy, null);

        // This layout doesn't exist yet, we'll create it dynamically or use a simple
        // programmatic approach if needed.
        // For better UX, let's create the layout file in the next step, but here I'll
        // assume standard IDs.
        // Or simpler: Build standard View programmatically to avoid another file if
        // lazy, but file is better.
        // Let's create the layout file 'dialog_add_policy.xml' after this.

        EditText inputName = dialogView.findViewById(R.id.inputPolicyName);
        EditText inputProvider = dialogView.findViewById(R.id.inputProvider);
        EditText inputNumber = dialogView.findViewById(R.id.inputPolicyNumber);
        EditText inputSum = dialogView.findViewById(R.id.inputSumInsured);
        EditText inputPremium = dialogView.findViewById(R.id.inputPremium);
        EditText inputAbha = dialogView.findViewById(R.id.inputAbha);
        TextView textRenewal = dialogView.findViewById(R.id.textRenewalDate);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[] { "HEALTH", "LIFE", "VEHICLE", "OTHER" });
        spinnerCategory.setAdapter(adapter);

        // Setup Date Picker
        final Calendar calendar = Calendar.getInstance();
        textRenewal.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                textRenewal.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Insurance Policy")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        String name = inputName.getText().toString();
                        String provider = inputProvider.getText().toString();
                        String number = inputNumber.getText().toString();
                        double sum = Double.parseDouble(inputSum.getText().toString());
                        double premium = Double.parseDouble(inputPremium.getText().toString());
                        String abha = inputAbha.getText().toString();
                        String category = spinnerCategory.getSelectedItem().toString();

                        InsurancePolicy policy = new InsurancePolicy();
                        policy.setPolicyName(name);
                        policy.setProvider(provider);
                        policy.setPolicyNumber(number);
                        policy.setSumInsured(sum);
                        policy.setPremiumAmount(premium);
                        policy.setCategory(category);
                        policy.setRenewalDate(calendar.getTimeInMillis());
                        policy.setAbhaId(abha);
                        policy.setPremiumFrequency("ANNUALLY"); // Defaulting for now

                        viewModel.addPolicy(policy);
                        Toast.makeText(requireContext(), "Policy Added", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Inner Adapter Class
    private class PoliciesAdapter extends RecyclerView.Adapter<PoliciesAdapter.PolicyViewHolder> {
        private List<InsurancePolicy> policies = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        void setPolicies(List<InsurancePolicy> policies) {
            this.policies = policies;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PolicyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_insurance_policy, parent, false);
            return new PolicyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PolicyViewHolder holder, int position) {
            InsurancePolicy policy = policies.get(position);
            holder.textName.setText(policy.getPolicyName());
            holder.textProvider.setText(policy.getProvider());
            holder.textNumber.setText("Pol: " + policy.getPolicyNumber());
            holder.textPremium.setText(currencyFormat.format(policy.getPremiumAmount()));
            holder.textCover.setText("Cover: " + currencyFormat.format(policy.getSumInsured()));
            holder.textRenewal.setText(dateFormat.format(new Date(policy.getRenewalDate())));

            // ABHA Badge
            if (policy.getAbhaId() != null && !policy.getAbhaId().isEmpty()) {
                holder.chipAbha.setVisibility(View.VISIBLE);
                holder.chipAbha.setText("ABHA: " + policy.getAbhaId());
            } else {
                holder.chipAbha.setVisibility(View.GONE);
            }

            // Icon logic based on category
            String icon = "📄";
            if ("HEALTH".equalsIgnoreCase(policy.getCategory()))
                icon = "🏥";
            else if ("LIFE".equalsIgnoreCase(policy.getCategory()))
                icon = "❤️";
            else if ("VEHICLE".equalsIgnoreCase(policy.getCategory()))
                icon = "🚗";
            holder.textIcon.setText(icon);
        }

        @Override
        public int getItemCount() {
            return policies.size();
        }

        class PolicyViewHolder extends RecyclerView.ViewHolder {
            TextView textName, textProvider, textNumber, textPremium, textCover, textRenewal, textIcon;
            com.google.android.material.chip.Chip chipAbha;

            PolicyViewHolder(View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textPolicyName);
                textProvider = itemView.findViewById(R.id.textProvider);
                textNumber = itemView.findViewById(R.id.textPolicyNumber);
                textPremium = itemView.findViewById(R.id.textPremium);
                textCover = itemView.findViewById(R.id.textCoverAmount);
                textRenewal = itemView.findViewById(R.id.textRenewalDate);
                textIcon = itemView.findViewById(R.id.textCategoryIcon);
                chipAbha = itemView.findViewById(R.id.chipAbha);
            }
        }
    }
}
