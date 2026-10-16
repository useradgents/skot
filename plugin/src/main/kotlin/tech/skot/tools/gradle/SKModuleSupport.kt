package tech.skot.tools.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import tech.skot.Versions
import java.io.File

private const val MIGRATION_GUIDE = "docs/migration/migration-2.0.0-ua.md"

/** SKot 2 n'utilise plus Kotlin Multiplatform : un module qui l'applique encore doit être migré. */
internal fun Project.skFailOnKotlinMultiplatform() {
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        throw GradleException(
            "SKot ${Versions.skot} : le module $path applique encore kotlin(\"multiplatform\"). " +
                "Retirez-le ainsi que le bloc kotlin { android { } sourceSets { } } (voir $MIGRATION_GUIDE).",
        )
    }
}

/** Fichiers de code ou de ressources Android d'un module de contrat (le manifeste est ignoré). */
internal fun skAndroidSourceFiles(projectDir: File): List<File> =
    projectDir.resolve("src/androidMain").listFiles().orEmpty()
        .filter { it.isDirectory && (it.name.startsWith("kotlin") || it.name.startsWith("res")) }
        .flatMap { dir -> dir.walkTopDown().filter { it.isFile }.toList() }

/** Un contrat est un module JVM pur : son code Android doit partir dans le module model. */
internal fun Project.skFailOnAndroidSources() {
    val files = skAndroidSourceFiles(projectDir)
    if (files.isNotEmpty()) {
        throw GradleException(
            "SKot ${Versions.skot} : le module $path est un module JVM, déplacez ces fichiers Android dans model " +
                "(voir $MIGRATION_GUIDE) :\n" + files.joinToString("\n") { "  " + it.relativeTo(projectDir).path },
        )
    }
}

private val skLegacyKmpModules = listOf("core", "viewcontract", "modelcontract", "viewmodel", "viewmodelTests", "generator")

/**
 * Les bibliothèques SKot compilées avant la 2.0 peuvent dépendre des artefacts de plateforme KMP
 * (`core-jvm`…), qui n'existent plus : ils sont remplacés par les artefacts uniques de cette version.
 */
internal fun Project.skSubstituteLegacyKmpArtifacts() {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            skLegacyKmpModules.forEach { module ->
                listOf("jvm", "android").forEach { platform ->
                    substitute(module("${Versions.group}:$module-$platform"))
                        .using(module("${Versions.group}:$module:${Versions.skot}"))
                }
            }
            substitute(module("${Versions.group}:model-jvm")).using(module("${Versions.group}:modelTests:${Versions.skot}"))
            substitute(module("${Versions.group}:model-android")).using(module("${Versions.group}:model:${Versions.skot}"))
        }
    }
}

internal fun Project.skKotlinCompilerOptions() {
    tasks.withType(KotlinCompilationTask::class.java).configureEach {
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4)
            optIn.add("kotlin.time.ExperimentalTime")
        }
    }
}

/**
 * Branche un module Android sur l'arborescence historique des modules SKot (commonMain, androidMain,
 * jvmTest, generated…), variantes skot_variants comprises. Les dossiers des projets ne bougent pas
 * avant le passage aux flavors.
 */
internal fun com.android.build.api.dsl.LibraryExtension.skLegacySourceSets(
    project: Project,
    withResources: Boolean,
) {
    val combinations = project.skVariantsCombinaison()
    sourceSets.getByName("main") {
        listOf("src/commonMain/kotlin", "src/androidMain/kotlin", "generated/commonMain/kotlin", "generated/androidMain/kotlin")
            .forEach { kotlin.directories.add(it) }
        combinations.forEach {
            kotlin.directories.add("src/commonMain/kotlin$it")
            kotlin.directories.add("src/androidMain/kotlin$it")
            kotlin.directories.add("generated$it/commonMain/kotlin")
            kotlin.directories.add("generated$it/androidMain/kotlin")
            if (withResources) res.directories.add("src/androidMain/res$it")
        }
        if (withResources) res.directories.add("src/androidMain/res")
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
    }
    sourceSets.getByName("test") {
        listOf("src/jvmTest/kotlin", "src/commonTest/kotlin", "generated/jvmTest/kotlin")
            .forEach { kotlin.directories.add(it) }
        combinations.forEach { kotlin.directories.add("generated$it/jvmTest/kotlin") }
    }
}
