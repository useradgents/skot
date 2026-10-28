package io.uad.skotsample.di

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar
import io.uad.skotsample.android.RootActivity
import kotlin.Unit
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKComponentView

public suspend fun initializeView(applicationContext: Context): Unit {
  SKActivity.launchActivityClass = RootActivity::class.java
  SKComponentView.displayMessage = { message ->
          Snackbar.make(activity.window.decorView, message.content, Snackbar.LENGTH_LONG)
              .apply {
                  view.apply {
                      (layoutParams as? FrameLayout.LayoutParams)?.let {
                          it.gravity = Gravity.TOP
                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                              it.topMargin =
      activity.window?.decorView?.rootWindowInsets?.systemWindowInsetTop
                                  ?: 0
                          }

                          layoutParams = it
                      }
                  }
                  show()
              }
      }
}
