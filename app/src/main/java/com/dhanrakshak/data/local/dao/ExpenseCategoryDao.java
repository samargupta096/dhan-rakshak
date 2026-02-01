package com.dhanrakshak.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dhanrakshak.data.local.entity.ExpenseCategory;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Data Access Object for ExpenseCategory entity.
 */
@Dao
public interface ExpenseCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(ExpenseCategory category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<ExpenseCategory> categories);

    @Update
    Completable update(ExpenseCategory category);

    @Delete
    Completable delete(ExpenseCategory category);

    @Query("SELECT * FROM expense_categories WHERE id = :id")
    Single<ExpenseCategory> getById(long id);

    @Query("SELECT * FROM expense_categories WHERE name = :name")
    Single<ExpenseCategory> getByName(String name);

    @Query("SELECT * FROM expense_categories WHERE parentId = 0 ORDER BY displayOrder")
    Flowable<List<ExpenseCategory>> getTopLevelCategories();

    @Query("SELECT * FROM expense_categories WHERE parentId = :parentId ORDER BY displayOrder")
    Flowable<List<ExpenseCategory>> getSubCategories(long parentId);

    @Query("SELECT * FROM expense_categories ORDER BY displayOrder")
    Flowable<List<ExpenseCategory>> getAllCategories();

    @Query("SELECT * FROM expense_categories WHERE isCustom = 1 ORDER BY displayOrder")
    Flowable<List<ExpenseCategory>> getCustomCategories();

    @Query("SELECT COUNT(*) FROM expense_categories")
    Single<Integer> getCategoryCount();
}
