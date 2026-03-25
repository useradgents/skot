@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import tech.skot.core.view.Icon

public class IconsMock : Icons {
  public var getReturnsNull: Boolean = false

  override fun `get`(key: String): Icon? = if (getReturnsNull) null else IconMock(key)
}
