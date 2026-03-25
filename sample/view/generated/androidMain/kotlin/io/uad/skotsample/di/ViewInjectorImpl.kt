package io.uad.skotsample.di

import io.uad.skotsample.screens.SplashVC
import io.uad.skotsample.screens.SplashViewProxy
import tech.skot.core.components.SKVisiblityListener

public class ViewInjectorImpl : ViewInjector {
  override fun splash(visibilityListener: SKVisiblityListener): SplashVC = SplashViewProxy(visibilityListener, )
}
