package com.dhanrakshak.presentation.trip;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.TripDao;
import com.dhanrakshak.data.local.entity.Trip;
import com.dhanrakshak.data.local.entity.TripExpense;
import com.dhanrakshak.data.local.entity.TripLocation;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class TripViewModel extends ViewModel {

    private final TripDao tripDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<Trip>> upcomingTrips = new MutableLiveData<>();
    private final MutableLiveData<List<Trip>> pastTrips = new MutableLiveData<>(); // Could separate query
    private final MutableLiveData<Double> currentTripTotalExpenses = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<TripExpense>> currentTripExpenses = new MutableLiveData<>();
    private final MutableLiveData<List<TripLocation>> currentTripLocations = new MutableLiveData<>();

    @Inject
    public TripViewModel(TripDao tripDao) {
        this.tripDao = tripDao;
        loadTrips();
    }

    private void loadTrips() {
        disposables.add(tripDao.getAllTrips()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trips -> {
                    // Simple in-memory filter for now, or could use separate queries
                    upcomingTrips.setValue(trips);
                }, Throwable::printStackTrace));
    }

    public void addTrip(Trip trip) {
        disposables.add(tripDao.insertTrip(trip)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(id -> loadTrips(), Throwable::printStackTrace));
    }

    public void loadTripDetails(long tripId) {
        // Load Expenses
        disposables.add(tripDao.getExpensesForTrip(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currentTripExpenses::setValue, Throwable::printStackTrace));

        // Load Locations
        disposables.add(tripDao.getLocationsForTrip(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currentTripLocations::setValue, Throwable::printStackTrace));

        // Load Total
        disposables.add(tripDao.getTotalExpensesForTrip(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(total -> currentTripTotalExpenses.setValue(total != null ? total : 0.0),
                        Throwable::printStackTrace));
    }

    public void addExpense(TripExpense expense) {
        disposables.add(tripDao.insertExpense(expense)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Refresh handled by Flowable, but loadTripDetails triggers specific ID updates
                    // if needed
                    // Or just rely on the Flowable subscription in loadTripDetails if it persists
                    // (it's inside a method, so be careful)
                    // Actually, we should keep the flowable logic more robust.
                    // usage: view invokes loadTripDetails(id) when entering detail screen.
                }, Throwable::printStackTrace));
    }

    public void addLocation(TripLocation location) {
        disposables.add(tripDao.insertLocation(location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                }, Throwable::printStackTrace));
    }

    public LiveData<List<Trip>> getUpcomingTrips() {
        return upcomingTrips;
    }

    public LiveData<List<TripExpense>> getCurrentTripExpenses() {
        return currentTripExpenses;
    }

    public LiveData<List<TripLocation>> getCurrentTripLocations() {
        return currentTripLocations;
    }

    public LiveData<Double> getCurrentTripTotalExpenses() {
        return currentTripTotalExpenses;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
