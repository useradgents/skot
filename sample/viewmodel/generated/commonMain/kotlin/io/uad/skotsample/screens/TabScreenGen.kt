@file:Suppress("REDUNDANT_PROJECTION","RedundantVisibilityModifier")

package io.uad.skotsample.screens

import io.uad.skotsample.di.modelInjector
import tech.skot.core.components.SKScreen

public abstract class TabScreenGen : SKScreen<TabScreenVC>() {
  override fun onRemove() {
    super.onRemove()
  }
}
