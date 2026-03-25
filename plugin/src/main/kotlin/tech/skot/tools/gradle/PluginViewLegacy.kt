package tech.skot.tools.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.LibraryExtension
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
        if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            project.plugins.apply("com.android.kotlin.multiplatform.library")
            project.extensions.findByType(KotlinMultiplatformAndroidLibraryExtension::class)?.androidBaseConfig(project)
        } else {
            project.plugins.apply("com.android.library")
            // kotlin.android removed: AGP 9.0 has built-in Kotlin support
            project.extensions.findByType(LibraryExtension::class)?.android(project)
        }

        project.extensions.findByType(KotlinMultiplatformExtension::class)?.conf(project)

        project.dependencies {
            dependencies(project)
            addDependenciesToViewLegacy(this, project.rootDir.toPath())
        }
    }

    private fun KotlinMultiplatformExtension.conf(project: Project) {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
            optIn.add("kotlin.time.ExperimentalTime")
        }
        jvm()
    }

    private fun LibraryExtension.android(project: Project) {
        sourceSets.getByName("main") {
            kotlin.srcDir("src/androidMain/kotlin")
            kotlin.srcDir("generated/androidMain/kotlin")
            assets.srcDirs("src/androidMain/assets")
            res.srcDir("src/androidMain/res_referenced")
            manifest.srcFile("src/androidMain/AndroidManifest.xml")

            skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
                res.srcDir("src/androidMain/res$it")
                res.srcDir("src/androidMain/res${it}_referenced")
                kotlin.srcDir("src/androidMain/kotlin$it")
            }
            res.srcDir("src/androidMain/res")
        }

        sourceSets.getByName("androidTest") {
            kotlin.srcDir("generated/androidTest/kotlin")
            skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
                kotlin.srcDir("generated$it/androidTest/kotlin")
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
