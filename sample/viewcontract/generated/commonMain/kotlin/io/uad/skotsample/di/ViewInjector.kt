package io.uad.skotsample.di

import io.uad.skotsample.screens.SplashVC
import tech.skot.core.components.SKVisiblityListener

public interface ViewInjector {
  public fun splash(visibilityListener: SKVisiblityListener): SplashVC
}
