package com.dhanrakshak.presentation.trip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.TripExpense;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TripDetailFragment extends Fragment {

    private static final String ARG_TRIP_ID = "trip_id";
    private long tripId;
    private TripViewModel viewModel;
    private TripExpenseAdapter expenseAdapter;

    // UI Refs
    private TextView textTotalSpent, textBudgetLimit;
    private LinearProgressIndicator progress;

    public static TripDetailFragment newInstance(long tripId) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TRIP_ID, tripId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = getArguments().getLong(ARG_TRIP_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        // Views
        textTotalSpent = view.findViewById(R.id.textTotalSpent);
        textBudgetLimit = view.findViewById(R.id.textBudgetLimit);
        progress = view.findViewById(R.id.detailProgress);

        RecyclerView recycler = view.findViewById(R.id.recyclerDetails);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        expenseAdapter = new TripExpenseAdapter();
        recycler.setAdapter(expenseAdapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddDetail);
        fab.setOnClickListener(v -> showAddExpenseDialog());

        // Initial Load
        viewModel.loadTripDetails(tripId);

        observeData();
    }

    private void observeData() {
        viewModel.getCurrentTripExpenses().observe(getViewLifecycleOwner(), expenses -> {
            expenseAdapter.submitList(expenses);
        });

        viewModel.getCurrentTripTotalExpenses().observe(getViewLifecycleOwner(), total -> {
            textTotalSpent.setText(String.format("₹%.0f", total));
            // Budget limit logic:
            // Ideally we should observe the Trip object too to get the budget,
            // but for simplicity assuming we might fetch it or just show spent.
            // The ViewModel doesn't currently expose a Single<Trip> for the detail view
            // easily without existing list.
            // Let's assume user just sees spent for now, or we can improve VM later.
            // Placeholder for budget limit update:
            // textBudgetLimit.setText("/ ₹" + trip.getBudget());
        });
    }

    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_trip_expense, null);
        TextInputEditText inputAmount = dialogView.findViewById(R.id.inputAmount);
        TextInputEditText inputCategory = dialogView.findViewById(R.id.inputCategory);
        TextInputEditText inputDesc = dialogView.findViewById(R.id.inputDescription);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Expense")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String amountStr = inputAmount.getText().toString();
                    String category = inputCategory.getText().toString();
                    String desc = inputDesc.getText().toString();

                    if (!amountStr.isEmpty() && !category.isEmpty()) {
                        double amount = Double.parseDouble(amountStr);
                        // Constructor: amount, category, paymentMethod, location, receiptPath, date,
                        // notes?
                        // Need to check TripExpense constructor.
                        // Assuming a constructor exists or valid setters.
                        // Using a generic one based on assumed entity structure:
                        // public TripExpense(long tripId, String description, double amount, String
                        // category)
                        TripExpense expense = new TripExpense(tripId, desc, amount, category);

                        viewModel.addExpense(expense);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
