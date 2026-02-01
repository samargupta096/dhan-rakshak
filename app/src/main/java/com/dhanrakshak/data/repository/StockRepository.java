package com.dhanrakshak.data.repository;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.TransactionDao;
import com.dhanrakshak.data.local.entity.Asset;
import com.dhanrakshak.data.local.entity.Transaction;
import com.dhanrakshak.data.remote.api.StockApi;
import com.dhanrakshak.data.remote.dto.StockQuoteDto;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository for Stock operations.
 * Handles both local database and remote API calls.
 */
@Singleton
public class StockRepository {

    private final AssetDao assetDao;
    private final TransactionDao transactionDao;
    private final StockApi stockApi;

    @Inject
    public StockRepository(AssetDao assetDao, TransactionDao transactionDao, StockApi stockApi) {
        this.assetDao = assetDao;
        this.transactionDao = transactionDao;
        this.stockApi = stockApi;
    }

    /**
     * Get all stocks from local database.
     */
    public Flowable<List<Asset>> getAllStocks() {
        return assetDao.getAssetsByType("STOCK");
    }

    /**
     * Add a new stock holding.
     */
    public Completable addStock(String symbol, String name, double quantity, double avgPrice) {
        Asset stock = new Asset("STOCK", name, symbol.toUpperCase(), quantity, avgPrice);
        return assetDao.insert(stock);
    }

    /**
     * Update stock holding.
     */
    public Completable updateStock(Asset stock) {
        return assetDao.update(stock);
    }

    /**
     * Delete stock holding.
     */
    public Completable deleteStock(Asset stock) {
        return assetDao.delete(stock);
    }

    /**
     * Fetch live stock price from API.
     */
    public Single<StockQuoteDto> fetchStockPrice(String symbol) {
        return stockApi.getNseQuote(symbol.toUpperCase())
                .subscribeOn(Schedulers.io());
    }

    /**
     * Update stock price in database.
     */
    public Completable updateStockPrice(String symbol, double price) {
        return assetDao.updatePrice(symbol.toUpperCase(), price, System.currentTimeMillis());
    }

    /**
     * Refresh all stock prices from API.
     */
    public Completable refreshAllStockPrices(List<Asset> stocks) {
        return Flowable.fromIterable(stocks)
                .flatMapCompletable(stock -> fetchStockPrice(stock.getIdentifier())
                        .flatMapCompletable(quote -> updateStockPrice(stock.getIdentifier(), quote.getLastPrice()))
                        .onErrorComplete() // Continue on error for individual stock
                );
    }

    /**
     * Get stock by symbol.
     */
    public Single<Asset> getStockBySymbol(String symbol) {
        return assetDao.getAllAssets()
                .firstOrError()
                .flatMap(assets -> {
                    for (Asset asset : assets) {
                        if (asset.getAssetType().equals("STOCK") &&
                                asset.getIdentifier().equalsIgnoreCase(symbol)) {
                            return Single.just(asset);
                        }
                    }
                    return Single.error(new Exception("Stock not found"));
                });
    }

    /**
     * Add buy transaction for a stock.
     */
    public Completable addBuyTransaction(long assetId, double amount, double units,
            double pricePerUnit, long date) {
        Transaction transaction = new Transaction(assetId, "BUY", amount, units, pricePerUnit, date);
        return transactionDao.insert(transaction);
    }

    /**
     * Add sell transaction for a stock.
     */
    public Completable addSellTransaction(long assetId, double amount, double units,
            double pricePerUnit, long date) {
        Transaction transaction = new Transaction(assetId, "SELL", amount, units, pricePerUnit, date);
        return transactionDao.insert(transaction);
    }

    /**
     * Get transaction history for a stock.
     */
    public Flowable<List<Transaction>> getTransactionHistory(long assetId) {
        return transactionDao.getTransactionsByAsset(assetId);
    }
}
