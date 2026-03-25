package io.uad.skotsample.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.uad.skotsample.view.R
import io.uad.skotsample.view.databinding.TabScreenBinding
import java.lang.Class
import kotlin.Boolean
import kotlin.Int
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKScreenViewProxy
import tech.skot.core.components.SKVisiblityListener

public class TabScreenViewProxy(
  override val visibilityListener: SKVisiblityListener,
) : SKScreenViewProxy<TabScreenBinding>(),
    TabScreenVC {
  override val layoutId: Int = R.layout.tab_screen

  override fun saveState() {
  }

  override fun getActivityClass(): Class<*> = io.uad.skotsample.android.BaseActivity::class.java

  override fun bindingOf(view: View): TabScreenBinding = TabScreenBinding.bind(view)

  override fun inflate(
    layoutInflater: LayoutInflater,
    parent: ViewGroup?,
    attachToParent: Boolean,
  ): TabScreenBinding = TabScreenBinding.inflate(layoutInflater, parent, attachToParent).also {
    it.root.tag = this.hashCode()
  }

  override fun bindTo(
    activity: SKActivity,
    fragment: Fragment?,
    binding: TabScreenBinding,
  ): TabScreenView = TabScreenView(this, activity, fragment, binding).apply {
  }
}

public interface TabScreenRAI
