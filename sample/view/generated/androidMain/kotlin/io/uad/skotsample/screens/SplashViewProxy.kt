package io.uad.skotsample.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.uad.skotsample.view.R
import io.uad.skotsample.view.databinding.SplashBinding
import java.lang.Class
import kotlin.Boolean
import kotlin.Int
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKScreenViewProxy
import tech.skot.core.components.SKVisiblityListener

public class SplashViewProxy(
  override val visibilityListener: SKVisiblityListener,
) : SKScreenViewProxy<SplashBinding>(),
    SplashVC {
  override val layoutId: Int = R.layout.splash

  override fun saveState() {
  }

  override fun getActivityClass(): Class<*> = io.uad.skotsample.android.BaseActivity::class.java

  override fun bindingOf(view: View): SplashBinding = SplashBinding.bind(view)

  override fun inflate(
    layoutInflater: LayoutInflater,
    parent: ViewGroup?,
    attachToParent: Boolean,
  ): SplashBinding = SplashBinding.inflate(layoutInflater, parent, attachToParent).also {
    it.root.tag = this.hashCode()
  }

  override fun bindTo(
    activity: SKActivity,
    fragment: Fragment?,
    binding: SplashBinding,
  ): SplashView = SplashView(this, activity, fragment, binding).apply {
  }
}

public interface SplashRAI
