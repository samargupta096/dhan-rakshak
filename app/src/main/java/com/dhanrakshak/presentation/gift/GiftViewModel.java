package com.dhanrakshak.presentation.gift;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.GiftTransactionDao;
import com.dhanrakshak.data.local.entity.GiftTransaction;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class GiftViewModel extends ViewModel {

    private final GiftTransactionDao giftDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<GiftTransaction>> transactions = new MutableLiveData<>();
    private final MutableLiveData<Double> totalGiven = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalReceived = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> netPosition = new MutableLiveData<>(0.0);

    @Inject
    public GiftViewModel(GiftTransactionDao giftDao) {
        this.giftDao = giftDao;
        loadData();
    }

    private void loadData() {
        loadTransactions();
        loadTotals();
    }

    private void loadTransactions() {
        disposables.add(giftDao.getAllTransactions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transactions::setValue, Throwable::printStackTrace));
    }

    private void loadTotals() {
        disposables.add(giftDao.getTotalGiven()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(given -> {
                    double g = given != null ? given : 0.0;
                    totalGiven.setValue(g);
                    calculateNet();
                }, Throwable::printStackTrace));

        disposables.add(giftDao.getTotalReceived()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(received -> {
                    double r = received != null ? received : 0.0;
                    totalReceived.setValue(r);
                    calculateNet();
                }, Throwable::printStackTrace));
    }

    private void calculateNet() {
        double g = totalGiven.getValue() != null ? totalGiven.getValue() : 0.0;
        double r = totalReceived.getValue() != null ? totalReceived.getValue() : 0.0;
        netPosition.setValue(r - g);
    }

    public void addTransaction(GiftTransaction transaction) {
        disposables.add(giftDao.insert(transaction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Flowable updates automatically
                }, Throwable::printStackTrace));
    }

    public LiveData<List<GiftTransaction>> getTransactions() {
        return transactions;
    }

    public LiveData<Double> getTotalGiven() {
        return totalGiven;
    }

    public LiveData<Double> getTotalReceived() {
        return totalReceived;
    }

    public LiveData<Double> getNetPosition() {
        return netPosition;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
