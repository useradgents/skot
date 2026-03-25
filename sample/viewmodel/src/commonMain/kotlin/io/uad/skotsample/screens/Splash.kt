package io.uad.skotsample.screens

import io.uad.skotsample.di.viewInjector
import tech.skot.core.view.ColorHex
import tech.skot.libraries.tabbar.SKBottomNavFrame
import tech.skot.libraries.tabbar.SKSimpleTab

public class Splash : SplashGen() {
    override val bottomNav = SKBottomNavFrame(SKBottomNavFrame.TabConf(SKSimpleTab(
        "tab1", selectedIcon = null, unSelectedIcon = null,
        selectedColor = null,
        unSelectedColor = ColorHex("#FF0000"),
        translateY = true,
        onTapped = null
    ),
        TabScreen()),
        SKBottomNavFrame.TabConf(SKSimpleTab(
            "tab2", selectedIcon = null, unSelectedIcon = null,
            selectedColor = null,
            unSelectedColor = ColorHex("#FF0000"),
            translateY = true,
            onTapped = null
        ),
            TabScreen()))

  final override val view: SplashVC = viewInjector.splash(
      visibilityListener = this,
      bottomNav = bottomNav.view
      )
}
