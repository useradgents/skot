package tech.skot.tools.gradle

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SKFlavorsTest {
    @Test
    fun `deux dimensions, combinaisons dans l'ordre marque puis environnement`() {
        val flavors = SKFlavors.parse("brand=sezane,octobre\nenvironment=qa,prod\n")
        assertEquals(listOf("brand", "environment"), flavors.dimensions)
        assertEquals(mapOf("brand" to listOf("sezane", "octobre"), "environment" to listOf("qa", "prod")), flavors.flavorsByDimension)
        assertEquals(listOf("sezaneQa", "sezaneProd", "octobreQa", "octobreProd"), flavors.variants.map { it.name })
        assertEquals(listOf("sezane", "qa", "sezaneQa"), flavors.variants.first().sourceSets)
        assertEquals(
            listOf("sezane", "qa", "sezaneQa", "prod", "sezaneProd", "octobre", "octobreQa", "octobreProd"),
            flavors.variantSourceSets,
        )
    }

    @Test
    fun `sans marque, une seule dimension`() {
        val flavors = SKFlavors.parse("environment=preprod, mockLocale")
        assertEquals(listOf("environment"), flavors.dimensions)
        assertEquals(listOf(SKVariant("preprod", listOf("preprod")), SKVariant("mockLocale", listOf("mockLocale"))), flavors.variants)
    }

    @Test
    fun `environnement obligatoire`() {
        val error = assertFailsWith<IllegalArgumentException> { SKFlavors.parse("brand=sezane\nenvironment=") }
        assertContains(error.message!!, "environment")
    }

    @Test
    fun `nom en majuscule refusé`() {
        val error = assertFailsWith<IllegalArgumentException> { SKFlavors.parse("environment=Prod") }
        assertContains(error.message!!, "Prod")
    }

    @Test
    fun `commentaire en fin de ligne refusé`() {
        val error = assertFailsWith<IllegalArgumentException> { SKFlavors.parse("environment=qa,prod # défaut qa") }
        assertContains(error.message!!, "prod # défaut qa")
    }

    @Test
    fun `nom réservé refusé`() {
        val error = assertFailsWith<IllegalArgumentException> { SKFlavors.parse("environment=debug") }
        assertContains(error.message!!, "debug")
    }

    @Test
    fun `même nom dans les deux dimensions refusé`() {
        val error = assertFailsWith<IllegalArgumentException> { SKFlavors.parse("brand=prod\nenvironment=prod") }
        assertContains(error.message!!, "prod")
    }

    @Test
    fun `fichier absent, pas de flavors`() {
        assertNull(SKFlavors.read(Files.createTempDirectory("sk").toFile()))
    }

    @Test
    fun `fichier lu à la racine`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve(SKFlavors.FILE_NAME).writeText("environment=dev\n")
        assertEquals(SKFlavors(emptyList(), listOf("dev")), SKFlavors.read(dir))
    }
}
