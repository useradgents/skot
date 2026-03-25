package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.project
import tech.skot.Versions

class PluginLibraryViewLegacy : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("maven-publish")

        project.extensions.findByType(LibraryExtension::class)?.android(project)

        project.dependencies {
            dependencies(project)
        }

        project.afterEvaluate {
            extensions.findByType(PublishingExtension::class)?.publications {
                create<MavenPublication>("release") {
                    from(components.getByName("release"))
                }
            }
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

        publishing {
            singleVariant("release")
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