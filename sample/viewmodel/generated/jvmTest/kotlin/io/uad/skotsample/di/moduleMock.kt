package io.uad.skotsample.di

import io.uad.skotsample.Colors
import io.uad.skotsample.ColorsMock
import io.uad.skotsample.Dimens
import io.uad.skotsample.DimensMock
import io.uad.skotsample.Fonts
import io.uad.skotsample.FontsMock
import io.uad.skotsample.Icons
import io.uad.skotsample.IconsMock
import io.uad.skotsample.Plurals
import io.uad.skotsample.PluralsMock
import io.uad.skotsample.Strings
import io.uad.skotsample.StringsMock
import io.uad.skotsample.Styles
import io.uad.skotsample.StylesMock
import io.uad.skotsample.view.Permissions
import io.uad.skotsample.view.PermissionsMock
import io.uad.skotsample.view.Transitions
import io.uad.skotsample.view.TransitionsMock
import tech.skot.core.di.BaseInjector
import tech.skot.core.di.CoreViewInjector
import tech.skot.core.di.CoreViewInjectorMock
import tech.skot.core.di.InjectorMock
import tech.skot.core.di.Module
import tech.skot.core.di.module
import tech.skot.core.view.Style

public val moduleMock: Module<InjectorMock> = module {
  single<CoreViewInjector> { CoreViewInjectorMock() }
  single<Strings> { StringsMock()}
  single<Plurals> { PluralsMock()}
  single<Icons> { IconsMock()}
  single<Colors> { ColorsMock()}
  single<Fonts> { FontsMock()}
  single<Styles> { StylesMock()}
  single<Dimens> { DimensMock()}
  single<ViewInjector> { ViewInjectorMock()}
  single<ModelInjector> { ModelInjectorMock()}
  single<Transitions> { TransitionsMock()}
  single<Permissions> { PermissionsMock()}
  byName["skFullScreenDialogStyle"] = Style("sk_fullScreen_dialog".hashCode())

}
