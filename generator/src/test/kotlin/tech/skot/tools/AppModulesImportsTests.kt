package tech.skot.tools

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import tech.skot.tools.generation.AppModulesNames
import kotlin.test.Test
import kotlin.test.assertTrue

class AppModulesImportsTests {
    @Test
    fun `androidApplication et androidModelFrameworkModule sont importés`() {
        val file =
            FileSpec.builder("io.test.di", "generatedAppModules")
                .addProperty(
                    PropertySpec.builder("probe", Any::class)
                        .initializer("listOf(%M, %M)", AppModulesNames.androidApplication, AppModulesNames.androidModelFrameworkModule)
                        .build(),
                )
                .build()
                .toString()
        assertTrue(file.contains("import tech.skot.core.di.androidApplication"), file)
        assertTrue(file.contains("import tech.skot.di.androidModelFrameworkModule"), file)
    }
}
