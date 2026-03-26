package tech.skot.tools.gradle

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.project
import tech.skot.Versions

class PluginApp : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.application")
        // kotlin.android removed: AGP 9.0 has built-in Kotlin support

        project.extensions.findByType(ApplicationExtension::class)?.conf(project)

        project.dependencies {
            dependencies(project)
        }
    }

    private fun ApplicationExtension.conf(project: Project) {
        sourceSets {
            getByName("main") {
                kotlin.srcDir("src/androidMain/kotlin")
                kotlin.srcDir("generated/androidMain/kotlin")
                skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach<String> {
                    kotlin.srcDir("src/androidMain/kotlin$it")
                    kotlin.srcDir("generated$it/androidMain/kotlin")
                }
                res.srcDir("src/androidMain/res")
                assets.srcDir("src/androidMain/assets")
                manifest.srcFile("src/androidMain/AndroidManifest.xml")
            }
            getByName("androidTest") {
                kotlin.srcDir("src/androidTest/kotlin")
            }
        }

        androidBaseConfig(project)

        packaging {
            resources.excludes.add("META-INF/*.kotlin_module")
            resources.excludes.add("META-INF/*")
        }

        compileOptions {
            isCoreLibraryDesugaringEnabled = true
        }
    }

    private fun DependencyHandlerScope.dependencies(project: Project) {
        add("implementation", project(":viewmodel"))
        add("implementation", project(":model"))
        add("implementation", project(":view"))

        val androidProperties = project.skReadAndroidProperties()
        if (androidProperties?.leakCanary != false) {
            add("debugImplementation", "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}")
        }

        add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:${Versions.desugaring}")
    }
}
