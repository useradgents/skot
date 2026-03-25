package io.uad.skotsample.screens

import tech.skot.core.components.SKScreenViewMock
import tech.skot.libraries.tabbar.SKBottomNavFrameVC

public class SplashViewMock(
  override val bottomNav: SKBottomNavFrameVC,
) : SKScreenViewMock(),
    SplashVC
