package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.BankAccount;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for BankAccount entity.
 */
@Dao
public interface BankAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(BankAccount bankAccount);

    @Update
    Completable update(BankAccount bankAccount);

    @Delete
    Completable delete(BankAccount bankAccount);

    @Query("SELECT * FROM bank_accounts WHERE id = :id")
    Single<BankAccount> getById(long id);

    @Query("SELECT * FROM bank_accounts WHERE isActive = 1 ORDER BY bankName")
    Flowable<List<BankAccount>> getAllActiveAccounts();

    @Query("SELECT * FROM bank_accounts ORDER BY bankName")
    Flowable<List<BankAccount>> getAllAccounts();

    @Query("SELECT * FROM bank_accounts WHERE bankName = :bankName AND accountNumberLast4 = :last4")
    Single<BankAccount> findByBankAndLast4(String bankName, String last4);

    @Query("SELECT SUM(balance) FROM bank_accounts WHERE isActive = 1")
    Flowable<Double> getTotalBankBalance();

    @Query("UPDATE bank_accounts SET balance = :balance, lastUpdated = :timestamp WHERE id = :id")
    Completable updateBalance(long id, double balance, long timestamp);

    @Query("SELECT * FROM bank_accounts WHERE bankName LIKE '%' || :query || '%'")
    Flowable<List<BankAccount>> searchAccounts(String query);
}
