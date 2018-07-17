package com.rsinukov.shutterstockclient.dagger

import dagger.MapKey
import kotlin.reflect.KClass

interface ShutterStockComponent

interface ShutterStockComponentBuilder<out T : ShutterStockComponent> {
    fun build(): T
}

@MapKey
annotation class ShutterStockComponentKey(val value: KClass<out ShutterStockComponent>)
