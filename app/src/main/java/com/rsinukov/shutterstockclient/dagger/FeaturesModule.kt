package com.rsinukov.shutterstockclient.dagger

import com.rsinukov.shutterstockclient.search.SearchComponent
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module(
        subcomponents = [
            SearchComponent::class
        ]
)
interface FeaturesModule {

    @Binds
    @IntoMap
    @ShutterStockComponentKey(SearchComponent::class)
    fun bindsSearchComponent(builder: SearchComponent.Builder): ShutterStockComponentBuilder<*>
}
