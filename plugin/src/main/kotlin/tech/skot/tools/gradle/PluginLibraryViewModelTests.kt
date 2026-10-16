package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import tech.skot.Versions

/**
 * Plugin du module viewmodelTests d'une library : les tests-utilitaires de son module viewmodel,
 * publiés en bibliothèque Android. Le namespace reste déclaré par le script de la library.
 */
class PluginLibraryViewModelTests : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("com.android.library")
        project.plugins.apply("maven-publish")
        project.skSubstituteLegacyKmpArtifacts()
        project.skKotlinCompilerOptions()

        project.extensions.getByType<LibraryExtension>().apply {
            androidBaseConfig(project)
            // Dossier historique du module jvm() de l'ancien module KMP.
            sourceSets.getByName("main") {
                kotlin.directories.add("src/jvmMain/kotlin")
            }
            publishing {
                singleVariant("release") { withSourcesJar() }
            }
        }
        project.dependencies.add("implementation", project.project(":viewmodel"))
        project.dependencies.add("implementation", "${Versions.group}:viewmodelTests:${Versions.skot}")
        project.dependencies.add("implementation", "${Versions.group}:core:${Versions.skot}")

        // Avant la 2.0.0, KMP créait la publication : elle est maintenant déclarée ici.
        project.afterEvaluate {
            extensions.getByType<PublishingExtension>().publications.create("release", MavenPublication::class.java) {
                from(components.getByName("release"))
            }
        }
    }
}
