package com.rsinukov.shutterstockclient.dagger

import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers

@Module
object RxModule {

    @JvmStatic
    @Provides
    @IoScheduler
    fun provideIoScheduler() = Schedulers.io()
}
