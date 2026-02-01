package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.UpiAccount;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface UpiAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(UpiAccount account);

    @Update
    Completable update(UpiAccount account);

    @Delete
    Completable delete(UpiAccount account);

    @Query("SELECT * FROM upi_accounts WHERE isActive = 1 ORDER BY isPrimary DESC, usageCount DESC")
    Flowable<List<UpiAccount>> getActiveAccounts();

    @Query("SELECT * FROM upi_accounts ORDER BY isPrimary DESC, usageCount DESC")
    Flowable<List<UpiAccount>> getAllAccounts();

    @Query("SELECT * FROM upi_accounts WHERE id = :id")
    Single<UpiAccount> getById(long id);

    @Query("SELECT * FROM upi_accounts WHERE upiId = :upiId")
    Single<UpiAccount> getByUpiId(String upiId);

    @Query("SELECT * FROM upi_accounts WHERE isPrimary = 1 LIMIT 1")
    Single<UpiAccount> getPrimaryAccount();

    @Query("SELECT * FROM upi_accounts WHERE bankName = :bankName AND isActive = 1")
    Flowable<List<UpiAccount>> getByBankName(String bankName);

    @Query("SELECT * FROM upi_accounts WHERE upiApp = :app AND isActive = 1")
    Flowable<List<UpiAccount>> getByUpiApp(String app);

    @Query("SELECT SUM(lastKnownBalance) FROM upi_accounts WHERE isActive = 1")
    Single<Double> getTotalBalance();

    @Query("UPDATE upi_accounts SET lastKnownBalance = :balance, lastBalanceCheckTime = :time, isBalanceStale = 0 WHERE id = :id")
    Completable updateBalance(long id, double balance, long time);

    @Query("UPDATE upi_accounts SET isPrimary = 0")
    Completable clearAllPrimary();

    @Query("UPDATE upi_accounts SET isPrimary = 1 WHERE id = :id")
    Completable setPrimary(long id);

    @Query("UPDATE upi_accounts SET dailyUsed = 0")
    Completable resetAllDailyUsage();

    @Query("UPDATE upi_accounts SET monthlyUsed = 0, dailyUsed = 0")
    Completable resetAllMonthlyUsage();

    @Query("UPDATE upi_accounts SET isBalanceStale = 1")
    Completable markAllBalancesStale();
}
