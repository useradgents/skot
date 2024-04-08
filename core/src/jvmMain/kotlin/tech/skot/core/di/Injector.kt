@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core.di

actual class BaseInjector(modules: List<Module<BaseInjector>>) :
    Injector<BaseInjector>(modules) {
    actual override val context = this
}
