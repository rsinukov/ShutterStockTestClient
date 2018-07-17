package com.rsinukov.shutterstockclient.dagger

import android.app.Application
import com.rsinukov.shutterstockclient.bl.network.dagger.ShutterStockApiModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [
    AppModule::class,
    ClientModule::class,
    FeaturesModule::class,
    RxModule::class,
    ShutterStockApiModule::class
])
@Singleton
interface AppComponent : ShutterStockComponent {

    fun inject(ShutterStockDagger: ShutterStockDagger)

    @Component.Builder
    interface Builder : ShutterStockComponentBuilder<AppComponent> {

        @BindsInstance
        fun application(application: Application): Builder

        override fun build(): AppComponent
    }
}
