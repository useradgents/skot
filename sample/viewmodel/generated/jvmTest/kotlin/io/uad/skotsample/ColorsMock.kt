@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import tech.skot.core.view.ColorRef

public class ColorsMock : Colors {
  public var getReturnsNull: Boolean = false

  override fun `get`(key: String): ColorRef? = if (getReturnsNull) null else ColorRef(key.hashCode())
}
