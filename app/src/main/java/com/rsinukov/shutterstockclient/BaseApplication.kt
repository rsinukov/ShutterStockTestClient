package com.rsinukov.shutterstockclient

import android.app.Application
import com.rsinukov.shutterstockclient.dagger.ShutterStockDagger
import timber.log.Timber

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initLogging()
        initDagger()
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initDagger() {
        ShutterStockDagger.getInstance(this)
                .initialize(this)
    }
}
