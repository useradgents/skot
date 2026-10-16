package tech.skot.core.di

import android.app.Application
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TestAndroidInjector {
    private class Greeting(val text: String)

    @Test
    fun `l'application passée est accessible par androidApplication`() {
        val application = Application()
        val injector = BaseInjector(application, emptyList())
        assertSame(application, injector.androidApplication)
    }

    @Test
    fun `les modules voient androidApplication dans leurs définitions`() {
        val application = Application()
        val injector =
            BaseInjector(
                application,
                listOf(module<BaseInjector> { single { Greeting(if (androidApplication === application) "ok" else "ko") } }),
            )
        assertEquals("ok", injector.get<Greeting>().text)
    }
}
