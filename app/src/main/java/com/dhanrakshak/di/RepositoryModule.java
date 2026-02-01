package com.dhanrakshak.di;

import com.dhanrakshak.data.local.dao.AssetDao;
import com.dhanrakshak.data.local.dao.BankAccountDao;
import com.dhanrakshak.data.local.dao.FixedDepositDao;
import com.dhanrakshak.data.local.dao.MutualFundSchemeDao;
import com.dhanrakshak.data.local.dao.RecurringDepositDao;
import com.dhanrakshak.data.local.dao.SmsTransactionDao;
import com.dhanrakshak.data.local.dao.TransactionDao;
import com.dhanrakshak.data.remote.api.AmfiApi;
import com.dhanrakshak.data.remote.api.StockApi;
import com.dhanrakshak.data.repository.BankRepository;
import com.dhanrakshak.data.repository.DepositRepository;
import com.dhanrakshak.data.repository.MutualFundRepository;
import com.dhanrakshak.data.repository.StockRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module for Repository dependencies.
 */
@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public StockRepository provideStockRepository(AssetDao assetDao, TransactionDao transactionDao,
            StockApi stockApi) {
        return new StockRepository(assetDao, transactionDao, stockApi);
    }

    @Provides
    @Singleton
    public MutualFundRepository provideMutualFundRepository(AssetDao assetDao,
            TransactionDao transactionDao,
            MutualFundSchemeDao schemeDao,
            AmfiApi amfiApi) {
        return new MutualFundRepository(assetDao, transactionDao, schemeDao, amfiApi);
    }

    @Provides
    @Singleton
    public BankRepository provideBankRepository(BankAccountDao bankAccountDao,
            SmsTransactionDao smsTransactionDao) {
        return new BankRepository(bankAccountDao, smsTransactionDao);
    }

    @Provides
    @Singleton
    public DepositRepository provideDepositRepository(FixedDepositDao fdDao,
            RecurringDepositDao rdDao) {
        return new DepositRepository(fdDao, rdDao);
    }
}
