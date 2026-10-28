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
        project.skFailOnLegacyLayout()
        project.skSubstituteLegacyKmpArtifacts()
        project.plugins.apply("com.android.dynamic-feature")
        println("############## project.extensions ${project.extensions}")
        println("############## applying skot-feature !!!")
        project.extensions.findByType(DynamicFeatureExtension::class)?.android(project)

        project.dependencies {
            dependencies(project)
            SKLibrary.addDependenciesToViewLegacy(this, (project.parent?.projectDir ?: project.rootDir).toPath())
        }
        project.skAddVariantModule("implementation")
    }

    private fun DynamicFeatureExtension.android(project: Project) {
        println("############## applying skot-feature LibraryExtension !!!")
        compileSdk = Versions.android_compileSdk
        defaultConfig {
            minSdk = Versions.android_minSdk
        }

        val flavors = project.skFlavors()
        skDeclareFlavors(flavors)
        skStandardSourceSets(flavors, withReferencedResources = true)

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
