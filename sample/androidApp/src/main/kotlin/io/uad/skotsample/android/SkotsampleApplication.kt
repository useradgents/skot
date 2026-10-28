package io.uad.skotsample.android

import android.app.Application
import io.uad.skotsample.di.generatedAppModules
import kotlin.Unit
import tech.skot.core.di.BaseInjector
import tech.skot.core.di.injector
import timber.log.Timber

public class SkotsampleApplication : Application() {
  public override fun onCreate(): Unit {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
    injector = BaseInjector(this,
                        generatedAppModules +
                        listOf(
                        ))
  }
}
