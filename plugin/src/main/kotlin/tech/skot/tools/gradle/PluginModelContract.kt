package tech.skot.tools.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import tech.skot.Versions

open class SKPluginModelContractExtension {
    var buildFiles: List<Any>? = null
}

@Suppress( "UNUSED_PARAMETER")
class PluginModelContract : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<SKPluginModelContractExtension>("skot")
        project.plugins.apply("com.android.library")
        project.plugins.apply("maven-publish")
        project.plugins.apply("kotlinx-serialization")

        project.extensions.findByType(LibraryExtension::class)?.conf(project, extension)

        project.extensions.findByType(KotlinMultiplatformExtension::class)?.conf(project)

        project.afterEvaluate {
            project.tasks.getByName("preDebugBuild").doFirst {
                extension.buildFiles?.let {
                    it.forEach {
                        copyBuildFileToImplementation(
                            build = it,
                            project = project,
                            addingVersionCodeAndDebug = true,
                            debug = true
                        )
                    }
                }
            }
            project.tasks.getByName("preReleaseBuild").doFirst {
                extension.buildFiles?.let {
                    it.forEach {
                        copyBuildFileToImplementation(
                            build = it,
                            project = project,
                            addingVersionCodeAndDebug = true,
                            debug = false
                        )
                    }
                }
            }
            project.tasks.getByName("compileKotlinJvm").doFirst {
                extension.buildFiles?.let {
                    it.forEach {
                        copyBuildFileToImplementation(
                            build = it,
                            project = project,
                            addingVersionCodeAndDebug = true,
                            debug = false
                        )
                    }
                }
            }
        }
    }

    private fun LibraryExtension.conf(
        project: Project,
        extension: SKPluginModelContractExtension,
    ) {
        androidBaseConfig(project)

        sourceSets {
            getByName("main").java.srcDirs("generated/androidMain/kotlin")
            getByName("main").java.srcDirs("src/androidMain/kotlin")
            getByName("main").manifest.srcFile("src/androidMain/AndroidManifest.xml")
            getByName("main").res.srcDir("src/androidMain/res")
        }
    }

    private fun KotlinMultiplatformExtension.conf(project: Project) {
        jvmToolchain(17)
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            optIn.add("kotlin.time.ExperimentalTime")
        }
        jvm()
        androidTarget {
        }

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
