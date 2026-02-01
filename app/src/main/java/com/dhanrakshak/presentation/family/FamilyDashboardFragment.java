package com.dhanrakshak.presentation.family;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dhanrakshak.databinding.FragmentFamilyDashboardBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Family Dashboard Fragment.
 * Displays combined net worth and asset allocation of the family.
 */
@AndroidEntryPoint
public class FamilyDashboardFragment extends Fragment {

    private FragmentFamilyDashboardBinding binding;
    private FamilyDashboardViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentFamilyDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FamilyDashboardViewModel.class);

        setupUI();
        observeData();
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void observeData() {
        viewModel.getTotalFamilyNetWorth().observe(getViewLifecycleOwner(), netWorth -> {
            binding.tvTotalNetWorth.setText(String.format("₹ %.2f", netWorth));
        });

        viewModel.getMemberContributions().observe(getViewLifecycleOwner(), contributions -> {
            // TODO: Bind to a chart or list
            // For now, we just rely on the existing member list in FamilyFragment
        });

        viewModel.loadDashboardData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
