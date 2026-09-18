package tech.skot.tools.gradle

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Forme réelle d'un objet SKBuild d'application : des constantes déclarées dans buildSrc.
object TestBuild {
    const val appName: String = "MyApp"
    const val versionName: String = "1.2.3"
}

class CopyBuildFileTest {
    private fun generate(): String {
        val outputDir = Files.createTempDirectory("skbuild").toFile()
        copyBuildFileToImplementation(
            build = TestBuild,
            outputDir = outputDir,
            versionCode = 42,
            addingVersionCodeAndDebug = true,
        )
        return File(outputDir, "tech/skot/tools/gradle/TestBuild.kt").readText()
    }

    @Test
    fun `debug is a runtime value read from SKEnv`() {
        val generated = generate()
        assertFalse(generated.contains("const val debug"), "debug must not be a compile-time constant")
        assertTrue(generated.contains("SKEnv.debug"), "debug must delegate to SKEnv")
    }

    @Test
    fun `the SKEnv import is emitted`() {
        assertTrue(generate().contains("import tech.skot.core.SKEnv"))
    }

    @Test
    fun `the other properties stay compile-time constants`() {
        val generated = generate()
        assertTrue(generated.contains("public const val versionCode: Int = 42"))
        assertTrue(generated.contains("public const val appName: String = \"MyApp\""))
    }
}
