package com.dhanrakshak.presentation.portfolio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhanrakshak.databinding.DialogAddBankAccountBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Dialog for adding a new bank account.
 */
public class AddBankAccountDialogFragment extends BottomSheetDialogFragment {

    private DialogAddBankAccountBinding binding;
    private final PortfolioViewModel viewModel;

    private static final String[] BANKS = {
            "HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Kotak Mahindra Bank",
            "Standard Chartered", "Yes Bank", "IDFC First Bank", "IndusInd Bank",
            "Punjab National Bank", "Bank of Baroda", "Canara Bank", "Union Bank"
    };

    private static final String[] ACCOUNT_TYPES = { "Savings", "Current", "Salary" };

    public AddBankAccountDialogFragment(PortfolioViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DialogAddBankAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup bank dropdown
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, BANKS);
        binding.autoBankName.setAdapter(bankAdapter);

        // Setup account type dropdown
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, ACCOUNT_TYPES);
        binding.autoAccountType.setAdapter(typeAdapter);

        binding.btnAdd.setOnClickListener(v -> {
            String bankName = binding.autoBankName.getText().toString().trim();
            String accountType = binding.autoAccountType.getText().toString().trim();
            String lastFour = binding.editLastFour.getText().toString().trim();

            if (bankName.isEmpty() || accountType.isEmpty() || lastFour.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lastFour.length() != 4) {
                Toast.makeText(requireContext(), "Enter last 4 digits only", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.addBankAccount(bankName, accountType, lastFour);
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
