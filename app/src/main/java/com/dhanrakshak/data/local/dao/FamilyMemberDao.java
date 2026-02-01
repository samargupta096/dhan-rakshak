package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.FamilyMember;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface FamilyMemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(FamilyMember member);

    @Update
    Completable update(FamilyMember member);

    @Delete
    Completable delete(FamilyMember member);

    @Query("SELECT * FROM family_members ORDER BY isOwner DESC, createdAt ASC")
    Flowable<List<FamilyMember>> getAllMembers();

    @Query("SELECT * FROM family_members WHERE id = :id")
    Single<FamilyMember> getById(long id);

    @Query("SELECT * FROM family_members WHERE isOwner = 1 LIMIT 1")
    Single<FamilyMember> getOwner();

    @Query("SELECT * FROM family_members WHERE relationship = :relationship")
    Flowable<List<FamilyMember>> getByRelationship(String relationship);

    @Query("SELECT SUM(contributionToNetWorth) FROM family_members")
    Single<Double> getTotalFamilyNetWorth();

    @Query("SELECT COUNT(*) FROM family_members")
    Single<Integer> getMemberCount();
}
