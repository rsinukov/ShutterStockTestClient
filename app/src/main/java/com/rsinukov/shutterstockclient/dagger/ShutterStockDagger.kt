package com.rsinukov.shutterstockclient.dagger

import android.app.Application
import android.content.Context
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@Suppress("UNCHECKED_CAST")
class ShutterStockDagger private constructor() {

    companion object {

        private val instance = ShutterStockDagger()

        @JvmStatic // param is here to ensure that no one tries to get instance from BL
        fun getInstance(@Suppress("UNUSED_PARAMETER") context: Context) = instance

        private val appComponentScope = AppComponent::class.java
    }

    private val components = HashMap<Any, ShutterStockComponent>()

    @Inject
    lateinit var builders: Map<Class<out ShutterStockComponent>, @JvmSuppressWildcards Provider<ShutterStockComponentBuilder<*>>>

    fun initialize(application: Application) {
        if (getComponent<AppComponent>(appComponentScope) != null) {
            throw IllegalStateException("Already initialized")
        }

        val appComponent = DaggerAppComponent.builder()
                .application(application)
                .build()

        putComponent(appComponentScope, appComponent)
        appComponent.inject(this)
    }

    fun appComponent(): AppComponent = get(appComponentScope)

    fun <T : ShutterStockComponent> builderFor(component: Class<T>): ShutterStockComponentBuilder<T> =
            builders[component]?.get() as ShutterStockComponentBuilder<T>?
                    ?: throw IllegalStateException("Component build for component ${component.simpleName} not found")

    operator fun <T : ShutterStockComponent> get(componentClass: Class<T>): T {
        return get(componentClass) { builderFor(componentClass).build() }
    }

    fun <T : ShutterStockComponent> get(scope: Any, creator: () -> T): T {
        getComponent<AppComponent>(appComponentScope)
                ?: throw IllegalStateException("No AppComponent - call VeonDagger.initialize()")

        val cached = getComponent<T>(scope)
        return if (cached == null) {
            val component = creator()
            putComponent(scope, component)
            component
        } else {
            cached
        }
    }

    fun remove(scope: Any) {
        if (scope == appComponentScope) {
            throw IllegalArgumentException("Trying to remove Application Component")
        }

        components.remove(scope)
    }

    private fun <T : ShutterStockComponent> getComponent(scope: Any): T? = components[scope] as T?

    private fun putComponent(scope: Any, component: ShutterStockComponent) {
        if (components[scope] != null) {
            Timber.e("Saving component for scope \"$scope\" while this scope already exists!")
        }
        components[scope] = component
    }
}
