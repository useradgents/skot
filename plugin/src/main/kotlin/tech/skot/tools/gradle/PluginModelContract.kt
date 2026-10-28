package tech.skot.tools.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import tech.skot.Versions

open class SKPluginModelContractExtension {
    var buildFiles: List<Any>? = null
}

@DisableCachingByDefault(because = "Simple recopie de fichiers, le cache n'apporte rien")
abstract class SKCopyBuildFileTask : DefaultTask() {
    @get:Internal
    abstract val buildFiles: ListProperty<Any>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val versionCode: Property<Int>

    @get:Input
    abstract val addingVersionCodeAndDebug: Property<Boolean>

    @TaskAction
    fun copy() {
        buildFiles.get().forEach {
            copyBuildFileToImplementation(
                build = it,
                outputDir = outputDir.get().asFile,
                versionCode = versionCode.get(),
                addingVersionCodeAndDebug = addingVersionCodeAndDebug.get(),
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
class PluginModelContract : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<SKPluginModelContractExtension>("skot")
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("java-library")
        project.plugins.apply("maven-publish")
        project.plugins.apply("kotlinx-serialization")
        project.skFailOnLegacyLayout()
        project.skFailOnContractVariantSources()
        project.skSubstituteLegacyKmpArtifacts()
        project.skKotlinCompilerOptions()

        val outputDirValue = project.layout.projectDirectory.dir("generated/main/kotlin")
        val appProvider = project.provider { extension.buildFiles ?: emptyList<Any>() }
        val versionCodeProvider = project.providers.provider { project.skVersionCode() }

        val copyBuildFile = project.tasks.register<SKCopyBuildFileTask>("skCopyBuildFile") {
            buildFiles.set(appProvider)
            this.outputDir.set(outputDirValue)
            this.versionCode.set(versionCodeProvider)
            this.addingVersionCodeAndDebug.set(true)
        }

        project.extensions.getByType<KotlinJvmProjectExtension>().conf()
        project.tasks.named("compileKotlin").configure { dependsOn(copyBuildFile) }

        project.dependencies.add("api", "${Versions.group}:modelcontract:${Versions.skot}")
    }

    private fun KotlinJvmProjectExtension.conf() {
        jvmToolchain(21)
        sourceSets.getByName("main").kotlin.srcDir("generated/main/kotlin")
    }
}
