package tech.skot.tools

import tech.skot.tools.generation.SKFlavors
import tech.skot.tools.generation.existsInSourceSets
import tech.skot.tools.generation.resources.checkReferencedInEveryVariant
import tech.skot.tools.generation.resources.referencedResDirs
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReferencedResourcesTest {
    private val flavors = SKFlavors(listOf("blue", "red"), listOf("dev", "prod"))

    @Test
    fun `dossiers res_referenced de main puis des variantes`() {
        val view = Files.createTempDirectory("sk").resolve("view")
        val dirs = referencedResDirs(view, flavors)
        assertEquals(listOf("main") + flavors.variantSourceSets, dirs.keys.toList())
        assertEquals(view.resolve("src/blueDev/res_referenced"), dirs["blueDev"])
        assertEquals(listOf("main"), referencedResDirs(view, null).keys.toList())
    }

    @Test
    fun `ressource présente dans chaque marque acceptée`() {
        checkReferencedInEveryVariant(
            "drawable",
            mapOf("main" to listOf("a"), "blue" to listOf("logo"), "red" to listOf("logo")),
            flavors,
        )
    }

    @Test
    fun `ressource d'environnement et de combinaison acceptées si complètes`() {
        checkReferencedInEveryVariant(
            "string",
            mapOf("dev" to listOf("url"), "prod" to listOf("url"), "blueDev" to listOf("x"), "blueProd" to listOf("x"), "red" to listOf("x")),
            flavors,
        )
    }

    @Test
    fun `ressource absente d'une marque refusée avec les variantes concernées`() {
        val error =
            assertFailsWith<IllegalStateException> {
                checkReferencedInEveryVariant("drawable", mapOf("blue" to listOf("logo"), "red" to emptyList()), flavors)
            }
        assertContains(error.message!!, "redDev : logo")
        assertContains(error.message!!, "redProd : logo")
        assertFalse(error.message!!.contains("blueDev"))
    }

    @Test
    fun `sans flavors aucune vérification`() {
        checkReferencedInEveryVariant("drawable", mapOf("main" to listOf("a"), "blue" to listOf("orphan")), null)
    }

    @Test
    fun `fichier écrit à la main trouvé dans main ou une variante`() {
        val root = Files.createTempDirectory("sk")
        Files.createDirectories(root.resolve("view/src/red/res/layout"))
        Files.writeString(root.resolve("view/src/red/res/layout/splash.xml"), "<x/>")
        assertTrue(existsInSourceSets(root, "view", "res/layout/splash.xml", flavors))
        assertFalse(existsInSourceSets(root, "view", "res/layout/other.xml", flavors))
        assertFalse(existsInSourceSets(root, "view", "res/layout/splash.xml", null))
    }

    @Test
    fun `racine contenant main`() {
        val root = Files.createTempDirectory("sk").resolve("main/app")
        Files.createDirectories(root.resolve("view/src/main/res/layout"))
        Files.writeString(root.resolve("view/src/main/res/layout/splash.xml"), "<x/>")
        assertTrue(existsInSourceSets(root, "view", "res/layout/splash.xml", flavors))
        assertFalse(existsInSourceSets(root, "view", "res/layout/other.xml", flavors))
    }
}
