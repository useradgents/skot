package io.uad.skotsample.di

import io.uad.skotsample.screens.SplashVC
import io.uad.skotsample.screens.TabScreenVC
import tech.skot.core.components.SKVisiblityListener
import tech.skot.libraries.tabbar.SKBottomNavFrameVC

public interface ViewInjector {
  public fun splash(visibilityListener: SKVisiblityListener, bottomNav: SKBottomNavFrameVC): SplashVC

  public fun tabScreen(visibilityListener: SKVisiblityListener): TabScreenVC
}
