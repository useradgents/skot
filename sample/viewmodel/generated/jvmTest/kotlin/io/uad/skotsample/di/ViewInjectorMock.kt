package io.uad.skotsample.di

import io.uad.skotsample.screens.SplashVC
import io.uad.skotsample.screens.SplashViewMock
import tech.skot.core.components.SKVisiblityListener

public class ViewInjectorMock : ViewInjector {
  override fun splash(visibilityListener: SKVisiblityListener): SplashVC = SplashViewMock()
}
