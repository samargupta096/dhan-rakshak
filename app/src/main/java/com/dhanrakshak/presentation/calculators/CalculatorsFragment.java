package com.dhanrakshak.presentation.calculators;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dhanrakshak.R;
import com.dhanrakshak.databinding.FragmentCalculatorsBinding;
import com.dhanrakshak.databinding.LayoutCalculatorTabBinding;
import com.dhanrakshak.util.CalculatorUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class CalculatorsFragment extends Fragment {

    private FragmentCalculatorsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCalculatorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        setupTabs();
        loadCalculator(0); // Default to SIP
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadCalculator(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadCalculator(int type) {
        binding.calculatorContainer.removeAllViews();
        LayoutCalculatorTabBinding tabBinding = LayoutCalculatorTabBinding.inflate(getLayoutInflater(),
                binding.calculatorContainer, true);

        // Customize UI based on type
        TextInputLayout amountInputInfo = (TextInputLayout) tabBinding.etAmount.getParent().getParent();

        switch (type) {
            case 0: // SIP
                amountInputInfo.setHint("Monthly Investment (₹)");
                tabBinding.etRate.setText("12"); // Default SIP rate
                break;
            case 1: // FD
                amountInputInfo.setHint("Principal Amount (₹)");
                tabBinding.etRate.setText("6.5"); // Default FD rate
                break;
            case 2: // RD
                amountInputInfo.setHint("Monthly Deposit (₹)");
                tabBinding.etRate.setText("6.0"); // Default RD rate
                break;
        }

        tabBinding.btnCalculate.setOnClickListener(v -> {
            String amountStr = tabBinding.etAmount.getText().toString();
            String rateStr = tabBinding.etRate.getText().toString();
            String yearsStr = tabBinding.etYears.getText().toString();

            if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(rateStr) || TextUtils.isEmpty(yearsStr)) {
                return;
            }

            double amount = Double.parseDouble(amountStr);
            double rate = Double.parseDouble(rateStr);
            double years = Double.parseDouble(yearsStr);

            double maturityAmount = 0;
            double investedAmount = 0;

            if (type == 0) { // SIP
                maturityAmount = CalculatorUtils.calculateSIP(amount, rate, years);
                investedAmount = amount * years * 12;
            } else if (type == 1) { // FD
                maturityAmount = CalculatorUtils.calculateFD(amount, rate, years);
                investedAmount = amount;
            } else if (type == 2) { // RD
                maturityAmount = CalculatorUtils.calculateRD(amount, rate, years);
                investedAmount = amount * years * 12;
            }

            double returns = maturityAmount - investedAmount;

            tabBinding.cardResult.setVisibility(View.VISIBLE);

            // Format currency
            java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

            tabBinding.tvInvested.setText(nf.format(investedAmount));
            tabBinding.tvReturns.setText(nf.format(returns));
            tabBinding.tvTotal.setText(nf.format(maturityAmount));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
