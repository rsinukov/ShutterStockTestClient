package com.rsinukov.shutterstockclient.bl.network.dagger

import com.rsinukov.shutterstockclient.bl.network.ShutterStockSearchApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class ShutterStockApiModule {

    @Singleton
    @Provides
    fun provideShutterStockApi(retrofit: Retrofit): ShutterStockSearchApi {
        return retrofit.create(ShutterStockSearchApi::class.java)
    }
}
