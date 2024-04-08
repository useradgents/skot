package tech.skot.tools.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import tech.skot.Versions

class PluginViewModel : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("kotlinx-serialization")

        project.extensions.findByType(LibraryExtension::class)?.conf(project)

        project.extensions.findByType(KotlinMultiplatformExtension::class)?.conf(project)
    }

    private fun LibraryExtension.conf(project: Project) {
        androidBaseConfig(project)

        sourceSets {
            getByName("main").java.srcDirs("src/androidMain/kotlin")
            getByName("main").manifest.srcFile("src/androidMain/AndroidManifest.xml")
            getByName("test").java.srcDirs("src/javaTest/kotlin")
        }
    }

    private fun KotlinMultiplatformExtension.conf(project: Project) {
        jvmToolchain(17)

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        }
        androidTarget {
        }
        jvm()

        sourceSets["commonMain"].kotlin.srcDir("generated/commonMain/kotlin")

        val parentProjectPath = project.parent?.path ?: ""

        sourceSets["commonMain"].dependencies {
            api(project("$parentProjectPath:viewcontract"))
            api(project("$parentProjectPath:modelcontract"))
            api("${Versions.group}:core:${Versions.skot}")
            api("${Versions.group}:viewmodel:${Versions.skot}")
        }

        sourceSets["androidMain"].dependencies {
        }

        skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
            sourceSets["commonMain"].kotlin.srcDir("src/commonMain/kotlin$it")
            sourceSets["androidMain"].kotlin.srcDir("src/androidMain/kotlin$it")
        }

        sourceSets["commonTest"].kotlin.srcDir("src/commonTest/kotlin")

        sourceSets["commonTest"].dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test-common:${Versions.kotlin}")
            implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${Versions.kotlin}")
        }

        sourceSets["jvmTest"].kotlin.srcDir("generated/jvmTest/kotlin")
        skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
            sourceSets["jvmTest"].kotlin.srcDir("generated$it/jvmTest/kotlin")
        }

        sourceSets["jvmTest"].dependencies {
            implementation("${Versions.group}:viewmodelTests:${Versions.skot}")
        }

        SKLibrary.addDependenciesToLibraries(this, (project.parent?.projectDir ?: project.rootDir).toPath(), "commonMain", "viewmodel")
        SKLibrary.addDependenciesToLibraries(this, (project.parent?.projectDir ?: project.rootDir).toPath(), "jvmTest", "viewmodelTests")
    }
}
