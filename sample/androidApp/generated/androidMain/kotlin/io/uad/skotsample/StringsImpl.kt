@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import android.`annotation`.SuppressLint
import android.content.Context
import io.uad.skotsample.view.R

public class StringsImpl(
  private val applicationContext: Context,
) : Strings {
  private fun `get`(strId: Int): String = applicationContext.getString(strId)

  @SuppressLint(value = ["DiscouragedApi"])
  override fun `get`(key: String): String? {
    val id = applicationContext.resources.getIdentifier(key,"string",applicationContext.packageName)
    return if(id > 0) {
      get(id)
    }
    else {
      null
    }
  }
}
