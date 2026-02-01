package com.dhanrakshak.presentation.family;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhanrakshak.data.local.dao.FamilyMemberDao;
import com.dhanrakshak.data.local.entity.FamilyMember;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Family management.
 * Handles family member CRUD and invite code generation.
 */
@HiltViewModel
public class FamilyViewModel extends ViewModel {

    private final FamilyMemberDao familyMemberDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<FamilyMember>> familyMembers = new MutableLiveData<>();
    private final MutableLiveData<String> inviteCode = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Double> familyNetWorth = new MutableLiveData<>();

    // Family Events LiveData
    private LiveData<List<FamilyEvent>> familyEvents;

    @Inject
    public FamilyViewModel(FamilyMemberDao familyMemberDao,
            FamilyEventDao familyEventDao,
            SyncRepository syncRepository) {
        this.familyMemberDao = familyMemberDao;
        this.familyEventDao = familyEventDao;
        this.syncRepository = syncRepository;

        this.familyEvents = familyEventDao.getAllEvents();
    }

    public LiveData<List<FamilyMember>> getFamilyMembers() {
        return familyMembers;
    }

    public LiveData<String> getInviteCode() {
        return inviteCode;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Double> getFamilyNetWorth() {
        return familyNetWorth;
    }

    public LiveData<List<FamilyEvent>> getFamilyEvents() {
        return familyEvents;
    }

    public void loadFamilyMembers() {
        disposables.add(
                familyMemberDao.getAllMembers()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                members -> familyMembers.setValue(members),
                                error -> message.setValue("Failed to load members: " + error.getMessage())));
    }

    public void generateInviteCode() {
        // Generate a short, readable invite code
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        inviteCode.setValue(code);

        // In a real implementation, this would be stored in Firebase
        // for the invited user to validate
        message.setValue("Invite code generated!");
    }

    public void joinFamily(String code) {
        // In a real implementation, this would validate the code against Firebase
        // and add the current user to the family group
        if (code.length() >= 8) {
            message.setValue("Joining family... (Firebase integration required)");
        } else {
            message.setValue("Invalid invite code");
        }
    }

    public void addFamilyMember(FamilyMember member) {
        disposables.add(
                familyMemberDao.insert(member)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                id -> {
                                    message.setValue("Family member added!");
                                    loadFamilyMembers();
                                },
                                error -> message.setValue("Failed to add family member")));
    }

    public void removeFamilyMember(FamilyMember member) {
        disposables.add(
                familyMemberDao.delete(member)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    message.setValue("Family member removed");
                                    loadFamilyMembers();
                                },
                                error -> message.setValue("Failed to remove family member")));
    }

    public void calculateFamilyNetWorth() {
        disposables.add(
                familyMemberDao.getTotalContribution()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                familyNetWorth::setValue,
                                error -> message.setValue("Failed to calculate family net worth")));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
