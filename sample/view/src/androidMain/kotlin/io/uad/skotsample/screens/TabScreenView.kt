package io.uad.skotsample.screens

import androidx.fragment.app.Fragment
import io.uad.skotsample.view.databinding.TabScreenBinding
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKScreenView

public class TabScreenView(
  override val proxy: TabScreenViewProxy,
  activity: SKActivity,
  fragment: Fragment?,
  binding: TabScreenBinding,
) : SKScreenView<TabScreenBinding>(proxy, activity, fragment, binding),
    TabScreenRAI
