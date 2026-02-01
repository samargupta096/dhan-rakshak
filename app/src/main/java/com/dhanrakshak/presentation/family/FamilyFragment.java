package com.dhanrakshak.presentation.family;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.dhanrakshak.databinding.FragmentFamilyBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Family Fragment - Manage family members and sharing.
 * Enables inviting family members and viewing shared dashboard.
 */
@AndroidEntryPoint
public class FamilyFragment extends Fragment {

    private FragmentFamilyBinding binding;
    private FamilyViewModel viewModel;

    private FamilyEventAdapter eventAdapter; // Added

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentFamilyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FamilyViewModel.class);

        setupUI();
        setupListeners();
        observeData();
    }

    private void setupUI() {
        // Members List
        binding.recyclerViewMembers.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Events List
        eventAdapter = new FamilyEventAdapter();
        binding.recyclerViewEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewEvents.setAdapter(eventAdapter);
    }

    private void setupListeners() {
        binding.btnInvite.setOnClickListener(v -> showInviteDialog());
        binding.btnJoinFamily.setOnClickListener(v -> showJoinFamilyDialog());
        binding.cardFamilyDashboard.setOnClickListener(v -> openFamilyDashboard());

        binding.btnAddEvent.setOnClickListener(v -> showAddEventDialog());

        binding.tabLayout
                .addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                        if (tab.getPosition() == 0) {
                            binding.layoutMembers.setVisibility(View.VISIBLE);
                            binding.layoutEvents.setVisibility(View.GONE);
                        } else {
                            binding.layoutMembers.setVisibility(View.GONE);
                            binding.layoutEvents.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    }
                });
    }

    private void observeData() {
        viewModel.getFamilyMembers().observe(getViewLifecycleOwner(), members -> {
            binding.emptyView.setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
            binding.recyclerViewMembers.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);
            binding.cardFamilyDashboard.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);

            // Update member count
            binding.tvMemberCount.setText(String.format("%d Members", members.size()));
        });

        viewModel.getFamilyEvents().observe(getViewLifecycleOwner(), events -> {
            eventAdapter.setEvents(events);
            binding.emptyViewEvents.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            binding.recyclerViewEvents.setVisibility(events.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getInviteCode().observe(getViewLifecycleOwner(), code -> {
            if (code != null && !code.isEmpty()) {
                showInviteCodeDialog(code);
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Load data
        viewModel.loadFamilyMembers();
        viewModel.loadFamilyEvents(); // Added
    }

    private void showInviteDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("👨‍👩‍👧‍👦 Invite Family Member")
                .setMessage(
                        "Generate an invite code to share with your family member. They can use this code to join your family group.")
                .setPositiveButton("Generate Code", (dialog, which) -> {
                    viewModel.generateInviteCode();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showInviteCodeDialog(String code) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("🔗 Your Invite Code")
                .setMessage(
                        "Share this code with your family member:\n\n" + code + "\n\nThis code expires in 24 hours.")
                .setPositiveButton("Copy Code", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) requireContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Invite Code", code);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "Code copied!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showJoinFamilyDialog() {
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter invite code");
        input.setPadding(48, 32, 48, 32);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("🔑 Join Family")
                .setMessage("Enter the invite code you received from a family member.")
                .setView(input)
                .setPositiveButton("Join", (dialog, which) -> {
                    String code = input.getText().toString().trim();
                    if (!code.isEmpty()) {
                        viewModel.joinFamily(code);
                    } else {
                        Toast.makeText(requireContext(), "Please enter a valid code", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openFamilyDashboard() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FamilyDashboardFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
