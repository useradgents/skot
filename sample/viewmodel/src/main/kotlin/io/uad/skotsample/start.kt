package io.uad.skotsample

import io.uad.skotsample.screens.Splash
import tech.skot.core.SKUri
import tech.skot.core.components.SKRootStack

public fun start(appStatus: Any?, action: String?) {
  SKRootStack.content = Splash()
}

public fun onDeeplink(uri: SKUri, fromWebView: Boolean): Boolean = false
