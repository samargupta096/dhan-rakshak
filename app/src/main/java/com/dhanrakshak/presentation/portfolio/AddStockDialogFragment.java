package com.dhanrakshak.presentation.portfolio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhanrakshak.databinding.DialogAddStockBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Dialog for adding a new stock holding.
 */
public class AddStockDialogFragment extends BottomSheetDialogFragment {

    private DialogAddStockBinding binding;
    private final PortfolioViewModel viewModel;

    public AddStockDialogFragment(PortfolioViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DialogAddStockBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAdd.setOnClickListener(v -> {
            String symbol = binding.editSymbol.getText().toString().trim().toUpperCase();
            String name = binding.editName.getText().toString().trim();
            String qtyStr = binding.editQuantity.getText().toString().trim();
            String priceStr = binding.editAvgPrice.getText().toString().trim();

            if (symbol.isEmpty() || name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantity = Double.parseDouble(qtyStr);
                double avgPrice = Double.parseDouble(priceStr);

                viewModel.addStock(symbol, name, quantity, avgPrice);
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
