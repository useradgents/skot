package io.uad.skotsample.screens

import tech.skot.core.components.SKOpens
import tech.skot.core.components.SKScreenVC
import tech.skot.libraries.tabbar.SKBottomNavFrameVC

@SKOpens([TabScreenVC::class])
public interface SplashVC : SKScreenVC {
    val bottomNav: SKBottomNavFrameVC
}


public interface TabScreenVC : SKScreenVC
