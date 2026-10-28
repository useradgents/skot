package tech.skot.tools.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import tech.skot.Versions
import java.io.File

private const val MIGRATION_GUIDE = "docs/migration/migration-2.0.0-ua.md"
private const val FLAVORS_MIGRATION_SCRIPT = "scripts/migration/2.0.0-ua/migrate-flavors.sh (dépôt SKot)"
private val variantGeneratedDir = Regex("generated[A-Z].*")

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

/** Dossiers de l'ancienne arborescence SKot (KMP, variantes skot_variants) présents dans un module. */
internal fun skLegacyLayoutDirs(projectDir: File): List<String> {
    val legacy =
        listOf("commonMain", "androidMain", "jvmMain", "jvmTest", "commonTest").map { "src/$it" } +
            listOf("commonMain", "androidMain", "jvmTest").map { "generated/$it" }
    val variantGenerated =
        projectDir.listFiles().orEmpty()
            .filter { it.isDirectory && variantGeneratedDir.matches(it.name) }
            .map { it.name }
            .sorted()
    return legacy.filter { projectDir.resolve(it).isDirectory } + variantGenerated
}

/**
 * SKot 2 utilise les product flavors et l'arborescence standard : un projet encore au format
 * skot_variants (fichier racine, dossiers commonMain/androidMain/jvmTest, variantes suffixées) doit
 * d'abord passer par le script de migration.
 */
internal fun Project.skFailOnLegacyLayout() {
    val legacyVariantsFile = rootProject.rootDir.resolve("skot_variants.properties").takeIf { it.isFile }
    val found = listOfNotNull(legacyVariantsFile?.let { "skot_variants.properties (racine du projet)" }) + skLegacyLayoutDirs(projectDir)
    if (found.isNotEmpty()) {
        throw GradleException(
            "SKot ${Versions.skot} : le module $path utilise encore l'ancienne arborescence SKot. " +
                "Lancez $FLAVORS_MIGRATION_SCRIPT (voir $MIGRATION_GUIDE) :\n" + found.joinToString("\n") { "  $it" },
        )
    }
}

/** Fichiers d'un contrat hors src/main et src/test : du code propre à une variante. */
internal fun skContractVariantFiles(projectDir: File): List<File> =
    projectDir.resolve("src").listFiles().orEmpty()
        .filter { it.isDirectory && it.name != "main" && it.name != "test" }
        .flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.name != ".DS_Store" }.toList() }

/** Un contrat est un module JVM sans flavors : son code de variante va dans le module :variant. */
internal fun Project.skFailOnContractVariantSources() {
    val files = skContractVariantFiles(projectDir)
    if (files.isNotEmpty()) {
        throw GradleException(
            "SKot ${Versions.skot} : le module $path est un contrat sans variantes, déplacez ces fichiers dans le module " +
                ":variant (voir $MIGRATION_GUIDE) :\n" + files.joinToString("\n") { "  " + it.relativeTo(projectDir).path },
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
