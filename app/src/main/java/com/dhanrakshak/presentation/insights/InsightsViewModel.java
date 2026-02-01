package com.dhanrakshak.presentation.insights;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.ai.insights.AiFinanceInsightsEngine;
import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.FixedDepositDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.data.local.entity.FixedDeposit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for AI-powered Insights screen.
 */
@HiltViewModel
public class InsightsViewModel extends ViewModel {

    private final AssetDao assetDao;
    private final BankAccountDao bankAccountDao;
    private final FixedDepositDao fixedDepositDao;
    private final SmsTransactionDao smsTransactionDao;
    private final com.dhanrakshak.data.local.dao.TransactionDao transactionDao;
    private final AiFinanceInsightsEngine insightsEngine;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<AiFinanceInsightsEngine.PortfolioInsights> insights = new MutableLiveData<>();
    private final MutableLiveData<Double> monthlyIncome = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> monthlyExpenses = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Cached data
    private List<Asset> cachedAssets = new ArrayList<>();
    private List<BankAccount> cachedBankAccounts = new ArrayList<>();
    private List<FixedDeposit> cachedFixedDeposits = new ArrayList<>();
    private List<com.dhanrakshak.data.local.entity.Transaction> cachedTransactions = new ArrayList<>();

    @Inject
    public InsightsViewModel(
            AssetDao assetDao,
            BankAccountDao bankAccountDao,
            FixedDepositDao fixedDepositDao,
            SmsTransactionDao smsTransactionDao,
            com.dhanrakshak.data.local.dao.TransactionDao transactionDao,
            AiFinanceInsightsEngine insightsEngine) {
        this.assetDao = assetDao;
        this.bankAccountDao = bankAccountDao;
        this.fixedDepositDao = fixedDepositDao;
        this.smsTransactionDao = smsTransactionDao;
        this.transactionDao = transactionDao;
        this.insightsEngine = insightsEngine;

        loadData();
    }

    private void loadData() {
        isLoading.setValue(true);

        // Load assets
        disposables.add(
                assetDao.getAllAssets()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(assets -> {
                            cachedAssets = assets;
                            checkAndGenerateInsights();
                        }, this::handleError));

        // Load bank accounts
        disposables.add(
                bankAccountDao.getAllActiveAccounts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(accounts -> {
                            cachedBankAccounts = accounts;
                            checkAndGenerateInsights();
                        }, this::handleError));

        // Load fixed deposits
        disposables.add(
                fixedDepositDao.getActiveDeposits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(deposits -> {
                            cachedFixedDeposits = deposits;
                            checkAndGenerateInsights();
                        }, this::handleError));

        // Load recent transactions for forecasting
        disposables.add(
                transactionDao.getAllTransactions()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(transactions -> {
                            cachedTransactions = transactions;
                            checkAndGenerateInsights();
                        }, error -> {
                            // Non-critical, just log or ignore
                            cachedTransactions = new ArrayList<>();
                            checkAndGenerateInsights();
                        }));

        // Calculate monthly income/expenses from SMS transactions
        calculateMonthlyStats();
    }

    // ...

    private void checkAndGenerateInsights() {
        // Generate insights when all data is loaded
        // For simplicity, we just call it whenever any data updates.
        // In product code, we might want to wait for "all" or use zip.

        AiFinanceInsightsEngine.PortfolioInsights result = insightsEngine.analyzePortfolio(
                cachedAssets,
                cachedBankAccounts,
                cachedFixedDeposits,
                cachedTransactions,
                monthlyIncome.getValue() != null ? monthlyIncome.getValue() : 0.0,
                monthlyExpenses.getValue() != null ? monthlyExpenses.getValue() : 0.0);

        insights.setValue(result);
        isLoading.setValue(false);
    }

    Calendar cal = Calendar.getInstance();cal.set(Calendar.DAY_OF_MONTH,1);cal.set(Calendar.HOUR_OF_DAY,0);
    long startOfMonth = cal.getTimeInMillis();
    long now = System.currentTimeMillis();

    // Get monthly credits (income)
    disposables.add(smsTransactionDao.getTotalCreditBetweenDates(startOfMonth,now).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(total->monthlyIncome.setValue(total!=null?total:0.0),error->monthlyIncome.setValue(0.0)));

    // Get monthly debits (expenses)
    disposables.add(smsTransactionDao.getTotalDebitBetweenDates(startOfMonth,now).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(total->monthlyExpenses.setValue(total!=null?total:0.0),error->monthlyExpenses.setValue(0.0)));
    }

    private void checkAndGenerateInsights() {
        // Generate insights when all data is loaded
        AiFinanceInsightsEngine.PortfolioInsights result = insightsEngine.analyzePortfolio(
                cachedAssets,
                cachedBankAccounts,
                cachedFixedDeposits,
                monthlyIncome.getValue() != null ? monthlyIncome.getValue() : 0.0,
                monthlyExpenses.getValue() != null ? monthlyExpenses.getValue() : 0.0);

        insights.setValue(result);
        isLoading.setValue(false);
    }

    /**
     * Refresh insights with updated income/expense values.
     */
    public void refreshInsights(double income, double expenses) {
        monthlyIncome.setValue(income);
        monthlyExpenses.setValue(expenses);
        checkAndGenerateInsights();
    }

    private void handleError(Throwable error) {
        errorMessage.setValue("Error loading data: " + error.getMessage());
        isLoading.setValue(false);
    }

    // Getters
    public LiveData<AiFinanceInsightsEngine.PortfolioInsights> getInsights() {
        return insights;
    }

    public LiveData<Double> getMonthlyIncome() {
        return monthlyIncome;
    }

    public LiveData<Double> getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
