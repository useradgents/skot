package tech.skot.tools.gradle

import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.gradle.AppExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.project
import tech.skot.Versions


class PluginFeature : Plugin<Project> {

    override fun apply(project: Project) {
//        project.plugins.apply("com.android.library")
        project.plugins.apply("com.android.dynamic-feature")
        println("############## project.extensions ${project.extensions}")
        println("############## applying skot-feature !!!")
        project.extensions.findByType(DynamicFeatureExtension::class)?.android(project)
        project.extensions.findByType(AppExtension::class)?.android()

        project.dependencies {
            dependencies(project)
            SKLibrary.addDependenciesToViewLegacy(this, (project.parent?.projectDir ?: project.rootDir).toPath())
        }
    }


    private fun DynamicFeatureExtension.android(project: Project) {

        println("############## applying skot-feature LibraryExtension !!!")
        sourceSets {
            getByName("main") {
                java.srcDir("src/androidMain/kotlin")
                java.srcDir("generated/androidMain/kotlin")
                skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach<String> {
                    java.srcDir("src/androidMain/kotlin$it")
                    java.srcDir("generated$it/androidMain/kotlin")
                    res.srcDir("src/androidMain/res$it")
                }
                res.srcDir("src/androidMain/res")
                res.srcDir("src/androidMain/res_referenced")

                manifest.srcFile("src/androidMain/AndroidManifest.xml")
            }
            getByName("androidTest") {
                java.srcDir("src/androidTest/kotlin")
            }
        }

        packaging.jniLibs.excludes.add("META-INF/*.kotlin_module")
        packaging.jniLibs.excludes.add("META-INF/*")

        lint.abortOnError = false

        buildFeatures {
            viewBinding = true
        }

    }

    private fun AppExtension.android() {
        compileSdkVersion(Versions.android_compileSdk)

        defaultConfig {
            minSdk = Versions.android_minSdk
            targetSdk = Versions.android_targetSdk
        }
    }


    private fun DependencyHandlerScope.dependencies(project: Project) {

        val parentProjectPath = project.parent?.path ?: ""

        add("implementation", project("$parentProjectPath:viewmodel"))
        add("implementation", project("$parentProjectPath:model"))
        add("api", project("$parentProjectPath:viewcontract"))

    }

}
