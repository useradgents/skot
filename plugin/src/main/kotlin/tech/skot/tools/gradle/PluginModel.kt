package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import tech.skot.Versions

class PluginModel : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("com.android.library")
        project.plugins.apply("kotlinx-serialization")
        project.skSubstituteLegacyKmpArtifacts()
        project.skKotlinCompilerOptions()
        project.skDontFailOnGeneratedTestSourcesOnly()

        project.extensions.getByType<LibraryExtension>().apply {
            androidBaseConfig(project)
            skLegacySourceSets(project, withResources = true)
        }

        val parentProjectPath = project.parent?.path ?: ""
        project.dependencies.apply {
            add("api", project.project("$parentProjectPath:modelcontract"))
            add("api", "${Versions.group}:model:${Versions.skot}")
            add("api", "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}")
            if (project.skReadImportsProperties()?.ktor2 != false) {
                add("api", "io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                add("api", "io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
            } else {
                add("api", "io.ktor:ktor-client-serialization:${Versions.ktor}")
            }
            add("testImplementation", "${Versions.group}:modelTests:${Versions.skot}")
        }
    }
}
