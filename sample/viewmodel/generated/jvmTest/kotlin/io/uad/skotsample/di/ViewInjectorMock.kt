package io.uad.skotsample.di

import io.uad.skotsample.screens.SplashVC
import io.uad.skotsample.screens.SplashViewMock
import io.uad.skotsample.screens.TabScreenVC
import io.uad.skotsample.screens.TabScreenViewMock
import tech.skot.core.components.SKVisiblityListener
import tech.skot.libraries.tabbar.SKBottomNavFrameVC
import tech.skot.libraries.tabbar.SKBottomNavFrameViewMock

public class ViewInjectorMock : ViewInjector {
  override fun splash(visibilityListener: SKVisiblityListener, bottomNav: SKBottomNavFrameVC): SplashVC = SplashViewMock(bottomNav as SKBottomNavFrameViewMock)

  override fun tabScreen(visibilityListener: SKVisiblityListener): TabScreenVC = TabScreenViewMock()
}
