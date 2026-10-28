package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.LibraryProductFlavor
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SKFlavorsSupportTest {
    private fun androidLibrary(flavorsFile: String?): LibraryExtension {
        val dir = Files.createTempDirectory("sk").toFile()
        flavorsFile?.let { dir.resolve(SKFlavors.FILE_NAME).writeText(it) }
        val project = ProjectBuilder.builder().withProjectDir(dir).build()
        project.plugins.apply("com.android.library")
        return project.extensions.getByType(LibraryExtension::class.java).apply {
            val flavors = project.skFlavors()
            skDeclareFlavors(flavors)
            skStandardSourceSets(flavors, withReferencedResources = true)
        }
    }

    @Test
    fun `les flavors du fichier racine sont déclarés, le premier de chaque dimension par défaut`() {
        val android = androidLibrary("brand=blue,red\nenvironment=dev,prod\n")
        assertEquals(listOf("brand", "environment"), android.flavorDimensions.toList())
        assertEquals(setOf("blue", "red", "dev", "prod"), android.productFlavors.names)
        assertEquals("brand", android.productFlavors.getByName("red").dimension)
        assertTrue((android.productFlavors.getByName("blue") as LibraryProductFlavor).isDefault)
        assertTrue((android.productFlavors.getByName("dev") as LibraryProductFlavor).isDefault)
        assertFalse((android.productFlavors.getByName("red") as LibraryProductFlavor).isDefault)
        assertFalse((android.productFlavors.getByName("prod") as LibraryProductFlavor).isDefault)
    }

    @Test
    fun `res_referenced et dossiers générés branchés sur chaque source set`() {
        val android = androidLibrary("brand=blue,red\nenvironment=dev,prod\n")
        listOf("main", "blue", "red", "dev", "prod", "blueDev", "redProd").forEach {
            assertTrue("src/$it/res_referenced" in android.sourceSets.getByName(it).res.directories, it)
        }
        assertTrue("generated/main/kotlin" in android.sourceSets.getByName("main").kotlin.directories)
        assertTrue("generated/test/kotlin" in android.sourceSets.getByName("test").kotlin.directories)
        assertTrue("generated/androidTest/kotlin" in android.sourceSets.getByName("androidTest").kotlin.directories)
    }

    @Test
    fun `aucune déclaration sans fichier`() {
        val android = androidLibrary(null)
        assertTrue(android.flavorDimensions.isEmpty())
        assertTrue(android.productFlavors.isEmpty())
        assertTrue("src/main/res_referenced" in android.sourceSets.getByName("main").res.directories)
    }
}
