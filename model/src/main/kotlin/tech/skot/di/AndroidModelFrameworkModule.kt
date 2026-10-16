package tech.skot.di

import android.content.Context
import tech.skot.core.di.BaseInjector
import tech.skot.core.di.module
import tech.skot.model.*

actual val modelFrameworkModule =
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
