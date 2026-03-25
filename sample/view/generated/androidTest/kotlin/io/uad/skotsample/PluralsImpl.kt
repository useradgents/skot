@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import android.content.Context
import io.uad.skotsample.view.R

public class PluralsImpl(
  private val applicationContext: Context,
) {
  private fun compute(
    pluralId: Int,
    quantity: Int,
    vararg formatArgs: Any,
  ): String = if (formatArgs.isEmpty()) {
    applicationContext.resources.getQuantityString(pluralId, quantity)
  }
  else {
    applicationContext.resources.getQuantityString(pluralId, quantity, *formatArgs)
  }
}
