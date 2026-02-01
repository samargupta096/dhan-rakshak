package com.dhanrakshak.di;

import android.content.Context;

import com.dhanrakshak.ai.insights.AiFinanceInsightsEngine;
import com.dhanrakshak.ai.sms.GeminiNanoClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module for AI-related dependencies.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AiModule {

    @Provides
    @Singleton
    public GeminiNanoClient provideGeminiNanoClient(@ApplicationContext Context context) {
        return new GeminiNanoClient(context);
    }

    @Provides
    @Singleton
    public AiFinanceInsightsEngine provideAiFinanceInsightsEngine(@ApplicationContext Context context) {
        return new AiFinanceInsightsEngine(context);
    }
}
