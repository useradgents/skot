package tech.skot.tools.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.*
import tech.skot.Versions

open class SKPluginToolsExtension {
    var app: App? = null
}

data class App(
    val packageName: String,
    val startScreen: String,
    val rootState: String? = null,
    val baseActivity: String = ".android.BaseActivity",
    val feature: String? = null,
    // le fullname d'une variable globale donnant la classe de base
    val baseActivityVar: String? = null,
    val initializationPlans: List<String> = emptyList(),
    val iOs: Boolean = false,
    val referenceIconsByVariant: Boolean = false,
    val ktlintOnGeneratedFiles: Boolean = true,
)

data class FeatureModule(val packageName: String, val startScreen: String)

class PluginTools : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<SKPluginToolsExtension>("skot")
        project.plugins.apply("java-library")
        project.plugins.apply("kotlin")

        project.dependencies {
            dependencies(project)
        }

        val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class)
        val sourceSet = javaPluginExtension.sourceSets["main"]

        val trueProjectDir = project.parent?.projectDir ?: project.rootDir
        project.task("skClearGenerated", type = Delete::class) {
            doLast {
                println("####### clearGenerated  $trueProjectDir ")
                println("####### clearGenerated  feature ${extension.app?.feature} ")
                extension.app?.feature?.let {
                    println("--------------- one feature to delete: $trueProjectDir/$it/generated")
                    trueProjectDir.toPath().resolve("$it/generated").toFile().deleteRecursively()
                }
            }
            group = "Skot"
            delete =
                setOf(
                    "$trueProjectDir/viewcontract/generated",
                    "$trueProjectDir/modelcontract/generated",
                    "$trueProjectDir/androidApp/generated",
                    "$trueProjectDir/model/generated",
                    "$trueProjectDir/view/generated",
                    "$trueProjectDir/viewmodel/generated",
                )
        }

        project.task("skGenerate") {
            doLast {
                println("Skot version ${Versions.skot}")

                val app = extension.app
                if (app == null) {
                    println("rien à générer .........")
                } else {
                    println("génération .........")
                    project.javaexec {
                        mainClass.set("tech.skot.tools.generation.GenerateKt")
                        classpath = sourceSet.runtimeClasspath
                        args =
                            listOf(
                                app.packageName,
                                app.startScreen,
                                app.rootState.toString(),
                                app.baseActivity,
                                (project.parent?.projectDir ?: project.rootDir).toPath().toString(),
                                app.feature ?: "null",
                                app.baseActivityVar ?: "null",
                                if (app.initializationPlans.isEmpty()) {
                                    "null"
                                } else {
                                    app.initializationPlans.joinToString("_")
                                },
                                app.iOs.toString(),
                                app.referenceIconsByVariant.toString(),
                            )
                    }

                    if (app.ktlintOnGeneratedFiles) {
                        println("ktLint ......")
                        val srcs = "**/generated/**/*.kt"
                        try {
                            project.javaexec {
                                isIgnoreExitValue = true
                                workingDir = project.rootDir
                                mainClass.set("com.pinterest.ktlint.Main")
                                classpath = sourceSet.runtimeClasspath
                                args = listOf("-F", srcs)
                                jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
                            }
                        } catch (ex: Exception) {
                            println("@@@@@@@@@@@   erreur ktlint")
                            ex.printStackTrace()
                        }
                    }
                }
            }
            val compileTask = project.tasks.getByName("compileKotlin")
            dependsOn(compileTask)

            group = "Skot"
        }
    }

    private fun DependencyHandlerScope.dependencies(project: Project) {
        val parentProjectPath = project.parent?.path ?: ""

        this.add("implementation", project("$parentProjectPath:viewcontract"))
        this.add("implementation", project("$parentProjectPath:modelcontract"))
        this.add("api", "${Versions.group}:generator:${Versions.skot}")
        this.add("implementation", "com.pinterest.ktlint:ktlint-cli:${Versions.ktlint}")
    }
}
