package tech.skot.tools.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
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
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import tech.skot.Versions

open class SKPluginModelContractExtension {
    var buildFiles: List<Any>? = null
}

abstract class SKCopyBuildFileTask : DefaultTask() {
    @get:Internal
    abstract val buildFiles: ListProperty<Any>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val versionCode: Property<Int>

    @get:Input
    abstract val addingVersionCodeAndDebug: Property<Boolean>

    @get:Input
    abstract val debug: Property<Boolean>

    @TaskAction
    fun copy() {
        buildFiles.get().forEach {
            copyBuildFileToImplementation(
                build = it,
                outputDir = outputDir.get().asFile,
                versionCode = versionCode.get(),
                addingVersionCodeAndDebug = addingVersionCodeAndDebug.get(),
                debug = debug.get()
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
class PluginModelContract : Plugin<Project> {
    override fun apply(project: Project) {
        println("==== PluginModelContract.apply() DEBUT pour ${project.name} ====")
        val extension = project.extensions.create<SKPluginModelContractExtension>("skot")
        project.plugins.apply("com.android.kotlin.multiplatform.library")
        project.plugins.apply("maven-publish")
        project.plugins.apply("kotlinx-serialization")

        val outputDirValue = project.layout.projectDirectory.dir("generated/commonMain/kotlin")
        val appProvider = project.provider { extension.buildFiles ?: emptyList<Any>() }
        val versionCodeProvider = project.providers.provider { project.skVersionCode() }

        val copyDebug = project.tasks.register<SKCopyBuildFileTask>("skCopyBuildFileDebug") {
            buildFiles.set(appProvider)
            this.outputDir.set(outputDirValue)
            this.versionCode.set(versionCodeProvider)
            this.addingVersionCodeAndDebug.set(true)
            this.debug.set(true)
            onlyIf {
                project.gradle.taskGraph.allTasks.any { it.name == "preDebugBuild" }
            }
        }

        val copyRelease = project.tasks.register<SKCopyBuildFileTask>("skCopyBuildFileRelease") {
            buildFiles.set(appProvider)
            this.outputDir.set(outputDirValue)
            this.versionCode.set(versionCodeProvider)
            this.addingVersionCodeAndDebug.set(true)
            this.debug.set(false)
            onlyIf {
                project.gradle.taskGraph.allTasks.any { it.name == "preReleaseBuild" }
            }
        }

        project.extensions.findByType(KotlinMultiplatformAndroidLibraryExtension::class)?.androidBaseConfig(project)
        project.extensions.findByType(KotlinMultiplatformExtension::class)?.conf(project)

        project.afterEvaluate {
            project.extensions.findByType(KotlinMultiplatformExtension::class)
                ?.targets
                ?.flatMap { it.compilations }
                ?.forEach { compilation ->
                    compilation.compileTaskProvider.configure {
                        dependsOn(copyDebug, copyRelease)
                    }
                }
        }
    }

    private fun KotlinMultiplatformExtension.conf(project: Project) {
        jvmToolchain(17)
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
            optIn.add("kotlin.time.ExperimentalTime")
        }
        jvm()

        sourceSets["commonMain"].kotlin.srcDir("generated/commonMain/kotlin")
        sourceSets["commonMain"].dependencies {
            api("${Versions.group}:modelcontract:${Versions.skot}")
        }

        skVariantsCombinaison(project.rootProject.rootDir.toPath()).forEach {
            sourceSets["commonMain"].kotlin.srcDir("src/commonMain/kotlin$it")
            sourceSets["androidMain"].kotlin.srcDir("src/androidMain/kotlin$it")
        }
    }
}
