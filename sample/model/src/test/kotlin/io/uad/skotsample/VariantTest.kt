package io.uad.skotsample

import io.uad.skotsample.variant.Brand
import io.uad.skotsample.variant.Environment
import kotlin.test.Test
import kotlin.test.assertTrue

class VariantTest {
    @Test
    fun `le model voit les constantes de sa variante`() {
        assertTrue(Brand.NAME in setOf("Blue", "Red"))
        assertTrue(Environment.BASE_URL.startsWith("https://"))
    }
}
