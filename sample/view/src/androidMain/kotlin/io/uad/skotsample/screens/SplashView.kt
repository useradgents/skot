package io.uad.skotsample.screens

import androidx.fragment.app.Fragment
import io.uad.skotsample.view.databinding.SplashBinding
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKScreenView

public class SplashView(
  override val proxy: SplashViewProxy,
  activity: SKActivity,
  fragment: Fragment?,
  binding: SplashBinding,
) : SKScreenView<SplashBinding>(proxy, activity, fragment, binding),
    SplashRAI
