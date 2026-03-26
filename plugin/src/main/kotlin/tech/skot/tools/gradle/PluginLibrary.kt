package tech.skot.tools.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import tech.skot.Versions

class PluginLibrary : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.kotlin.multiplatform.library")
        project.plugins.apply("maven-publish")
        project.plugins.apply("kotlinx-serialization")

        project.extensions.findByType(KotlinMultiplatformAndroidLibraryExtension::class)?.androidBaseConfig(project)
        project.extensions.findByType(KotlinMultiplatformExtension::class)?.conf()
    }

    private fun KotlinMultiplatformExtension.conf() {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        }

        jvm()

        sourceSets["commonMain"].dependencies {
            api(project(":viewcontract"))
        }

        sourceSets["commonMain"].dependencies {
            implementation("${Versions.group}:viewmodel:${Versions.skot}")
        }
    }
}
