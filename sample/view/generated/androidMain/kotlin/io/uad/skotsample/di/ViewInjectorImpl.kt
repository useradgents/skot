package io.uad.skotsample.di

import io.uad.skotsample.screens.SplashVC
import io.uad.skotsample.screens.SplashViewProxy
import io.uad.skotsample.screens.TabScreenVC
import io.uad.skotsample.screens.TabScreenViewProxy
import tech.skot.core.components.SKVisiblityListener
import tech.skot.libraries.tabbar.SKBottomNavFrameVC
import tech.skot.libraries.tabbar.SKBottomNavFrameViewProxy

public class ViewInjectorImpl : ViewInjector {
  override fun splash(visibilityListener: SKVisiblityListener, bottomNav: SKBottomNavFrameVC): SplashVC = SplashViewProxy(visibilityListener, bottomNav as SKBottomNavFrameViewProxy)

  override fun tabScreen(visibilityListener: SKVisiblityListener): TabScreenVC = TabScreenViewProxy(visibilityListener, )
}
