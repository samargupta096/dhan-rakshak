package com.dhanrakshak.presentation.insights;

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
import com.dhanrakshak.ai.insights.AiFinanceInsightsEngine;
import com.dhanrakshak.databinding.FragmentInsightsBinding;
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
 * Insights Fragment with AI-powered portfolio analysis.
 */
@AndroidEntryPoint
public class InsightsFragment extends Fragment {

    private FragmentInsightsBinding binding;
    private InsightsViewModel viewModel;
    private SuggestionsAdapter suggestionsAdapter;
    private WhatIfAdapter whatIfAdapter;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InsightsViewModel.class);

        setupViews();
        setupCharts();
        observeData();
    }

    private void setupViews() {
        // Setup suggestions RecyclerView
        suggestionsAdapter = new SuggestionsAdapter();
        binding.recyclerSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSuggestions.setAdapter(suggestionsAdapter);
        binding.recyclerSuggestions.setNestedScrollingEnabled(false);

        // Setup what-if RecyclerView
        whatIfAdapter = new WhatIfAdapter();
        binding.recyclerWhatIf.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerWhatIf.setAdapter(whatIfAdapter);
        binding.recyclerWhatIf.setNestedScrollingEnabled(false);
    }

    private void setupCharts() {
        // Allocation pie chart
        binding.pieChartAllocation.setUsePercentValues(true);
        binding.pieChartAllocation.getDescription().setEnabled(false);
        binding.pieChartAllocation.setDrawHoleEnabled(true);
        binding.pieChartAllocation.setHoleColor(Color.TRANSPARENT);
        binding.pieChartAllocation.setHoleRadius(50f);
        binding.pieChartAllocation.setTransparentCircleRadius(55f);
        binding.pieChartAllocation.getLegend().setEnabled(true);
        binding.pieChartAllocation.getLegend().setTextColor(
                getResources().getColor(R.color.on_background, null));
        binding.pieChartAllocation.setEntryLabelColor(Color.WHITE);
    }

    private void observeData() {
        // Loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.contentLayout.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        // Main insights data
        viewModel.getInsights().observe(getViewLifecycleOwner(), insights -> {
            if (insights != null) {
                updateUI(insights);
            }
        });
    }

    private void updateUI(AiFinanceInsightsEngine.PortfolioInsights insights) {
        // Update allocation chart
        updateAllocationChart(insights.allocationAnalysis);

        // Update risk assessment
        updateRiskAssessment(insights.riskAssessment);

        // Update AI analysis text
        if (insights.aiAnalysis != null) {
            binding.textAiAnalysis.setText(insights.aiAnalysis);
        }

        // Update suggestions
        if (insights.suggestions != null && !insights.suggestions.isEmpty()) {
            suggestionsAdapter.submitList(insights.suggestions);
            binding.cardSuggestions.setVisibility(View.VISIBLE);
        } else {
            binding.cardSuggestions.setVisibility(View.GONE);
        }

        // Update what-if scenarios
        if (insights.whatIfScenarios != null && !insights.whatIfScenarios.isEmpty()) {
            whatIfAdapter.submitList(insights.whatIfScenarios);
            binding.cardWhatIf.setVisibility(View.VISIBLE);
        } else {
            binding.cardWhatIf.setVisibility(View.GONE);
        }

        // Update deviation messages
        if (insights.allocationAnalysis != null &&
                insights.allocationAnalysis.deviationMessages != null &&
                !insights.allocationAnalysis.deviationMessages.isEmpty()) {
            StringBuilder deviations = new StringBuilder();
            for (String msg : insights.allocationAnalysis.deviationMessages) {
                deviations.append("• ").append(msg).append("\n");
            }
            binding.textDeviations.setText(deviations.toString().trim());
            binding.cardDeviations.setVisibility(View.VISIBLE);
        } else {
            binding.cardDeviations.setVisibility(View.GONE);
        }

        // Update Forecast
        if (binding.textAiAnalysis != null && insights.metrics != null && insights.metrics.forecastAnalysis != null) {
            String currentText = binding.textAiAnalysis.getText().toString();
            String newText = currentText + "\n\n" + insights.metrics.forecastAnalysis;
            binding.textAiAnalysis.setText(newText);
        }
    }

    private void updateAllocationChart(AiFinanceInsightsEngine.AllocationAnalysis analysis) {
        if (analysis == null)
            return;

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (analysis.currentEquity > 0) {
            entries.add(new PieEntry((float) analysis.currentEquity, "Equity"));
            colors.add(Color.parseColor("#2196F3"));
        }
        if (analysis.currentDebt > 0) {
            entries.add(new PieEntry((float) analysis.currentDebt, "Debt"));
            colors.add(Color.parseColor("#4CAF50"));
        }
        if (analysis.currentGold > 0) {
            entries.add(new PieEntry((float) analysis.currentGold, "Gold"));
            colors.add(Color.parseColor("#FFC107"));
        }
        if (analysis.currentCash > 0) {
            entries.add(new PieEntry((float) analysis.currentCash, "Cash"));
            colors.add(Color.parseColor("#607D8B"));
        }

        if (entries.isEmpty()) {
            binding.pieChartAllocation.setVisibility(View.GONE);
            return;
        }

        binding.pieChartAllocation.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.pieChartAllocation));

        binding.pieChartAllocation.setData(data);
        binding.pieChartAllocation.invalidate();
        binding.pieChartAllocation.animateY(1000);
    }

    private void updateRiskAssessment(AiFinanceInsightsEngine.RiskAssessment risk) {
        if (risk == null)
            return;

        // Risk level
        binding.textRiskLevel.setText(risk.riskLevel);
        binding.textRiskDescription.setText(risk.riskDescription);

        // Risk score progress
        binding.progressRisk.setProgress((int) (risk.riskScore * 10));

        // Color based on risk level
        int riskColor;
        if (risk.riskScore <= 3) {
            riskColor = Color.parseColor("#4CAF50"); // Green
        } else if (risk.riskScore <= 6) {
            riskColor = Color.parseColor("#FFC107"); // Yellow
        } else {
            riskColor = Color.parseColor("#F44336"); // Red
        }
        binding.textRiskLevel.setTextColor(riskColor);

        // Diversification
        binding.textDiversification.setText(risk.diversificationLevel);
        binding.progressDiversification.setProgress((int) (risk.diversificationScore * 10));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
