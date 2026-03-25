package io.uad.skotsample

import tech.skot.core.SKFeatureInitializer
import tech.skot.core.SKUri

public class SkotsampleInitializer(
  initialize: suspend () -> Unit,
  onDeepLink: (uri: SKUri, fromWebView: Boolean) -> Boolean,
  start: suspend (action: String?) -> Unit,
  resetToRoot: () -> Unit,
) : SKFeatureInitializer(initialize, onDeepLink, start, resetToRoot)
