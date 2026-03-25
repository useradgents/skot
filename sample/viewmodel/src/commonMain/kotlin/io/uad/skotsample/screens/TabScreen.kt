package io.uad.skotsample.screens

import io.uad.skotsample.di.viewInjector

public class TabScreen : TabScreenGen() {
  final override val view: TabScreenVC = viewInjector.tabScreen(
      visibilityListener = this,
      )
}
