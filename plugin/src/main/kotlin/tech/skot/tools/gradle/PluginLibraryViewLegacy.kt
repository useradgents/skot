package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import tech.skot.Versions

class PluginLibraryViewLegacy : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("maven-publish")
        // kotlin.android removed: AGP 9.0 has built-in Kotlin support

        project.extensions.findByType(LibraryExtension::class)?.android(project)

        project.dependencies {
            dependencies(project)
        }
    }

    private fun LibraryExtension.android(project: Project) {
        sourceSets.getByName("main") {
            kotlin.srcDir("src/androidMain/kotlin")
            kotlin.srcDir("generated/androidMain/kotlin")
            res.srcDir("src/androidMain/res")
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }

        sourceSets.getByName("androidTest") {
            res.srcDirs("src/androidTest/res")
        }

        androidBaseConfig(project)

        buildFeatures {
            viewBinding = true
        }
    }

    private fun DependencyHandlerScope.dependencies(project: Project) {
        add("implementation", "${Versions.group}:viewlegacy:${Versions.skot}")
        if (project.findProject(":viewcontract") != null) {
            add("implementation", project(":viewcontract"))
        }

        add("androidTestImplementation", "${Versions.group}:viewlegacyTests:${Versions.skot}")
    }
}
