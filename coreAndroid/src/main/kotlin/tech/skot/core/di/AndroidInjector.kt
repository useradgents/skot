package tech.skot.core.di

import android.app.Application

/**
 * Construit l'injecteur Android : enregistre l'[Application] puis charge [modules]. Porte le même
 * nom que la classe [BaseInjector] : un import de `BaseInjector` importe les deux, les appels
 * existants `BaseInjector(application, modules)` compilent sans changement.
 */
fun BaseInjector(
    androidApplication: Application,
    modules: List<Module<in BaseInjector>>,
): BaseInjector = BaseInjector(listOf(module<BaseInjector> { single<Application> { androidApplication } }) + modules)

/** L'[Application] enregistrée par `BaseInjector(application, modules)`. */
val BaseInjector.androidApplication: Application
    get() = get<Application>()
