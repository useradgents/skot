package tech.skot.tools.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test


fun Project.commandLine(vararg strCmd: String): String {
    val process =
        ProcessBuilder(strCmd.asList())
            .directory(projectDir)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
    val res = String(process.inputStream.readBytes())
    process.waitFor()
    process.destroy()
    return res
}

/**
 * Les plugins skot déclarent `generated/jvmTest` comme source de test et y génèrent des mocks et
 * des classes de base abstraites. Un module dont l'application n'a encore écrit aucun test possède
 * donc des sources de test sans aucun `@Test` — ce que Gradle 9 traite par défaut comme une erreur
 * de configuration et qui fait échouer `./gradlew build`.
 */
internal fun Project.skDontFailOnGeneratedTestSourcesOnly() {
    tasks.withType(Test::class.java).configureEach {
        failOnNoDiscoveredTests.set(false)
    }
}

