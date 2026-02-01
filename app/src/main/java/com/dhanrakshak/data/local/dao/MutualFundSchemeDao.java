package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.MutualFundScheme;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for MutualFundScheme entity (AMFI data).
 */
@Dao
public interface MutualFundSchemeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(MutualFundScheme scheme);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<MutualFundScheme> schemes);

    @Update
    Completable update(MutualFundScheme scheme);

    @Delete
    Completable delete(MutualFundScheme scheme);

    @Query("SELECT * FROM mf_schemes WHERE schemeCode = :schemeCode")
    Single<MutualFundScheme> getBySchemeCode(long schemeCode);

    @Query("SELECT * FROM mf_schemes WHERE isin = :isin")
    Single<MutualFundScheme> getByIsin(String isin);

    @Query("SELECT * FROM mf_schemes WHERE schemeName LIKE '%' || :query || '%' LIMIT 50")
    Flowable<List<MutualFundScheme>> searchSchemes(String query);

    @Query("SELECT * FROM mf_schemes WHERE amcName = :amcName ORDER BY schemeName")
    Flowable<List<MutualFundScheme>> getSchemesByAmc(String amcName);

    @Query("SELECT DISTINCT amcName FROM mf_schemes ORDER BY amcName")
    Single<List<String>> getAllAmcNames();

    @Query("SELECT COUNT(*) FROM mf_schemes")
    Single<Integer> getSchemeCount();

    @Query("UPDATE mf_schemes SET latestNav = :nav, navDate = :navDate, lastUpdated = :timestamp WHERE schemeCode = :schemeCode")
    Completable updateNav(long schemeCode, double nav, long navDate, long timestamp);

    @Query("DELETE FROM mf_schemes")
    Completable deleteAll();
}
