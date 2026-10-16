package tech.skot.tools.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import tech.skot.Versions

class PluginLibraryContract : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("java-library")
        project.plugins.apply("maven-publish")
        project.plugins.apply("kotlinx-serialization")
        project.skFailOnAndroidSources()
        project.skSubstituteLegacyKmpArtifacts()
        project.skKotlinCompilerOptions()

        project.extensions.getByType<KotlinJvmProjectExtension>().apply {
            jvmToolchain(21)
            sourceSets.getByName("main").kotlin.srcDir("src/commonMain/kotlin")
            sourceSets.getByName("test").kotlin.srcDir("src/jvmTest/kotlin")
        }
        project.extensions.getByType<JavaPluginExtension>().withSourcesJar()
        // Avant la 2.0.0, KMP créait la publication : elle est maintenant déclarée ici.
        project.extensions.getByType<PublishingExtension>().publications.register("maven", MavenPublication::class.java) {
            from(project.components.getByName("java"))
        }
        project.dependencies.add("api", "${Versions.group}:viewcontract:${Versions.skot}")
    }
}
