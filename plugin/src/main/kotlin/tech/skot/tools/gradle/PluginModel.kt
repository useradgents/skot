package tech.skot.tools.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import tech.skot.Versions

class PluginModel : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.kotlin.multiplatform.library")
        project.plugins.apply("kotlinx-serialization")

        project.extensions.findByType(KotlinMultiplatformAndroidLibraryExtension::class)?.androidBaseConfig(project)
        project.extensions.findByType(KotlinMultiplatformExtension::class)?.conf(project)
    }

    private fun KotlinMultiplatformExtension.conf(project: Project) {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            optIn.add("kotlin.time.ExperimentalTime")
        }
        jvm()

        sourceSets["commonMain"].kotlin.srcDir("generated/commonMain/kotlin")

        val parentProjectPath = project.parent?.path ?: ""

        sourceSets["commonMain"].dependencies {
            api(project("$parentProjectPath:modelcontract"))
            api("${Versions.group}:model:${Versions.skot}")
            api("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}")

            if (project.skReadImportsProperties()?.ktor2 != false) {
                api("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                api("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
            } else {
                api("io.ktor:ktor-client-serialization:${Versions.ktor}")
            }
        }

        skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
            sourceSets["commonMain"].kotlin.srcDir("src/commonMain/kotlin$it")
            sourceSets["androidMain"].kotlin.srcDir("src/androidMain/kotlin$it")
        }

        sourceSets["jvmTest"].kotlin.srcDir("generated/jvmTest")

        sourceSets["androidMain"].kotlin.srcDir("generated/androidMain/kotlin")
    }
}
