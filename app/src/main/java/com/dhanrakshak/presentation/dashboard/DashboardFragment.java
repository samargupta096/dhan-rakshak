package com.dhanrakshak.presentation.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dhanrakshak.R;
import com.dhanrakshak.databinding.FragmentDashboardBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Dashboard Fragment showing net worth, asset allocation, and recent
 * transactions.
 */
@AndroidEntryPoint
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private RecentTransactionsAdapter transactionsAdapter;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupViews();
        setupFabListeners(view);
        observeData();
    }

    @Inject
    com.dhanrakshak.core.FeatureManager featureManager;

    private void setupFabListeners(View view) {
        View fabScanner = view.findViewById(R.id.fabScanReceipt);
        if (fabScanner != null) {
            boolean scannerEnabled = featureManager
                    .isFeatureEnabled(com.dhanrakshak.core.FeatureManager.FEATURE_SCANNER);
            boolean healthEnabled = featureManager.isFeatureEnabled(com.dhanrakshak.core.FeatureManager.FEATURE_HEALTH);

            if (featureManager.isFeatureEnabled(FeatureManager.FEATURE_TRIPS)) {
                binding.cardTrips.setVisibility(View.VISIBLE);
            } else {
                binding.cardTrips.setVisibility(View.GONE);
            }

            if (featureManager.isFeatureEnabled(FeatureManager.FEATURE_WORK)) {
                binding.cardWork.setVisibility(View.VISIBLE);
            } else {
                binding.cardWork.setVisibility(View.GONE);
            }

            if (scannerEnabled) {
                fabScanner.setVisibility(View.VISIBLE);
                binding.cardTrips.setOnClickListener(v -> {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new com.dhanrakshak.presentation.trip.TripListFragment())
                            .addToBackStack(null)
                            .commit();
                });

                binding.cardWork.setOnClickListener(v -> {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,
                                    new com.dhanrakshak.presentation.scanner.ReceiptScannerFragment())
                            .addToBackStack(null)
                            .commit();
                });
            } else {
                fabScanner.setVisibility(View.GONE);
            }

            if (healthEnabled) {
                // If scanner disabled, maybe show health button instead?
                // For now sticking to LongClick as per previous design, but only if enabled.
                fabScanner.setOnLongClickListener(v -> {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new com.dhanrakshak.presentation.health.HealthFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;
                });
            }
        }
    }

    private void setupViews() {
        // Setup RecyclerView for recent transactions
        transactionsAdapter = new RecentTransactionsAdapter();
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTransactions.setAdapter(transactionsAdapter);
        binding.recyclerTransactions.setNestedScrollingEnabled(false);

        // Setup Pie Chart
        setupPieChart();
    }

    private void setupPieChart() {
        binding.pieChartAllocation.setUsePercentValues(true);
        binding.pieChartAllocation.getDescription().setEnabled(false);
        binding.pieChartAllocation.setExtraOffsets(5, 10, 5, 5);
        binding.pieChartAllocation.setDragDecelerationFrictionCoef(0.95f);
        binding.pieChartAllocation.setDrawHoleEnabled(true);
        binding.pieChartAllocation.setHoleColor(Color.TRANSPARENT);
        binding.pieChartAllocation.setTransparentCircleRadius(61f);
        binding.pieChartAllocation.setEntryLabelColor(Color.WHITE);
        binding.pieChartAllocation.setEntryLabelTextSize(10f);
        binding.pieChartAllocation.getLegend().setEnabled(true);
        binding.pieChartAllocation.getLegend().setTextColor(
                requireContext().getResources().getColor(R.color.on_background, null));
    }

    private void observeData() {
        // Observe total net worth
        viewModel.getTotalNetWorth().observe(getViewLifecycleOwner(), netWorth -> {
            if (netWorth != null) {
                binding.textNetWorth.setText(currencyFormat.format(netWorth));
            } else {
                binding.textNetWorth.setText("₹0.00");
            }
        });

        // Observe profit/loss
        viewModel.getTotalProfitLoss().observe(getViewLifecycleOwner(), profitLoss -> {
            if (profitLoss != null) {
                String prefix = profitLoss >= 0 ? "+" : "";
                binding.textProfitLoss.setText(prefix + currencyFormat.format(profitLoss));
                binding.textProfitLoss.setTextColor(getResources().getColor(
                        profitLoss >= 0 ? R.color.credit_green : R.color.debit_red, null));
            }
        });

        // Observe asset allocation
        viewModel.getAssetAllocation().observe(getViewLifecycleOwner(), this::updatePieChart);

        // Observe recent transactions
        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                transactionsAdapter.submitList(transactions);
                binding.emptyTransactions.setVisibility(View.GONE);
                binding.recyclerTransactions.setVisibility(View.VISIBLE);
            } else {
                binding.emptyTransactions.setVisibility(View.VISIBLE);
                binding.recyclerTransactions.setVisibility(View.GONE);
            }
        });
    }

    private void updatePieChart(List<DashboardViewModel.AssetAllocationItem> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            binding.pieChartAllocation.setVisibility(View.GONE);
            return;
        }

        binding.pieChartAllocation.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (DashboardViewModel.AssetAllocationItem item : allocations) {
            if (item.percentage > 0) {
                entries.add(new PieEntry((float) item.percentage, item.assetType));
                colors.add(Color.parseColor(item.color));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.pieChartAllocation));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        binding.pieChartAllocation.setData(data);
        binding.pieChartAllocation.invalidate();
        binding.pieChartAllocation.animateY(1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
