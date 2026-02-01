package com.dhanrakshak.di;

import com.dhanrakshak.data.remote.api.AmfiApi;
import com.dhanrakshak.data.remote.api.StockApi;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Hilt module for network dependency injection.
 */
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String STOCK_API_BASE_URL = BuildConfig.STOCK_API_URL;
    private static final String AMFI_BASE_URL = BuildConfig.AMFI_API_URL;

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    @StockApiRetrofit
    public Retrofit provideStockRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(STOCK_API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    @AmfiApiRetrofit
    public Retrofit provideAmfiRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(AMFI_BASE_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public StockApi provideStockApi(@StockApiRetrofit Retrofit retrofit) {
        return retrofit.create(StockApi.class);
    }

    @Provides
    @Singleton
    public AmfiApi provideAmfiApi(@AmfiApiRetrofit Retrofit retrofit) {
        return retrofit.create(AmfiApi.class);
    }
}
