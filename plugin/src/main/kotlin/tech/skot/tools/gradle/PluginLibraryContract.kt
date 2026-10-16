package tech.skot.tools.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
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
        project.skSubstituteLegacyKmpArtifacts()

        project.extensions.getByType<KotlinJvmProjectExtension>().apply {
            jvmToolchain(21)
            sourceSets.getByName("main").kotlin.srcDir("src/commonMain/kotlin")
        }
        project.dependencies.add("api", "${Versions.group}:viewcontract:${Versions.skot}")
    }
}
