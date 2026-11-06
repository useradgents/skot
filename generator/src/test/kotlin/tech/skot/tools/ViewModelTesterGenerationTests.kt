package tech.skot.tools

import com.squareup.kotlinpoet.ClassName
import tech.skot.tools.generation.viewmodel.typeParametersCountOf
import tech.skot.tools.generation.viewmodel.withStarProjections
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewModelTesterGenerationTests {
    @Test
    fun genericViewModelIsDetectedInItsSource() {
        val source =
            """
            package com.example

            class AccountSubSelectItem<D>(
                val item: D,
            ) : AccountSubSelectItemGen()
            """.trimIndent()
        assertEquals(1, typeParametersCountOf(source, "AccountSubSelectItem"))
    }

    @Test
    fun boundedAndNestedTypeParametersAreCountedAtTopLevelOnly() {
        val source = "internal open class ConfigurableLayoutList<K : Any, V : Map<K, List<V>>> : ConfigurableLayoutListGen()"
        assertEquals(2, typeParametersCountOf(source, "ConfigurableLayoutList"))
    }

    @Test
    fun nonGenericViewModelHasNoTypeParameter() {
        val source = "class AccountSubSelectItem(val item: String) : AccountSubSelectItemGen()"
        assertEquals(0, typeParametersCountOf(source, "AccountSubSelectItem"))
    }

    @Test
    fun anotherGenericClassOfTheFileIsIgnored() {
        val source =
            """
            class AccountSubSelectItemHolder<T>(val value: T)

            class AccountSubSelectItem(val item: String) : AccountSubSelectItemGen()
            """.trimIndent()
        assertEquals(0, typeParametersCountOf(source, "AccountSubSelectItem"))
    }

    @Test
    fun genericViewModelTypeIsStarProjected() {
        val viewModel = ClassName("com.example", "AccountSubSelectItem")
        assertEquals("com.example.AccountSubSelectItem<*>", viewModel.withStarProjections(1).toString())
        assertEquals("com.example.AccountSubSelectItem<*, *>", viewModel.withStarProjections(2).toString())
        assertEquals("com.example.AccountSubSelectItem", viewModel.withStarProjections(0).toString())
    }
}
