package com.dhanrakshak.data.repository;

import android.util.Log;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.TransactionDao;
import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class SyncRepository {

    private static final String TAG = "SyncRepository";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_ASSETS = "assets";
    private static final String COLLECTION_TRANSACTIONS = "transactions";

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final AssetDao assetDao;
    private final TransactionDao transactionDao;

    @Inject
    public SyncRepository(FirebaseAuth auth, FirebaseFirestore firestore, AssetDao assetDao,
            TransactionDao transactionDao) {
        this.auth = auth;
        this.firestore = firestore;
        this.assetDao = assetDao;
        this.transactionDao = transactionDao;
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Uploads local data to Firestore (One-way Backup for MVP).
     * Overwrites remote collections for the current user.
     */
    public Completable syncToCloud() {
        return Completable.create(emitter -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                emitter.onError(new Exception("User not logged in"));
                return;
            }

            String userId = user.getUid();
            Log.d(TAG, "Starting sync for user: " + userId);

            // Fetch local data (Blocking for simplicity in this Stream, ideally fully
            // reactive)
            // Note: In real app, consider pagination or incremental sync.
            // Using RxJava blockingGet() here safely within subscribeOn(IO).

            try {
                // 1. Sync Assets
                List<Asset> assets = assetDao.getAllAssets().blockingFirst();
                if (!assets.isEmpty()) {
                    WriteBatch batch = firestore.batch();
                    CollectionReference assetsRef = firestore.collection(COLLECTION_USERS).document(userId)
                            .collection(COLLECTION_ASSETS);

                    for (Asset asset : assets) {
                        // Use Asset ID as Document ID
                        batch.set(assetsRef.document(String.valueOf(asset.getId())), asset);
                    }
                    batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to sync assets", e));
                    Log.d(TAG, "Synced " + assets.size() + " assets.");
                }

                // 2. Sync Transactions
                List<Transaction> transactions = transactionDao.getAllTransactions().blockingFirst();
                if (!transactions.isEmpty()) {
                    // Firestore batch limit is 500. For MVP assuming < 500 or splitting needed.
                    // Simple split logic:
                    syncTransactionsInBatches(userId, transactions);
                }

                emitter.onComplete();

            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private void syncTransactionsInBatches(String userId, List<Transaction> transactions) {
        final int BATCH_SIZE = 450;
        for (int i = 0; i < transactions.size(); i += BATCH_SIZE) {
            int end = Math.min(transactions.size(), i + BATCH_SIZE);
            List<Transaction> batchList = transactions.subList(i, end);

            WriteBatch batch = firestore.batch();
            CollectionReference transRef = firestore.collection(COLLECTION_USERS).document(userId)
                    .collection(COLLECTION_TRANSACTIONS);

            for (Transaction t : batchList) {
                batch.set(transRef.document(String.valueOf(t.getId())), t);
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Batch transaction sync success"))
                    .addOnFailureListener(e -> Log.e(TAG, "Batch transaction sync failed", e));
        }
    }

    public Completable signInWithGoogle(String idToken) {
        return Completable.create(emitter -> {
            com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider
                    .getCredential(idToken, null);
            auth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }
}
