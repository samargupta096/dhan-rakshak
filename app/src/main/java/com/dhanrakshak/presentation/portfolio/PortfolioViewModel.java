package com.dhanrakshak.presentation.portfolio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.data.local.entity.FixedDeposit;
import com.dhanrakshak.data.local.entity.RecurringDeposit;
import com.dhanrakshak.data.repository.BankRepository;
import com.dhanrakshak.data.repository.DepositRepository;
import com.dhanrakshak.data.repository.MutualFundRepository;
import com.dhanrakshak.data.repository.StockRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Portfolio management.
 */
@HiltViewModel
public class PortfolioViewModel extends ViewModel {

    private final StockRepository stockRepository;
    private final MutualFundRepository mfRepository;
    private final BankRepository bankRepository;
    private final DepositRepository depositRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // LiveData for each asset type
    private final MutableLiveData<List<Asset>> stocks = new MutableLiveData<>();
    private final MutableLiveData<List<Asset>> mutualFunds = new MutableLiveData<>();
    private final MutableLiveData<List<BankAccount>> bankAccounts = new MutableLiveData<>();
    private final MutableLiveData<List<FixedDeposit>> fixedDeposits = new MutableLiveData<>();
    private final MutableLiveData<List<RecurringDeposit>> recurringDeposits = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    @Inject
    public PortfolioViewModel(StockRepository stockRepository, MutualFundRepository mfRepository,
            BankRepository bankRepository, DepositRepository depositRepository) {
        this.stockRepository = stockRepository;
        this.mfRepository = mfRepository;
        this.bankRepository = bankRepository;
        this.depositRepository = depositRepository;

        loadAllData();
    }

    private void loadAllData() {
        loadStocks();
        loadMutualFunds();
        loadBankAccounts();
        loadFixedDeposits();
        loadRecurringDeposits();
    }

    private void loadStocks() {
        disposables.add(
                stockRepository.getAllStocks()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(stocks::setValue, this::handleError));
    }

    private void loadMutualFunds() {
        disposables.add(
                mfRepository.getAllMutualFunds()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(mutualFunds::setValue, this::handleError));
    }

    private void loadBankAccounts() {
        disposables.add(
                bankRepository.getActiveAccounts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bankAccounts::setValue, this::handleError));
    }

    private void loadFixedDeposits() {
        disposables.add(
                depositRepository.getActiveFixedDeposits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fixedDeposits::setValue, this::handleError));
    }

    private void loadRecurringDeposits() {
        disposables.add(
                depositRepository.getActiveRecurringDeposits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(recurringDeposits::setValue, this::handleError));
    }

    // ===== Stock Operations =====

    public void addStock(String symbol, String name, double quantity, double avgPrice) {
        isLoading.setValue(true);
        disposables.add(
                stockRepository.addStock(symbol, name, quantity, avgPrice)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    operationSuccess.setValue(true);
                                },
                                this::handleError));
    }

    public void deleteStock(Asset stock) {
        disposables.add(
                stockRepository.deleteStock(stock)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> operationSuccess.setValue(true), this::handleError));
    }

    public void refreshStockPrices() {
        List<Asset> currentStocks = stocks.getValue();
        if (currentStocks == null || currentStocks.isEmpty())
            return;

        isLoading.setValue(true);
        disposables.add(
                stockRepository.refreshAllStockPrices(currentStocks)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> isLoading.setValue(false),
                                this::handleError));
    }

    // ===== Mutual Fund Operations =====

    public void addMutualFund(long schemeCode, String schemeName, double units, double avgNav) {
        isLoading.setValue(true);
        disposables.add(
                mfRepository.addMutualFund(schemeCode, schemeName, units, avgNav)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    operationSuccess.setValue(true);
                                },
                                this::handleError));
    }

    public void deleteMutualFund(Asset mf) {
        disposables.add(
                mfRepository.deleteMutualFund(mf)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> operationSuccess.setValue(true), this::handleError));
    }

    public void syncMutualFundNav() {
        isLoading.setValue(true);
        disposables.add(
                mfRepository.syncNavData()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    operationSuccess.setValue(true);
                                },
                                this::handleError));
    }

    // ===== Bank Account Operations =====

    public void addBankAccount(String bankName, String accountType, String lastFourDigits) {
        isLoading.setValue(true);
        disposables.add(
                bankRepository.addAccount(bankName, accountType, lastFourDigits)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    operationSuccess.setValue(true);
                                },
                                this::handleError));
    }

    public void updateBankBalance(long accountId, double balance) {
        disposables.add(
                bankRepository.updateBalance(accountId, balance)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> operationSuccess.setValue(true), this::handleError));
    }

    public void deleteBankAccount(BankAccount account) {
        disposables.add(
                bankRepository.deleteAccount(account)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> operationSuccess.setValue(true), this::handleError));
    }

    // ===== FD/RD Operations =====

    public void addFixedDeposit(String bankName, double principal, double interestRate,
            int tenureMonths, String compoundingFrequency,
            long startDate, long maturityDate) {
        isLoading.setValue(true);
        disposables.add(
                depositRepository.addFixedDeposit(bankName, principal, interestRate, tenureMonths,
                        compoundingFrequency, startDate, maturityDate)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    operationSuccess.setValue(true);
                                },
                                this::handleError));
    }

    public void deleteFixedDeposit(FixedDeposit fd) {
        disposables.add(
                depositRepository.deleteFixedDeposit(fd)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> operationSuccess.setValue(true), this::handleError));
    }

    public void addRecurringDeposit(String bankName, double monthlyAmount, double interestRate,
            int tenureMonths, long startDate, long maturityDate) {
        isLoading.setValue(true);
        disposables.add(
                depositRepository.addRecurringDeposit(bankName, monthlyAmount, interestRate,
                        tenureMonths, startDate, maturityDate)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    isLoading.setValue(false);
                                    operationSuccess.setValue(true);
                                },
                                this::handleError));
    }

    public void deleteRecurringDeposit(RecurringDeposit rd) {
        disposables.add(
                depositRepository.deleteRecurringDeposit(rd)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> operationSuccess.setValue(true), this::handleError));
    }

    private void handleError(Throwable error) {
        isLoading.setValue(false);
        errorMessage.setValue(error.getMessage());
    }

    // Getters
    public LiveData<List<Asset>> getStocks() {
        return stocks;
    }

    public LiveData<List<Asset>> getMutualFunds() {
        return mutualFunds;
    }

    public LiveData<List<BankAccount>> getBankAccounts() {
        return bankAccounts;
    }

    public LiveData<List<FixedDeposit>> getFixedDeposits() {
        return fixedDeposits;
    }

    public LiveData<List<RecurringDeposit>> getRecurringDeposits() {
        return recurringDeposits;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
