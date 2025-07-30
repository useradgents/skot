package tech.skot.tools.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import tech.skot.Versions
import tech.skot.tools.gradle.SKLibrary.Companion.addDependenciesToViewLegacy

@Suppress("UNUSED_PARAMETER")
class PluginViewLegacy : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.library")

        project.extensions.findByType(LibraryExtension::class)?.android(project)
        project.extensions.findByType(KotlinMultiplatformExtension::class)?.conf(project)

        project.dependencies {
            dependencies(project)
            addDependenciesToViewLegacy(this, project.rootDir.toPath())
        }
    }

    private fun KotlinMultiplatformExtension.conf(project: Project) {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            optIn.add("kotlin.time.ExperimentalTime")

        }
        androidTarget()
        jvm()
    }

    private fun LibraryExtension.android(project: Project) {
        sourceSets.getByName("main") {
            java.srcDir("src/androidMain/kotlin")
            java.srcDir("generated/androidMain/kotlin")
            assets.srcDirs("src/androidMain/assets")
            res.srcDir("src/androidMain/res_referenced")
            manifest.srcFile("src/androidMain/AndroidManifest.xml")

            skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
                res.srcDir("src/androidMain/res$it")
                res.srcDir("src/androidMain/res${it}_referenced")
                java.srcDir("src/androidMain/kotlin$it")
            }
            res.srcDir("src/androidMain/res")
        }

        sourceSets.getByName("androidTest") {
            java.srcDir("generated/androidTest/kotlin")
            skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
                java.srcDir("generated$it/androidTest/kotlin")
            }
        }

        androidBaseConfig(project)

        buildFeatures {
            viewBinding = true
        }
    }

    private fun DependencyHandlerScope.dependencies(project: Project) {
        val parentProjectPath = project.parent?.path ?: ""

        add("api", "${Versions.group}:viewlegacy:${Versions.skot}")
        add("api", project("$parentProjectPath:viewcontract"))
        add("androidTestImplementation", "${Versions.group}:viewlegacyTests:${Versions.skot}")
    }
}
