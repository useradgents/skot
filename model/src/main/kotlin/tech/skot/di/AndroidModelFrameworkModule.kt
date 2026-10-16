package tech.skot.di

import android.content.Context
import tech.skot.core.di.BaseInjector
import tech.skot.core.di.Module
import tech.skot.core.di.androidApplication
import tech.skot.core.di.module
import tech.skot.model.*

/**
 * Module framework du model pour l'application Android. Le module des tests unitaires garde le nom
 * `modelFrameworkModule` dans l'artefact modelTests (les `TestModel` des clients l'importent).
 */
val androidModelFrameworkModule: Module<BaseInjector> =
    module<BaseInjector> {

        single<Context> {
            androidApplication
        }

        factory<PersistorFactory> {
            object : PersistorFactory {
                override fun getPersistor(
                    dbFileName: String,
                    cache: Boolean,
                ): SKPersistor = AndroidSKPersistor(androidApplication, dbFileName, cache)
            }
        }

        factory<Prefs> {
            AndroidPrefs(androidApplication)
        }

        factory<Device> {
            AndroidDevice()
        }
    }
