package com.dhanrakshak.data.repository;

import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.entity.BankAccount;
import com.dhanrakshak.data.local.entity.SmsTransaction;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Repository for Bank Account operations.
 */
@Singleton
public class BankRepository {

    private final BankAccountDao bankAccountDao;
    private final SmsTransactionDao smsTransactionDao;

    @Inject
    public BankRepository(BankAccountDao bankAccountDao, SmsTransactionDao smsTransactionDao) {
        this.bankAccountDao = bankAccountDao;
        this.smsTransactionDao = smsTransactionDao;
    }

    /**
     * Get all bank accounts.
     */
    public Flowable<List<BankAccount>> getAllAccounts() {
        return bankAccountDao.getAllAccounts();
    }

    /**
     * Get active bank accounts.
     */
    public Flowable<List<BankAccount>> getActiveAccounts() {
        return bankAccountDao.getAllActiveAccounts();
    }

    /**
     * Add a new bank account.
     */
    public Completable addAccount(String bankName, String accountType, String lastFourDigits) {
        BankAccount account = new BankAccount(bankName, accountType, lastFourDigits);
        return bankAccountDao.insert(account);
    }

    /**
     * Update bank account.
     */
    public Completable updateAccount(BankAccount account) {
        return bankAccountDao.update(account);
    }

    /**
     * Delete bank account.
     */
    public Completable deleteAccount(BankAccount account) {
        return bankAccountDao.delete(account);
    }

    /**
     * Update account balance manually.
     */
    public Completable updateBalance(long accountId, double balance) {
        return bankAccountDao.updateBalance(accountId, balance, System.currentTimeMillis());
    }

    /**
     * Get total bank balance.
     */
    public Flowable<Double> getTotalBalance() {
        return bankAccountDao.getTotalBankBalance();
    }

    /**
     * Get transactions for a specific account.
     */
    public Flowable<List<SmsTransaction>> getAccountTransactions(long accountId) {
        return smsTransactionDao.getTransactionsByAccount(accountId);
    }

    /**
     * Get recent transactions across all accounts.
     */
    public Flowable<List<SmsTransaction>> getRecentTransactions(int limit) {
        return smsTransactionDao.getRecentTransactions(limit);
    }

    /**
     * Get total spending for a date range.
     */
    public Single<Double> getTotalSpending(long startDate, long endDate) {
        return smsTransactionDao.getTotalDebitBetweenDates(startDate, endDate);
    }

    /**
     * Get total income for a date range.
     */
    public Single<Double> getTotalIncome(long startDate, long endDate) {
        return smsTransactionDao.getTotalCreditBetweenDates(startDate, endDate);
    }

    /**
     * Get account by ID.
     */
    public Single<BankAccount> getAccountById(long accountId) {
        return bankAccountDao.getById(accountId);
    }
}
