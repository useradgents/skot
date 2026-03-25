package io.uad.skotsample.di

import io.uad.skotsample.Colors
import io.uad.skotsample.Dimens
import io.uad.skotsample.Fonts
import io.uad.skotsample.Icons
import io.uad.skotsample.Plurals
import io.uad.skotsample.Strings
import io.uad.skotsample.Styles
import io.uad.skotsample.di.ViewInjector
import io.uad.skotsample.view.Permissions
import io.uad.skotsample.view.Transitions
import tech.skot.core.di.`get`

public val viewInjector: ViewInjector = get()

public val modelInjector: ModelInjector = get()

public val strings: Strings = get()

public val plurals: Plurals = get()

public val icons: Icons = get()

public val colors: Colors = get()

public val fonts: Fonts = get()

public val styles: Styles = get()

public val dimens: Dimens = get()

public val transitions: Transitions = get()

public val permissions: Permissions = get()
