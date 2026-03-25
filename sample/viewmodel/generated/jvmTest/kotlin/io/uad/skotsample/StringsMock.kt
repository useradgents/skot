@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

public class StringsMock : Strings {
  public var getReturnsNull: Boolean = false

  override fun `get`(key: String): String? = if (getReturnsNull) null else key
}
