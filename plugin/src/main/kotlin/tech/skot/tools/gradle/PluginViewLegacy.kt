package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.project
import tech.skot.Versions
import tech.skot.tools.gradle.SKLibrary.Companion.addDependenciesToViewLegacy

@Suppress("UNUSED_PARAMETER")
class PluginViewLegacy : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnLegacyLayout()
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("com.android.library")
        // kotlin.android removed: AGP 9.0 has built-in Kotlin support
        project.skSubstituteLegacyKmpArtifacts()
        project.extensions.findByType(LibraryExtension::class)?.android(project)

        project.dependencies {
            dependencies(project)
            addDependenciesToViewLegacy(this, project.rootDir.toPath())
        }
        project.skAddVariantModule("api")
    }

    private fun LibraryExtension.android(project: Project) {
        val flavors = project.skFlavors()
        skDeclareFlavors(flavors)
        skStandardSourceSets(flavors, withReferencedResources = true)

        androidBaseConfig(project)

        buildFeatures {
            viewBinding = true
        }
    }

    private fun DependencyHandlerScope.dependencies(project: Project) {
        val parentProjectPath = project.parent?.path ?: ""

        add("api", "${Versions.group}:viewlegacy:${Versions.skot}")
        add("api", project("$parentProjectPath:viewcontract"))
        add("androidTestImplementation", "${Versions.group}:viewlegacyTests:${Versions.skot}")
    }
}
