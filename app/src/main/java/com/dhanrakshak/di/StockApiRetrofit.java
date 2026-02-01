package com.dhanrakshak.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Qualifier for Stock API Retrofit instance.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface StockApiRetrofit {
}
