package com.dhanrakshak.presentation.trip;

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
import androidx.recyclerview.widget.RecyclerView;

import com.dhanrakshak.R;
import com.dhanrakshak.data.local.entity.Trip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TripListFragment extends Fragment implements TripAdapter.OnTripClickListener {

    private TripViewModel viewModel;
    private TripAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        setupRecyclerView(view);
        setupFab(view);
        observeData();
    }

    private void setupRecyclerView(View view) {
        RecyclerView recycler = view.findViewById(R.id.recyclerTrips);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TripAdapter(this);
        recycler.setAdapter(adapter);
    }

    private void setupFab(View view) {
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddTrip);
        fab.setOnClickListener(v -> showAddTripDialog());
    }

    private void observeData() {
        viewModel.getUpcomingTrips().observe(getViewLifecycleOwner(), trips -> {
            adapter.submitList(trips);
        });
    }

    private void showAddTripDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_trip, null);
        TextInputEditText inputName = dialogView.findViewById(R.id.inputTripName);
        TextInputEditText inputBudget = dialogView.findViewById(R.id.inputBudget);

        // Date Selection logic
        final long[] startDate = { 0 };
        final long[] endDate = { 0 };

        dialogView.findViewById(R.id.btnDateRange).setOnClickListener(v -> {
            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = MaterialDatePicker.Builder
                    .dateRangePicker()
                    .setTitleText("Select Trip Dates")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                startDate[0] = selection.first;
                endDate[0] = selection.second;
            });
            picker.show(getParentFragmentManager(), "TRIP_DATE_PICKER");
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Plan a New Trip")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = inputName.getText().toString();
                    String budgetStr = inputBudget.getText().toString();

                    if (name.isEmpty() || budgetStr.isEmpty() || startDate[0] == 0) {
                        Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double budget = Double.parseDouble(budgetStr);
                    // Create minimal trip object, destination can be empty or added to dialog
                    Trip trip = new Trip(name, "", startDate[0], endDate[0], budget);
                    viewModel.addTrip(trip);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onTripClick(Trip trip) {
        // Navigate to Detail Fragment
        // For now, simple transition. Real implementation would use Navigation
        // Component or FragmentManager
        TripDetailFragment detailFragment = TripDetailFragment.newInstance(trip.getId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment) // Assuming 'fragment_container' exists in
                                                                  // MainActivity
                .addToBackStack(null)
                .commit();
    }
}
