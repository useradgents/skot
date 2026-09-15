package tech.skot.tools.gradle

import com.android.build.api.dsl.DynamicFeatureExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.project
import tech.skot.Versions

class PluginFeature : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.dynamic-feature")
        println("############## project.extensions ${project.extensions}")
        println("############## applying skot-feature !!!")
        project.extensions.findByType(DynamicFeatureExtension::class)?.android(project)

        project.dependencies {
            dependencies(project)
            SKLibrary.addDependenciesToViewLegacy(this, (project.parent?.projectDir ?: project.rootDir).toPath())
        }
    }

    private fun DynamicFeatureExtension.android(project: Project) {
        println("############## applying skot-feature LibraryExtension !!!")
        compileSdk = Versions.android_compileSdk
        defaultConfig {
            minSdk = Versions.android_minSdk
        }

        sourceSets {
            getByName("main") {
                kotlin.directories.add("src/androidMain/kotlin")
                kotlin.directories.add("generated/androidMain/kotlin")
                skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach<String> {
                    kotlin.directories.add("src/androidMain/kotlin$it")
                    kotlin.directories.add("generated$it/androidMain/kotlin")
                    res.directories.add("src/androidMain/res$it")
                }
                res.directories.add("src/androidMain/res")
                res.directories.add("src/androidMain/res_referenced")

                manifest.srcFile("src/androidMain/AndroidManifest.xml")
            }
            getByName("androidTest") {
                kotlin.directories.add("src/androidTest/kotlin")
            }
        }

        packaging.jniLibs.excludes.add("META-INF/*.kotlin_module")
        packaging.jniLibs.excludes.add("META-INF/*")

        lint.abortOnError = false

        buildFeatures {
            viewBinding = true
        }
    }

    private fun DependencyHandlerScope.dependencies(project: Project) {
        val parentProjectPath = project.parent?.path ?: ""

        add("implementation", project("$parentProjectPath:viewmodel"))
        add("implementation", project("$parentProjectPath:model"))
        add("api", project("$parentProjectPath:viewcontract"))
    }
}
