package com.dhanrakshak.data.remote.api;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;

/**
 * Retrofit API for AMFI NAV data.
 * Fetches daily NAV file for all mutual fund schemes.
 */
public interface AmfiApi {

    /**
     * Get all NAV data as plain text.
     * Format: Scheme Code;ISIN Div Payout/Growth;ISIN Div Reinvestment;Scheme
     * Name;NAV;Date
     */
    @GET("spages/NAVAll.txt")
    Single<String> getAllNavData();
}
