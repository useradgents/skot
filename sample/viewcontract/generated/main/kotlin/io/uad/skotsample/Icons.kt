@file:Suppress("PropertyName","SpellCheckingInspection","RedundantVisibilityModifier","FunctionName")

package io.uad.skotsample

import tech.skot.core.view.Icon

public interface Icons {
    public val ic_brand: Icon

    public fun `get`(key: String): Icon?
}
