package com.dhanrakshak.presentation.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.SmsTransaction;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Dashboard screen.
 */
@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final AssetDao assetDao;
    private final BankAccountDao bankAccountDao;
    private final SmsTransactionDao smsTransactionDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Double> totalNetWorth = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalProfitLoss = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<AssetAllocationItem>> assetAllocation = new MutableLiveData<>();
    private final MutableLiveData<List<SmsTransaction>> recentTransactions = new MutableLiveData<>();

    @Inject
    public DashboardViewModel(AssetDao assetDao, BankAccountDao bankAccountDao,
            SmsTransactionDao smsTransactionDao) {
        this.assetDao = assetDao;
        this.bankAccountDao = bankAccountDao;
        this.smsTransactionDao = smsTransactionDao;

        loadData();
    }

    private void loadData() {
        loadNetWorth();
        loadAssetAllocation();
        loadRecentTransactions();
    }

    private void loadNetWorth() {
        // Load total assets value
        disposables.add(
                assetDao.getTotalAssetsValue()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(assetValue -> {
                            // Also add bank balances
                            disposables.add(
                                    bankAccountDao.getTotalBankBalance()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bankBalance -> {
                                                double assetVal = assetValue != null ? assetValue : 0.0;
                                                double bankVal = bankBalance != null ? bankBalance : 0.0;
                                                totalNetWorth.setValue(assetVal + bankVal);
                                            }, error -> totalNetWorth.setValue(assetValue != null ? assetValue : 0.0)));
                        }, error -> {
                        }));

        // Load profit/loss
        disposables.add(
                assetDao.getTotalInvestedAmount()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(invested -> {
                            disposables.add(
                                    assetDao.getTotalAssetsValue()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(current -> {
                                                double investedVal = invested != null ? invested : 0.0;
                                                double currentVal = current != null ? current : 0.0;
                                                totalProfitLoss.setValue(currentVal - investedVal);
                                            }, error -> {
                                            }));
                        }, error -> {
                        }));
    }

    private void loadAssetAllocation() {
        List<AssetAllocationItem> allocations = new ArrayList<>();

        // Load values for each asset type
        String[] assetTypes = { "STOCK", "MUTUAL_FUND", "GOLD", "EPF", "PPF", "BANK" };
        String[] colors = { "#2196F3", "#9C27B0", "#FFC107", "#4CAF50", "#00BCD4", "#607D8B" };
        String[] labels = { "Stocks", "Mutual Funds", "Gold", "EPF", "PPF", "Bank" };

        for (int i = 0; i < assetTypes.length; i++) {
            final int index = i;
            disposables.add(
                    assetDao.getValueByType(assetTypes[i])
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(value -> {
                                if (value != null && value > 0) {
                                    allocations.add(new AssetAllocationItem(
                                            labels[index], value, colors[index]));
                                    recalculatePercentages(allocations);
                                }
                            }, error -> {
                            }));
        }

        // Add bank balance
        disposables.add(
                bankAccountDao.getTotalBankBalance()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(value -> {
                            if (value != null && value > 0) {
                                allocations.add(new AssetAllocationItem("Bank", value, "#607D8B"));
                                recalculatePercentages(allocations);
                            }
                        }, error -> {
                        }));
    }

    private void recalculatePercentages(List<AssetAllocationItem> allocations) {
        double total = 0;
        for (AssetAllocationItem item : allocations) {
            total += item.value;
        }

        if (total > 0) {
            for (AssetAllocationItem item : allocations) {
                item.percentage = (item.value / total) * 100;
            }
        }

        assetAllocation.setValue(new ArrayList<>(allocations));
    }

    private void loadRecentTransactions() {
        disposables.add(
                smsTransactionDao.getRecentTransactions(10)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                recentTransactions::setValue,
                                error -> recentTransactions.setValue(new ArrayList<>())));
    }

    public LiveData<Double> getTotalNetWorth() {
        return totalNetWorth;
    }

    public LiveData<Double> getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public LiveData<List<AssetAllocationItem>> getAssetAllocation() {
        return assetAllocation;
    }

    public LiveData<List<SmsTransaction>> getRecentTransactions() {
        return recentTransactions;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    /**
     * Asset allocation item for pie chart.
     */
    public static class AssetAllocationItem {
        public String assetType;
        public double value;
        public double percentage;
        public String color;

        public AssetAllocationItem(String assetType, double value, String color) {
            this.assetType = assetType;
            this.value = value;
            this.color = color;
        }
    }
}
