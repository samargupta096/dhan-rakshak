package com.dhanrakshak.presentation.health;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.InsurancePolicyDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.dao.HealthMetricDao;
import com.dhanrakshak.data.local.dao.LabReportDao;
import com.dhanrakshak.data.local.entity.InsurancePolicy;
import com.dhanrakshak.data.local.entity.SmsTransaction;
import com.dhanrakshak.data.local.entity.HealthMetric;
import com.dhanrakshak.data.local.entity.LabReport;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class HealthViewModel extends ViewModel {

    private final InsurancePolicyDao policyDao;
    private final SmsTransactionDao smsTransactionDao;
    private final HealthMetricDao healthMetricDao;
    private final LabReportDao labReportDao;
    private final com.dhanrakshak.data.repository.HealthRepository healthRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<InsurancePolicy>> policies = new MutableLiveData<>();
    private final MutableLiveData<List<HealthMetric>> healthMetrics = new MutableLiveData<>();
    private final MutableLiveData<List<LabReport>> labReports = new MutableLiveData<>();

    private final MutableLiveData<Double> totalMedicalExpense = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalHealthCover = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalYearlyPremium = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public HealthViewModel(InsurancePolicyDao policyDao, SmsTransactionDao smsTransactionDao,
            HealthMetricDao healthMetricDao, LabReportDao labReportDao,
            com.dhanrakshak.data.repository.HealthRepository healthRepository) {
        this.policyDao = policyDao;
        this.smsTransactionDao = smsTransactionDao;
        this.healthMetricDao = healthMetricDao;
        this.labReportDao = labReportDao;
        this.healthRepository = healthRepository;
        loadData();
    }

    private void loadData() {
        isLoading.setValue(true);
        loadPolicies();
        loadMedicalExpenses();
        loadHealthMetrics();
        loadLabReports();
        healthRepository.refreshHealthData(); // Sync health data
    }

    private void loadPolicies() {
        disposables.add(policyDao.getAllPolicies()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    policies.setValue(list);
                    calculatePolicyStats(list);
                    isLoading.setValue(false);
                }, Throwable::printStackTrace));
    }

    private void loadHealthMetrics() {
        disposables.add(healthMetricDao.getAllMetrics()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    healthMetrics.setValue(list);
                }, Throwable::printStackTrace));
    }

    private void loadLabReports() {
        disposables.add(labReportDao.getAllReports()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    labReports.setValue(list);
                }, Throwable::printStackTrace));
    }

    private void calculatePolicyStats(List<InsurancePolicy> policyList) {
        double cover = 0;
        double premium = 0;
        for (InsurancePolicy p : policyList) {
            if ("HEALTH".equalsIgnoreCase(p.getCategory())) {
                cover += p.getSumInsured();
            }
            double policyPremium = p.getPremiumAmount();
            if ("MONTHLY".equalsIgnoreCase(p.getPremiumFrequency())) {
                policyPremium *= 12;
            }
            premium += policyPremium;
        }
        totalHealthCover.setValue(cover);
        totalYearlyPremium.setValue(premium);
    }

    private void loadMedicalExpenses() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startOfMonth = cal.getTimeInMillis();
        long endOfMonth = System.currentTimeMillis();

        disposables.add(smsTransactionDao.getTransactionsByCategoryBetweenDates("Health", startOfMonth, endOfMonth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transactions -> {
                    double total = 0;
                    for (SmsTransaction t : transactions) {
                        total += t.getAmount();
                    }
                    totalMedicalExpense.setValue(total);
                }, Throwable::printStackTrace));
    }

    public void addPolicy(InsurancePolicy policy) {
        disposables.add(policyDao.insertPolicy(policy)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::loadPolicies, Throwable::printStackTrace));
    }

    public void addHealthMetric(HealthMetric metric) {
        disposables.add(healthMetricDao.insert(metric)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::loadHealthMetrics, Throwable::printStackTrace));
    }

    public void addLabReport(LabReport report) {
        disposables.add(labReportDao.insert(report)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::loadLabReports, Throwable::printStackTrace));
    }

    // Getters
    public LiveData<List<InsurancePolicy>> getPolicies() {
        return policies;
    }

    public LiveData<List<HealthMetric>> getHealthMetrics() {
        return healthMetrics;
    }

    public LiveData<List<LabReport>> getLabReports() {
        return labReports;
    }

    public LiveData<Double> getTotalMedicalExpense() {
        return totalMedicalExpense;
    }

    public LiveData<Double> getTotalHealthCover() {
        return totalHealthCover;
    }

    public LiveData<Double> getTotalYearlyPremium() {
        return totalYearlyPremium;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
