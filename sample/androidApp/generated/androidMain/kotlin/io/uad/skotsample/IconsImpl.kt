@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import android.`annotation`.SuppressLint
import android.content.Context
import io.uad.skotsample.view.R
import tech.skot.core.view.Icon

public class IconsImpl(
  private val applicationContext: Context,
) : Icons {
  @SuppressLint(value = ["DiscouragedApi"])
  override fun `get`(key: String): Icon? {
    val id = applicationContext.resources.getIdentifier(key,"drawable",applicationContext.packageName)
    return if(id > 0) {
      Icon(id)
    }
    else {
      null
    }
  }
}
