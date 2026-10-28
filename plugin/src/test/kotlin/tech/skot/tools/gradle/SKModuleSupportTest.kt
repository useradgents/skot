package tech.skot.tools.gradle

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SKModuleSupportTest {
    @Test
    fun `aucune source Android dans un module sans androidMain`() {
        val dir = Files.createTempDirectory("sk").toFile()
        assertTrue(skAndroidSourceFiles(dir).isEmpty())
    }

    @Test
    fun `le manifeste seul n'est pas une source Android`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve("src/androidMain").mkdirs()
        dir.resolve("src/androidMain/AndroidManifest.xml").writeText("<manifest/>")
        assertTrue(skAndroidSourceFiles(dir).isEmpty())
    }

    @Test
    fun `le code et les ressources Android sont listés, variantes comprises`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve("src/androidMain/kotlin/a").mkdirs()
        dir.resolve("src/androidMain/kotlin/a/File.kt").writeText("")
        dir.resolve("src/androidMain/resProd/values").mkdirs()
        dir.resolve("src/androidMain/resProd/values/strings.xml").writeText("")
        assertEquals(
            listOf("src/androidMain/kotlin/a/File.kt", "src/androidMain/resProd/values/strings.xml"),
            skAndroidSourceFiles(dir).map { it.relativeTo(dir).invariantSeparatorsPath }.sorted(),
        )
    }

    @Test
    fun `les ressources et manifestes de l'arborescence standard sont des sources Android`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve("src/main/res/values").mkdirs()
        dir.resolve("src/main/res/values/strings.xml").writeText("")
        dir.resolve("src/prod/res_referenced/drawable").mkdirs()
        dir.resolve("src/prod/res_referenced/drawable/a.xml").writeText("")
        dir.resolve("src/main/AndroidManifest.xml").writeText("<manifest/>")
        dir.resolve("src/main/kotlin/a").mkdirs()
        dir.resolve("src/main/kotlin/a/File.kt").writeText("")
        assertEquals(
            listOf("src/main/AndroidManifest.xml", "src/main/res/values/strings.xml", "src/prod/res_referenced/drawable/a.xml"),
            skStandardAndroidSourceFiles(dir).map { it.relativeTo(dir).invariantSeparatorsPath }.sorted(),
        )
    }

    @Test
    fun `un contrat sans ressources ni manifeste n'a pas de source Android standard`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve("src/main/kotlin").mkdirs()
        dir.resolve("src/test/kotlin").mkdirs()
        assertTrue(skStandardAndroidSourceFiles(dir).isEmpty())
    }

    @Test
    fun `un module à l'arborescence standard n'a rien d'ancien`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve("src/main/kotlin").mkdirs()
        dir.resolve("src/test/kotlin").mkdirs()
        dir.resolve("generated/main/kotlin").mkdirs()
        dir.resolve("generated/androidTest/kotlin").mkdirs()
        assertTrue(skLegacyLayoutDirs(dir).isEmpty())
    }

    @Test
    fun `l'ancienne arborescence et les générés de variante sont listés`() {
        val dir = Files.createTempDirectory("sk").toFile()
        listOf("src/commonMain/kotlin", "src/androidMain/res", "src/jvmTest/kotlin", "generated/commonMain/kotlin", "generatedSezane/commonMain/kotlin")
            .forEach { dir.resolve(it).mkdirs() }
        assertEquals(
            listOf("src/commonMain", "src/androidMain", "src/jvmTest", "generated/commonMain", "generatedSezane"),
            skLegacyLayoutDirs(dir),
        )
    }

    @Test
    fun `un contrat sans variante n'a rien à déplacer`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve("src/main/kotlin/a").mkdirs()
        dir.resolve("src/main/kotlin/a/Api.kt").writeText("")
        dir.resolve("src/test/kotlin/a").mkdirs()
        dir.resolve("src/test/kotlin/a/ApiTest.kt").writeText("")
        assertTrue(skContractVariantFiles(dir).isEmpty())
    }

    @Test
    fun `le code de variante d'un contrat est listé`() {
        val dir = Files.createTempDirectory("sk").toFile()
        dir.resolve("src/sezaneProd/kotlin/a").mkdirs()
        dir.resolve("src/sezaneProd/kotlin/a/TargetConstants.kt").writeText("")
        dir.resolve("src/sezaneProd/.DS_Store").writeText("")
        assertEquals(
            listOf("src/sezaneProd/kotlin/a/TargetConstants.kt"),
            skContractVariantFiles(dir).map { it.relativeTo(dir).invariantSeparatorsPath },
        )
    }
}
