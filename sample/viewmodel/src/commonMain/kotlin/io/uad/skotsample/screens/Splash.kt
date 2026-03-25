package io.uad.skotsample.screens

import io.uad.skotsample.di.viewInjector

public class Splash : SplashGen() {
  final override val view: SplashVC = viewInjector.splash(
      visibilityListener = this,
      )
}
