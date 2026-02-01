package com.dhanrakshak.data.repository;

import com.dhanrakshak.data.local.dao.FixedDepositDao;
import com.dhanrakshak.data.local.dao.RecurringDepositDao;
import com.dhanrakshak.data.local.entity.FixedDeposit;
import com.dhanrakshak.data.local.entity.RecurringDeposit;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Repository for Fixed Deposits and Recurring Deposits.
 */
@Singleton
public class DepositRepository {

    private final FixedDepositDao fdDao;
    private final RecurringDepositDao rdDao;

    @Inject
    public DepositRepository(FixedDepositDao fdDao, RecurringDepositDao rdDao) {
        this.fdDao = fdDao;
        this.rdDao = rdDao;
    }

    // ===== Fixed Deposit Operations =====

    public Flowable<List<FixedDeposit>> getAllFixedDeposits() {
        return fdDao.getAllDeposits();
    }

    public Flowable<List<FixedDeposit>> getActiveFixedDeposits() {
        return fdDao.getActiveDeposits();
    }

    public Completable addFixedDeposit(String bankName, double principal, double interestRate,
            int tenureMonths, String compoundingFrequency,
            long startDate, long maturityDate) {
        FixedDeposit fd = new FixedDeposit(bankName, principal, interestRate, tenureMonths,
                compoundingFrequency, startDate, maturityDate);
        return fdDao.insert(fd);
    }

    public Completable updateFixedDeposit(FixedDeposit fd) {
        return fdDao.update(fd);
    }

    public Completable deleteFixedDeposit(FixedDeposit fd) {
        return fdDao.delete(fd);
    }

    public Single<FixedDeposit> getFixedDepositById(long id) {
        return fdDao.getById(id);
    }

    public Flowable<Double> getTotalFdPrincipal() {
        return fdDao.getTotalPrincipal();
    }

    public Flowable<Double> getTotalFdMaturityValue() {
        return fdDao.getTotalMaturityValue();
    }

    // ===== Recurring Deposit Operations =====

    public Flowable<List<RecurringDeposit>> getAllRecurringDeposits() {
        return rdDao.getAllDeposits();
    }

    public Flowable<List<RecurringDeposit>> getActiveRecurringDeposits() {
        return rdDao.getActiveDeposits();
    }

    public Completable addRecurringDeposit(String bankName, double monthlyAmount, double interestRate,
            int tenureMonths, long startDate, long maturityDate) {
        RecurringDeposit rd = new RecurringDeposit(bankName, monthlyAmount, interestRate,
                tenureMonths, startDate, maturityDate);
        return rdDao.insert(rd);
    }

    public Completable updateRecurringDeposit(RecurringDeposit rd) {
        return rdDao.update(rd);
    }

    public Completable deleteRecurringDeposit(RecurringDeposit rd) {
        return rdDao.delete(rd);
    }

    public Single<RecurringDeposit> getRecurringDepositById(long id) {
        return rdDao.getById(id);
    }

    public Flowable<Double> getTotalRdDeposited() {
        return rdDao.getTotalDeposited();
    }

    public Flowable<Double> getTotalRdMaturityValue() {
        return rdDao.getTotalMaturityValue();
    }

    /**
     * Record a new RD installment payment.
     */
    public Completable recordRdInstallment(long rdId, int newInstallmentCount) {
        return rdDao.updateInstallmentCount(rdId, newInstallmentCount);
    }
}
