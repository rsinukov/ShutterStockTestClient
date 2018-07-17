package com.rsinukov.shutterstockclient.search

import com.rsinukov.shutterstockclient.dagger.ShutterStockComponent
import com.rsinukov.shutterstockclient.dagger.ShutterStockComponentBuilder
import com.rsinukov.shutterstockclient.search.bl.SearchPresenter
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SearchScope

@Subcomponent
@SearchScope
interface SearchComponent : ShutterStockComponent {

    fun providePresenter(): SearchPresenter

    @Subcomponent.Builder
    interface Builder : ShutterStockComponentBuilder<SearchComponent> {
        override fun build(): SearchComponent
    }
}
