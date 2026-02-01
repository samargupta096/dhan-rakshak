package com.dhanrakshak.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dhanrakshak.data.local.dao.HealthGoalDao;
import com.dhanrakshak.data.local.entity.HealthGoal;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class HealthRepository {

    private final HealthGoalDao healthGoalDao;

    // Simulated Google Health Connect Data
    private final MutableLiveData<Map<String, Double>> dailyHealthData = new MutableLiveData<>();

    @Inject
    public HealthRepository(HealthGoalDao healthGoalDao) {
        this.healthGoalDao = healthGoalDao;
        refreshHealthData(); // Initial simulated sync
    }

    public LiveData<java.util.List<HealthGoal>> getAllGoals() {
        return healthGoalDao.getAllGoals();
    }

    public LiveData<HealthGoal> getGoalByType(String type) {
        return healthGoalDao.getGoalByType(type);
    }

    public void saveGoal(HealthGoal goal) {
        Completable.fromAction(() -> healthGoalDao.insert(goal))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public LiveData<Map<String, Double>> getDailyHealthData() {
        return dailyHealthData;
    }

    /**
     * Simulates fetching data from Google Health Connect.
     * In a real app, this would use Health Connect Client.
     */
    public void refreshHealthData() {
        // Simulate data fetching (Values would normally come from Health Connect API)
        Map<String, Double> data = new HashMap<>();
        Random random = new Random();

        // Simulated Steps (Random between 2000 and 12000)
        data.put("STEPS", 2000 + (10000 * random.nextDouble()));

        // Simulated Calories Burned (Random between 1500 and 3000)
        data.put("CALORIES", 1500 + (1500 * random.nextDouble()));

        // Simulated Sleep Hours (Random between 5 and 9)
        data.put("SLEEP", 5 + (4 * random.nextDouble()));

        dailyHealthData.postValue(data);
    }
}
