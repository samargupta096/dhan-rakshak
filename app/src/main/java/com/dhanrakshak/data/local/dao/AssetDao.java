package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.Asset;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for Asset entity.
 */
@Dao
public interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Asset asset);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<Asset> assets);

    @Update
    Completable update(Asset asset);

    @Delete
    Completable delete(Asset asset);

    @Query("DELETE FROM assets WHERE id = :id")
    Completable deleteById(long id);

    @Query("SELECT * FROM assets WHERE id = :id")
    Single<Asset> getById(long id);

    @Query("SELECT * FROM assets ORDER BY currentValue DESC")
    Flowable<List<Asset>> getAllAssets();

    @Query("SELECT * FROM assets WHERE assetType = :type ORDER BY currentValue DESC")
    Flowable<List<Asset>> getAssetsByType(String type);

    @Query("SELECT SUM(currentValue) FROM assets")
    Flowable<Double> getTotalAssetsValue();

    @Query("SELECT SUM(investedAmount) FROM assets")
    Flowable<Double> getTotalInvestedAmount();

    @Query("SELECT SUM(currentValue) FROM assets WHERE assetType = :type")
    Flowable<Double> getValueByType(String type);

    @Query("SELECT * FROM assets WHERE name LIKE '%' || :query || '%' OR identifier LIKE '%' || :query || '%'")
    Flowable<List<Asset>> searchAssets(String query);

    @Query("SELECT COUNT(*) FROM assets")
    Single<Integer> getAssetCount();

    @Query("UPDATE assets SET currentPrice = :price, currentValue = quantity * :price, lastUpdated = :timestamp WHERE identifier = :identifier")
    Completable updatePrice(String identifier, double price, long timestamp);
}
