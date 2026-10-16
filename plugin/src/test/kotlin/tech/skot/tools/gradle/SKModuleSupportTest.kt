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
}
