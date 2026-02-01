package com.dhanrakshak.presentation.portfolio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhanrakshak.databinding.DialogAddMutualFundBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Dialog for adding a new mutual fund holding.
 */
public class AddMutualFundDialogFragment extends BottomSheetDialogFragment {

    private DialogAddMutualFundBinding binding;
    private final PortfolioViewModel viewModel;

    public AddMutualFundDialogFragment(PortfolioViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DialogAddMutualFundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAdd.setOnClickListener(v -> {
            String schemeCodeStr = binding.editSchemeCode.getText().toString().trim();
            String schemeName = binding.editSchemeName.getText().toString().trim();
            String unitsStr = binding.editUnits.getText().toString().trim();
            String navStr = binding.editAvgNav.getText().toString().trim();

            if (schemeCodeStr.isEmpty() || schemeName.isEmpty() || unitsStr.isEmpty() || navStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long schemeCode = Long.parseLong(schemeCodeStr);
                double units = Double.parseDouble(unitsStr);
                double avgNav = Double.parseDouble(navStr);

                viewModel.addMutualFund(schemeCode, schemeName, units, avgNav);
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
