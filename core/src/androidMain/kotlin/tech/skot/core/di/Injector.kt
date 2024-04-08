@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core.di

import android.app.Application

actual class BaseInjector(val androidApplication: Application, modules: List<Module<in BaseInjector>>) :
    Injector<BaseInjector>(modules) {
    actual override val context = this
}
