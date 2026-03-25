@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import android.`annotation`.SuppressLint
import android.content.Context
import io.uad.skotsample.view.R
import tech.skot.core.view.ColorRef

public class ColorsImpl(
  private val applicationContext: Context,
) : Colors {
  @SuppressLint(value = ["DiscouragedApi"])
  override fun `get`(key: String): ColorRef? {
    val id = applicationContext.resources.getIdentifier(key,"color",applicationContext.packageName)
    return if(id > 0) {
      ColorRef(id)
    }
    else {
      null
    }
  }
}
