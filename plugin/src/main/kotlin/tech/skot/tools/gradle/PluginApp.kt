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
        project.skFailOnLegacyLayout()
        project.skSubstituteLegacyKmpArtifacts()
        project.plugins.apply("com.android.application")
        // kotlin.android removed: AGP 9.0 has built-in Kotlin support

        project.extensions.findByType(ApplicationExtension::class)?.conf(project)

        project.dependencies {
            dependencies(project)
        }
        project.skAddVariantModule("implementation")
    }

    private fun ApplicationExtension.conf(project: Project) {
        val flavors = project.skFlavors()
        skDeclareFlavors(flavors)
        skStandardSourceSets(flavors, withReferencedResources = false)

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
