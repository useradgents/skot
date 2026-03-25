@file:Suppress("REDUNDANT_PROJECTION","RedundantVisibilityModifier")

package io.uad.skotsample.screens

import io.uad.skotsample.di.modelInjector
import tech.skot.core.components.SKComponent
import tech.skot.core.components.SKComponentVC
import tech.skot.core.components.SKScreen

public abstract class SplashGen : SKScreen<SplashVC>() {
  protected abstract val bottomNav: SKComponent<out SKComponentVC>

  override fun onRemove() {
    bottomNav.onRemove()
    super.onRemove()
  }
}
