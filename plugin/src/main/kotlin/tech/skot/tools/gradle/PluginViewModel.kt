package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import tech.skot.Versions
import tech.skot.tools.gradle.SKLibrary.Companion.addDependenciesToLibraries

class PluginViewModel : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("com.android.library")
        project.plugins.apply("kotlinx-serialization")
        project.skSubstituteLegacyKmpArtifacts()
        project.skKotlinCompilerOptions()
        project.skDontFailOnGeneratedTestSourcesOnly()

        val flavors = project.skFlavors()
        project.extensions.getByType<LibraryExtension>().apply {
            androidBaseConfig(project)
            skDeclareFlavors(flavors)
            skStandardSourceSets(flavors, withReferencedResources = false)
        }
        project.skAddVariantModule("api")

        val parentProjectPath = project.parent?.path ?: ""
        val librariesPath = (project.parent?.projectDir ?: project.rootDir).toPath()
        project.dependencies.apply {
            add("api", project.project("$parentProjectPath:viewcontract"))
            add("api", project.project("$parentProjectPath:modelcontract"))
            add("api", "${Versions.group}:core:${Versions.skot}")
            add("api", "${Versions.group}:viewmodel:${Versions.skot}")
            add("testImplementation", "${Versions.group}:viewmodelTests:${Versions.skot}")
            add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}")
        }
        addDependenciesToLibraries(project.dependencies, librariesPath, "api", "viewmodel")
        addDependenciesToLibraries(project.dependencies, librariesPath, "testImplementation", "viewmodelTests")
    }
}
