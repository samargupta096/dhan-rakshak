package com.dhanrakshak.presentation.family;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.FamilyMemberDao;
import com.dhanrakshak.data.local.entity.FamilyMember;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class FamilyDashboardViewModel extends ViewModel {

    private final FamilyMemberDao familyMemberDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Double> totalFamilyNetWorth = new MutableLiveData<>();
    private final MutableLiveData<List<FamilyMember>> memberContributions = new MutableLiveData<>();

    @Inject
    public FamilyDashboardViewModel(FamilyMemberDao familyMemberDao) {
        this.familyMemberDao = familyMemberDao;
    }

    public LiveData<Double> getTotalFamilyNetWorth() {
        return totalFamilyNetWorth;
    }

    public LiveData<List<FamilyMember>> getMemberContributions() {
        return memberContributions;
    }

    public void loadDashboardData() {
        // Calculate total net worth from all members' contributions
        disposables.add(
                familyMemberDao.getTotalFamilyNetWorth()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                total -> totalFamilyNetWorth.setValue(total != null ? total : 0.0),
                                Throwable::printStackTrace));

        // Load members for breakdown
        disposables.add(
                familyMemberDao.getAllMembers()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                memberContributions::setValue,
                                Throwable::printStackTrace));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
