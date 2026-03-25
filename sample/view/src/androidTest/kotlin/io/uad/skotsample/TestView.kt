package io.uad.skotsample

import androidx.test.core.app.ApplicationProvider
import io.uad.skotsample.di.initializeView
import kotlinx.coroutines.runBlocking
import org.junit.Before
import tech.skot.view.tests.SKTestView

public abstract class TestView : SKTestView() {
  public val strings: StringsImpl = StringsImpl(ApplicationProvider.getApplicationContext())

  public val plurals: PluralsImpl = PluralsImpl(ApplicationProvider.getApplicationContext())

  public val icons: IconsImpl = IconsImpl(ApplicationProvider.getApplicationContext())

  public val colors: ColorsImpl = ColorsImpl(ApplicationProvider.getApplicationContext())

  public val fonts: FontsImpl = FontsImpl()

  @Before
  public fun initialize() {
    runBlocking { initializeView() }
  }
}
