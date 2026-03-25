package io.uad.skotsample.di

import io.uad.skotsample.Colors
import io.uad.skotsample.ColorsImpl
import io.uad.skotsample.Dimens
import io.uad.skotsample.DimensImpl
import io.uad.skotsample.Fonts
import io.uad.skotsample.FontsImpl
import io.uad.skotsample.Icons
import io.uad.skotsample.IconsImpl
import io.uad.skotsample.Plurals
import io.uad.skotsample.PluralsImpl
import io.uad.skotsample.SkotsampleInitializer
import io.uad.skotsample.Strings
import io.uad.skotsample.StringsImpl
import io.uad.skotsample.Styles
import io.uad.skotsample.StylesImpl
import io.uad.skotsample.onDeeplink
import io.uad.skotsample.start
import io.uad.skotsample.view.Permissions
import io.uad.skotsample.view.PermissionsImpl
import io.uad.skotsample.view.Transitions
import io.uad.skotsample.view.TransitionsImpl
import kotlin.collections.List
import tech.skot.core.components.SKRootStack
import tech.skot.core.di.BaseInjector
import tech.skot.core.di.Module
import tech.skot.core.di.coreViewModule
import tech.skot.core.di.module
import tech.skot.di.modelFrameworkModule

public val generatedAppModules: List<Module<BaseInjector>> = listOf(module {
  single<Strings> { StringsImpl(androidApplication)}
  single<Plurals> { PluralsImpl(androidApplication)}
  single<Icons> { IconsImpl(androidApplication)}
  single<Colors> { ColorsImpl(androidApplication)}
  single<Fonts> { FontsImpl()}
  single<Styles> { StylesImpl()}
  single<Dimens> { DimensImpl()}
  single<ViewInjector> { ViewInjectorImpl()}
  single<ModelInjector> { ModelInjectorImpl()}
  single<Transitions> { TransitionsImpl()}
  single<Permissions> { PermissionsImpl()}
  single {
    SkotsampleInitializer(
    initialize =  {
      initializeView(androidApplication)
    }
    ,
    onDeepLink = { uri, fromWebView ->
      onDeeplink(uri, fromWebView)
    }
    ,
    start = { action ->
      start(startModel(action), action)
    }
    ,
    resetToRoot =  {
      SKRootStack.resetToRoot()
    }
    )
  }
}
,
modelFrameworkModule,
coreViewModule,
)
