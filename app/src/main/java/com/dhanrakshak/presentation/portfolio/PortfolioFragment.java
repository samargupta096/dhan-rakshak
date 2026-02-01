package com.dhanrakshak.presentation.portfolio;

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

import com.dhanrakshak.R;
import com.dhanrakshak.databinding.FragmentPortfolioBinding;
import com.google.android.material.tabs.TabLayout;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Portfolio Fragment with tabs for different asset types.
 */
@AndroidEntryPoint
public class PortfolioFragment extends Fragment {

    private FragmentPortfolioBinding binding;
    private PortfolioViewModel viewModel;

    private StockAdapter stockAdapter;
    private MutualFundAdapter mfAdapter;
    private BankAccountAdapter bankAdapter;
    private FixedDepositAdapter fdAdapter;
    private RecurringDepositAdapter rdAdapter;

    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentPortfolioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PortfolioViewModel.class);

        setupTabs();
        setupAdapters();
        setupClickListeners();
        observeData();
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Stocks"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Mutual Funds"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Banks"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("FD"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("RD"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateRecyclerView();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupAdapters() {
        stockAdapter = new StockAdapter(asset -> {
            // Handle stock click - show details/edit
        }, asset -> {
            viewModel.deleteStock(asset);
        });

        mfAdapter = new MutualFundAdapter(asset -> {
            // Handle MF click
        }, asset -> {
            viewModel.deleteMutualFund(asset);
        });

        bankAdapter = new BankAccountAdapter(account -> {
            // Handle bank account click
        }, account -> {
            viewModel.deleteBankAccount(account);
        });

        fdAdapter = new FixedDepositAdapter(fd -> {
            // Handle FD click
        }, fd -> {
            viewModel.deleteFixedDeposit(fd);
        });

        rdAdapter = new RecurringDepositAdapter(rd -> {
            // Handle RD click
        }, rd -> {
            viewModel.deleteRecurringDeposit(rd);
        });

        binding.recyclerAssets.setLayoutManager(new LinearLayoutManager(requireContext()));
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        switch (currentTab) {
            case 0:
                binding.recyclerAssets.setAdapter(stockAdapter);
                binding.textEmptyTitle.setText(R.string.no_stocks);
                break;
            case 1:
                binding.recyclerAssets.setAdapter(mfAdapter);
                binding.textEmptyTitle.setText(R.string.no_mutual_funds);
                break;
            case 2:
                binding.recyclerAssets.setAdapter(bankAdapter);
                binding.textEmptyTitle.setText(R.string.no_bank_accounts);
                break;
            case 3:
                binding.recyclerAssets.setAdapter(fdAdapter);
                binding.textEmptyTitle.setText(R.string.no_fixed_deposits);
                break;
            case 4:
                binding.recyclerAssets.setAdapter(rdAdapter);
                binding.textEmptyTitle.setText(R.string.no_recurring_deposits);
                break;
        }
    }

    private void setupClickListeners() {
        binding.fabAdd.setOnClickListener(v -> showAddDialog());

        binding.btnRefresh.setOnClickListener(v -> {
            switch (currentTab) {
                case 0:
                    viewModel.refreshStockPrices();
                    break;
                case 1:
                    viewModel.syncMutualFundNav();
                    break;
            }
        });
    }

    private void showAddDialog() {
        switch (currentTab) {
            case 0:
                new AddStockDialogFragment(viewModel).show(
                        getChildFragmentManager(), "add_stock");
                break;
            case 1:
                new AddMutualFundDialogFragment(viewModel).show(
                        getChildFragmentManager(), "add_mf");
                break;
            case 2:
                new AddBankAccountDialogFragment(viewModel).show(
                        getChildFragmentManager(), "add_bank");
                break;
            case 3:
                new AddFixedDepositDialogFragment(viewModel).show(
                        getChildFragmentManager(), "add_fd");
                break;
            case 4:
                new AddRecurringDepositDialogFragment(viewModel).show(
                        getChildFragmentManager(), "add_rd");
                break;
        }
    }

    private void observeData() {
        viewModel.getStocks().observe(getViewLifecycleOwner(), stocks -> {
            stockAdapter.submitList(stocks);
            updateEmptyState(currentTab == 0 && (stocks == null || stocks.isEmpty()));
        });

        viewModel.getMutualFunds().observe(getViewLifecycleOwner(), mfs -> {
            mfAdapter.submitList(mfs);
            updateEmptyState(currentTab == 1 && (mfs == null || mfs.isEmpty()));
        });

        viewModel.getBankAccounts().observe(getViewLifecycleOwner(), accounts -> {
            bankAdapter.submitList(accounts);
            updateEmptyState(currentTab == 2 && (accounts == null || accounts.isEmpty()));
        });

        viewModel.getFixedDeposits().observe(getViewLifecycleOwner(), fds -> {
            fdAdapter.submitList(fds);
            updateEmptyState(currentTab == 3 && (fds == null || fds.isEmpty()));
        });

        viewModel.getRecurringDeposits().observe(getViewLifecycleOwner(), rds -> {
            rdAdapter.submitList(rds);
            updateEmptyState(currentTab == 4 && (rds == null || rds.isEmpty()));
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Operation successful", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerAssets.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
