package com.financeapp.core.config

import com.financeapp.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = object : AppConfig {
        override val defaultExchangeApiKey: String = BuildConfig.EXCHANGE_API_KEY
    }
}
