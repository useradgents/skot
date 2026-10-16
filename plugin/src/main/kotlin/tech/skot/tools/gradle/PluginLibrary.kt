package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import tech.skot.Versions

class PluginLibrary : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("com.android.library")
        project.plugins.apply("maven-publish")
        project.plugins.apply("kotlinx-serialization")
        project.skSubstituteLegacyKmpArtifacts()
        project.skKotlinCompilerOptions()

        project.extensions.getByType<LibraryExtension>().apply {
            androidBaseConfig(project)
            skLegacySourceSets(project, withResources = false)
            publishing {
                singleVariant("release") { withSourcesJar() }
            }
        }
        project.dependencies.add("api", project.project(":viewcontract"))
        project.dependencies.add("implementation", "${Versions.group}:viewmodel:${Versions.skot}")

        // Avant la 2.0.0, KMP créait la publication : elle est maintenant déclarée ici.
        project.afterEvaluate {
            extensions.getByType<PublishingExtension>().publications.create("release", MavenPublication::class.java) {
                from(components.getByName("release"))
            }
        }
    }
}
