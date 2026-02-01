package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.InsurancePolicy;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface InsurancePolicyDao {

    @Query("SELECT * FROM insurance_policies ORDER BY renewalDate ASC")
    Flowable<List<InsurancePolicy>> getAllPolicies();

    @Query("SELECT * FROM insurance_policies WHERE category = :category ORDER BY renewalDate ASC")
    Flowable<List<InsurancePolicy>> getPoliciesByCategory(String category);

    @Query("SELECT SUM(premiumAmount) FROM insurance_policies")
    Single<Double> getTotalPremiumAmount();

    @Query("SELECT SUM(sumInsured) FROM insurance_policies WHERE category = 'HEALTH'")
    Single<Double> getTotalHealthCover();

    @Query("SELECT * FROM insurance_policies WHERE renewalDate BETWEEN :startDate AND :endDate")
    Flowable<List<InsurancePolicy>> getUpcomingRenewals(long startDate, long endDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPolicy(InsurancePolicy policy);

    @Update
    Completable updatePolicy(InsurancePolicy policy);

    @Delete
    Completable deletePolicy(InsurancePolicy policy);
}
