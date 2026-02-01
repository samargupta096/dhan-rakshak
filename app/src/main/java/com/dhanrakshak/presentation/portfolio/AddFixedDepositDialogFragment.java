package com.dhanrakshak.presentation.portfolio;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhanrakshak.databinding.DialogAddFixedDepositBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Dialog for adding a new Fixed Deposit.
 */
public class AddFixedDepositDialogFragment extends BottomSheetDialogFragment {

    private DialogAddFixedDepositBinding binding;
    private final PortfolioViewModel viewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private long startDate = System.currentTimeMillis();
    private long maturityDate = 0;

    private static final String[] BANKS = {
            "HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Kotak Mahindra Bank",
            "Standard Chartered", "Yes Bank", "IDFC First Bank", "Post Office"
    };

    private static final String[] COMPOUNDING = { "Quarterly", "Monthly", "Yearly" };

    public AddFixedDepositDialogFragment(PortfolioViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DialogAddFixedDepositBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup dropdowns
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, BANKS);
        binding.autoBankName.setAdapter(bankAdapter);

        ArrayAdapter<String> compoundAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, COMPOUNDING);
        binding.autoCompounding.setAdapter(compoundAdapter);
        binding.autoCompounding.setText("Quarterly", false);

        // Set default start date
        binding.editStartDate.setText(dateFormat.format(startDate));

        // Date pickers
        binding.editStartDate.setOnClickListener(v -> showDatePicker(true));
        binding.editMaturityDate.setOnClickListener(v -> showDatePicker(false));

        binding.btnAdd.setOnClickListener(v -> addFixedDeposit());
        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    if (isStartDate) {
                        startDate = selected.getTimeInMillis();
                        binding.editStartDate.setText(dateFormat.format(startDate));
                    } else {
                        maturityDate = selected.getTimeInMillis();
                        binding.editMaturityDate.setText(dateFormat.format(maturityDate));
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void addFixedDeposit() {
        String bankName = binding.autoBankName.getText().toString().trim();
        String principalStr = binding.editPrincipal.getText().toString().trim();
        String rateStr = binding.editInterestRate.getText().toString().trim();
        String tenureStr = binding.editTenure.getText().toString().trim();
        String compounding = binding.autoCompounding.getText().toString().trim();

        if (bankName.isEmpty() || principalStr.isEmpty() || rateStr.isEmpty() ||
                tenureStr.isEmpty() || maturityDate == 0) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double principal = Double.parseDouble(principalStr);
            double rate = Double.parseDouble(rateStr);
            int tenure = Integer.parseInt(tenureStr);

            viewModel.addFixedDeposit(bankName, principal, rate, tenure,
                    compounding, startDate, maturityDate);
            dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
