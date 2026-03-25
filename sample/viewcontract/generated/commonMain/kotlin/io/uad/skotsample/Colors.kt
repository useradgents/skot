@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import tech.skot.core.view.ColorRef

public interface Colors {
  public fun `get`(key: String): ColorRef?
}
