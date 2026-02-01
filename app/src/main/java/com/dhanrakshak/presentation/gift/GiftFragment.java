package com.dhanrakshak.presentation.gift;

import android.app.DatePickerDialog;
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
import com.dhanrakshak.data.local.entity.GiftTransaction;
import com.dhanrakshak.databinding.FragmentGiftBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GiftFragment extends Fragment {

    private FragmentGiftBinding binding;
    private GiftViewModel viewModel;
    private GiftAdapter adapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentGiftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GiftViewModel.class);

        setupRecyclerView();
        setupObservers();

        binding.fabAddGift.setOnClickListener(v -> showAddGiftDialog());
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new GiftAdapter();
        binding.recyclerGifts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerGifts.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getTotalGiven().observe(getViewLifecycleOwner(),
                amount -> binding.textTotalGiven.setText(currencyFormat.format(amount)));

        viewModel.getTotalReceived().observe(getViewLifecycleOwner(),
                amount -> binding.textTotalReceived.setText(currencyFormat.format(amount)));

        viewModel.getNetPosition().observe(getViewLifecycleOwner(), amount -> {
            binding.textNetPosition.setText(currencyFormat.format(Math.abs(amount)));
            if (amount > 0) {
                binding.textNetPosition.setTextColor(getResources().getColor(R.color.income_green, null));
                binding.textNetPosition.setText("+ " + currencyFormat.format(amount));
            } else if (amount < 0) {
                binding.textNetPosition.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            } else {
                binding.textNetPosition.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
        });

        viewModel.getTransactions().observe(getViewLifecycleOwner(), list -> adapter.setTransactions(list));
    }

    private void showAddGiftDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_gift, null);

        MaterialButtonToggleGroup toggleGroup = dialogView.findViewById(R.id.toggleType);
        com.google.android.material.textfield.EditText inputName = dialogView.findViewById(R.id.inputPersonName);
        com.google.android.material.textfield.EditText inputOccasion = dialogView.findViewById(R.id.inputOccasion);
        com.google.android.material.textfield.EditText inputValue = dialogView.findViewById(R.id.inputValue);
        com.google.android.material.textfield.EditText inputDesc = dialogView.findViewById(R.id.inputDescription);
        TextView textDate = dialogView.findViewById(R.id.textDate);

        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        textDate.setText("Date: " + sdf.format(calendar.getTime()));

        textDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                textDate.setText("Date: " + sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Gift")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        String type = toggleGroup.getCheckedButtonId() == R.id.btnGiven ? "GIVEN" : "RECEIVED";
                        String name = inputName.getText().toString();
                        String occasion = inputOccasion.getText().toString();
                        double value = Double.parseDouble(inputValue.getText().toString());
                        String desc = inputDesc.getText().toString();

                        if (name.isEmpty() || occasion.isEmpty()) {
                            Toast.makeText(requireContext(), "Name and Occasion required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        GiftTransaction t = new GiftTransaction(type, name, occasion, value,
                                calendar.getTimeInMillis());
                        t.setDescription(desc);

                        viewModel.addTransaction(t);
                        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {
        private List<GiftTransaction> transactions = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        void setTransactions(List<GiftTransaction> list) {
            this.transactions = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_transaction, parent, false);
            return new GiftViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
            GiftTransaction t = transactions.get(position);

            holder.textPerson.setText(t.getPersonName());
            holder.textOccasion.setText(t.getOccasion() + " • " + dateFormat.format(new Date(t.getDate())));
            holder.textAmount.setText(currencyFormat.format(t.getValue()));
            holder.textDesc.setText(t.getDescription() != null ? t.getDescription() : "");

            if ("GIVEN".equals(t.getType())) {
                holder.textAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                holder.textIcon.setText("📤");
            } else {
                holder.textAmount.setTextColor(getResources().getColor(R.color.income_green, null));
                holder.textIcon.setText("📥");
            }
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        class GiftViewHolder extends RecyclerView.ViewHolder {
            TextView textPerson, textOccasion, textAmount, textDesc, textIcon;

            GiftViewHolder(View itemView) {
                super(itemView);
                textPerson = itemView.findViewById(R.id.textPersonName);
                textOccasion = itemView.findViewById(R.id.textOccasion);
                textAmount = itemView.findViewById(R.id.textAmount);
                textDesc = itemView.findViewById(R.id.textDescription);
                textIcon = itemView.findViewById(R.id.textTypeIcon);
            }
        }
    }
}
