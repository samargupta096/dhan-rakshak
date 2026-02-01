package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.CryptoHolding;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface CryptoHoldingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(CryptoHolding holding);

    @Update
    Completable update(CryptoHolding holding);

    @Delete
    Completable delete(CryptoHolding holding);

    @Query("SELECT * FROM crypto_holdings ORDER BY currentValue DESC")
    Flowable<List<CryptoHolding>> getAllHoldings();

    @Query("SELECT * FROM crypto_holdings WHERE id = :id")
    Single<CryptoHolding> getById(long id);

    @Query("SELECT * FROM crypto_holdings WHERE symbol = :symbol")
    Single<CryptoHolding> getBySymbol(String symbol);

    @Query("SELECT SUM(currentValue) FROM crypto_holdings")
    Single<Double> getTotalValue();

    @Query("SELECT SUM(investedAmount) FROM crypto_holdings")
    Single<Double> getTotalInvested();

    @Query("UPDATE crypto_holdings SET currentPriceInr = :price, currentValue = quantity * :price, lastPriceUpdate = :timestamp WHERE coinGeckoId = :coinId")
    Completable updatePrice(String coinId, double price, long timestamp);

    @Query("SELECT coinGeckoId FROM crypto_holdings")
    Single<List<String>> getAllCoinIds();
}
