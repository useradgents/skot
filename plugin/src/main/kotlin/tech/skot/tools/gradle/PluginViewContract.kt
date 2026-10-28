package tech.skot.tools.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import tech.skot.Versions
import tech.skot.tools.gradle.SKLibrary.Companion.addDependenciesToLibraries

class PluginViewContract : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("java-library")
        project.plugins.apply("maven-publish")
        project.plugins.apply("kotlinx-serialization")
        project.skFailOnLegacyLayout()
        project.skFailOnContractVariantSources()
        project.skSubstituteLegacyKmpArtifacts()
        project.skKotlinCompilerOptions()

        project.extensions.getByType<KotlinJvmProjectExtension>().conf()

        project.dependencies.add("api", "${Versions.group}:viewcontract:${Versions.skot}")
        // utilisé souvent par les projets et inclu à core de toutes façons
        project.dependencies.add("implementation", "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinxDateTime}")
        addDependenciesToLibraries(
            project.dependencies,
            (project.parent?.projectDir ?: project.rootDir).toPath(),
            "api",
            "viewcontract",
        )
    }

    private fun KotlinJvmProjectExtension.conf() {
        jvmToolchain(21)
        sourceSets.getByName("main").kotlin.srcDir("generated/main/kotlin")
    }
}
