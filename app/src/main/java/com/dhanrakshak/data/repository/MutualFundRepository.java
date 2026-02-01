package com.dhanrakshak.data.repository;

import android.util.Log;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.MutualFundSchemeDao;
import com.dhanrakshak.data.local.dao.TransactionDao;
import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.MutualFundScheme;
import com.dhanrakshak.data.local.entity.Transaction;
import com.dhanrakshak.data.remote.api.AmfiApi;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository for Mutual Fund operations.
 * Fetches NAV data from AMFI and manages local holdings.
 */
@Singleton
public class MutualFundRepository {

    private static final String TAG = "MutualFundRepository";

    private final AssetDao assetDao;
    private final TransactionDao transactionDao;
    private final MutualFundSchemeDao schemeDao;
    private final AmfiApi amfiApi;

    @Inject
    public MutualFundRepository(AssetDao assetDao, TransactionDao transactionDao,
            MutualFundSchemeDao schemeDao, AmfiApi amfiApi) {
        this.assetDao = assetDao;
        this.transactionDao = transactionDao;
        this.schemeDao = schemeDao;
        this.amfiApi = amfiApi;
    }

    /**
     * Get all mutual fund holdings.
     */
    public Flowable<List<Asset>> getAllMutualFunds() {
        return assetDao.getAssetsByType("MUTUAL_FUND");
    }

    /**
     * Add a new mutual fund holding.
     */
    public Completable addMutualFund(long schemeCode, String schemeName, double units, double avgNav) {
        Asset mf = new Asset("MUTUAL_FUND", schemeName, String.valueOf(schemeCode), units, avgNav);
        return assetDao.insert(mf);
    }

    /**
     * Update mutual fund holding.
     */
    public Completable updateMutualFund(Asset mf) {
        return assetDao.update(mf);
    }

    /**
     * Delete mutual fund holding.
     */
    public Completable deleteMutualFund(Asset mf) {
        return assetDao.delete(mf);
    }

    /**
     * Search mutual fund schemes by name.
     */
    public Flowable<List<MutualFundScheme>> searchSchemes(String query) {
        return schemeDao.searchSchemes(query);
    }

    /**
     * Get scheme by code.
     */
    public Single<MutualFundScheme> getSchemeByCode(long schemeCode) {
        return schemeDao.getBySchemeCode(schemeCode);
    }

    /**
     * Fetch and parse all NAV data from AMFI.
     * This downloads the complete NAV file and updates local database.
     */
    public Completable syncNavData() {
        return amfiApi.getAllNavData()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(this::parseAndSaveNavData);
    }

    /**
     * Parse AMFI NAV text file and save to database.
     * Format: Scheme Code;ISIN Div Payout/Growth;ISIN Div Reinvestment;Scheme
     * Name;NAV;Date
     */
    private Completable parseAndSaveNavData(String navText) {
        return Completable.fromAction(() -> {
            List<MutualFundScheme> schemes = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new StringReader(navText));
            String line;
            String currentAmcName = "";

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty())
                    continue;

                // Check if it's an AMC header (doesn't contain ';')
                if (!line.contains(";")) {
                    if (!line.startsWith("Scheme") && !line.startsWith("Open") &&
                            !line.startsWith("Close")) {
                        currentAmcName = line;
                    }
                    continue;
                }

                // Parse scheme data
                String[] parts = line.split(";");
                if (parts.length >= 5) {
                    try {
                        long schemeCode = Long.parseLong(parts[0].trim());
                        String isin = parts[1].trim();
                        String schemeName = parts[3].trim();
                        double nav = Double.parseDouble(parts[4].trim());

                        MutualFundScheme scheme = new MutualFundScheme(
                                schemeCode, schemeName, isin, nav);
                        scheme.setAmcName(currentAmcName);
                        scheme.setNavDate(System.currentTimeMillis());

                        schemes.add(scheme);

                        // Batch insert every 500 schemes
                        if (schemes.size() >= 500) {
                            schemeDao.insertAll(schemes).blockingAwait();
                            schemes.clear();
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                    }
                }
            }

            // Insert remaining schemes
            if (!schemes.isEmpty()) {
                schemeDao.insertAll(schemes).blockingAwait();
            }

            Log.d(TAG, "NAV data sync complete");
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Update NAV for a specific scheme.
     */
    public Completable updateSchemeNav(long schemeCode, double nav) {
        return schemeDao.updateNav(schemeCode, nav, System.currentTimeMillis(),
                System.currentTimeMillis());
    }

    /**
     * Update all mutual fund holding values based on latest NAV.
     */
    public Completable refreshHoldingValues() {
        return getAllMutualFunds()
                .firstOrError()
                .flatMapCompletable(holdings -> Flowable.fromIterable(holdings)
                        .flatMapCompletable(holding -> {
                            long schemeCode = Long.parseLong(holding.getIdentifier());
                            return getSchemeByCode(schemeCode)
                                    .flatMapCompletable(scheme -> {
                                        holding.setCurrentPrice(scheme.getLatestNav());
                                        return assetDao.update(holding);
                                    })
                                    .onErrorComplete();
                        }));
    }

    /**
     * Add SIP transaction.
     */
    public Completable addSipTransaction(long assetId, double amount, double units,
            double nav, long date) {
        Transaction transaction = new Transaction(assetId, "SIP", amount, units, nav, date);
        return transactionDao.insert(transaction);
    }

    /**
     * Get scheme count to check if data is synced.
     */
    public Single<Integer> getSchemeCount() {
        return schemeDao.getSchemeCount();
    }
}
