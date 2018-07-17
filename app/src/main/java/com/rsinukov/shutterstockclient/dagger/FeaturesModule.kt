package com.rsinukov.shutterstockclient.dagger

import com.rsinukov.shutterstockclient.features.templateslist.SearchComponent
import dagger.Module

@Module(
        subcomponents = [
            SearchComponent::class
        ]
)
interface FeaturesModule
