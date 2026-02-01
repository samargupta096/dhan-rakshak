package com.dhanrakshak.data.remote.api;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

/**
 * CoinGecko API for cryptocurrency prices.
 * Free tier: 10-30 calls/minute
 */
public interface CryptoApi {

    String BASE_URL = "https://api.coingecko.com/api/v3/";

    /**
     * Get price for multiple coins in INR and USD.
     */
    @GET("simple/price")
    Single<Map<String, CoinPrice>> getPrices(
            @Query("ids") String coinIds, // comma-separated: bitcoin,ethereum,solana
            @Query("vs_currencies") String currencies, // "inr,usd"
            @Query("include_24hr_change") boolean include24hChange);

    /**
     * Get list of all coins.
     */
    @GET("coins/list")
    Single<List<CoinInfo>> getCoinsList();

    /**
     * Get detailed coin data.
     */
    @GET("coins/{id}")
    Single<CoinDetail> getCoinDetail(
            @retrofit2.http.Path("id") String coinId);

    /**
     * Search for coins.
     */
    @GET("search")
    Single<SearchResult> searchCoins(
            @Query("query") String query);

    // Response classes
    class CoinPrice {
        public double inr;
        public double usd;
        public double inr_24h_change;
        public double usd_24h_change;
    }

    class CoinInfo {
        public String id;
        public String symbol;
        public String name;
    }

    class CoinDetail {
        public String id;
        public String symbol;
        public String name;
        public Image image;
        public MarketData market_data;

        public static class Image {
            public String thumb;
            public String small;
            public String large;
        }

        public static class MarketData {
            public Map<String, Double> current_price;
            public Map<String, Double> market_cap;
            public Map<String, Double> price_change_percentage_24h_in_currency;
        }
    }

    class SearchResult {
        public List<CoinInfo> coins;
    }
}
