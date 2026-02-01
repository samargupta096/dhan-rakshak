package com.dhanrakshak.data.remote.api;

import com.dhanrakshak.data.remote.dto.StockQuoteDto;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit API for Indian Stock Market data.
 * Uses free Indian Stock Market API.
 */
public interface StockApi {

    /**
     * Get stock quote from NSE.
     * 
     * @param symbol Stock symbol (e.g., "RELIANCE", "INFY")
     */
    @GET("api/nse/{symbol}")
    Single<StockQuoteDto> getNseQuote(@Path("symbol") String symbol);

    /**
     * Get stock quote from BSE.
     * 
     * @param symbol Stock symbol
     */
    @GET("api/bse/{symbol}")
    Single<StockQuoteDto> getBseQuote(@Path("symbol") String symbol);
}
