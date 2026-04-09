package tech.skot.tools.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.register
import org.gradle.process.ExecOperations
import tech.skot.Versions
import javax.inject.Inject

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
    val referenceIconsByVariant: Boolean = false,
    val referenceFontsByVariant: Boolean = false,
    val referenceColorsByVariant: Boolean = false,
    val ktlintOnGeneratedFiles: Boolean = true,
)

data class FeatureModule(val packageName: String, val startScreen: String)

abstract class SkGenerateTask : DefaultTask() {
    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val startScreen: Property<String>

    @get:Input
    @get:Optional
    abstract val rootState: Property<String>

    @get:Input
    abstract val baseActivity: Property<String>

    @get:Input
    abstract val projectDir: Property<String>

    @get:Input
    @get:Optional
    abstract val feature: Property<String>

    @get:Input
    @get:Optional
    abstract val baseActivityVar: Property<String>

    @get:Input
    abstract val initializationPlans: Property<String>

    @get:Input
    abstract val referenceIconsByVariant: Property<Boolean>

    @get:Input
    abstract val referenceFontsByVariant: Property<Boolean>

    @get:Input
    abstract val referenceColorsByVariant: Property<Boolean>

    @get:Input
    abstract val ktlintOnGeneratedFiles: Property<Boolean>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootDir: DirectoryProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun generate() {
        if (!packageName.isPresent) {
            println("rien à générer .........")
            return
        }
        println("Skot version ${Versions.skot}")

        println("génération .........")

        execOperations.javaexec {
            mainClass.set("tech.skot.tools.generation.GenerateKt")
            classpath = runtimeClasspath
            args =
                listOf(
                    packageName.get(),
                    startScreen.get(),
                    rootState.getOrElse("null"),
                    baseActivity.get(),
                    projectDir.get(),
                    feature.getOrElse("null"),
                    baseActivityVar.getOrElse("null"),
                    initializationPlans.get(),
                    referenceIconsByVariant.get().toString(),
                    referenceFontsByVariant.get().toString(),
                    referenceColorsByVariant.get().toString(),)
        }

        if (ktlintOnGeneratedFiles.get()) {
            println("ktLint ......")
            val srcs = "**/generated/**/*.kt"
            try {
                execOperations.javaexec {
                    isIgnoreExitValue = true
                    workingDir = rootDir.get().asFile
                    mainClass.set("com.pinterest.ktlint.Main")
                    classpath = runtimeClasspath
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

class PluginTools : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<SKPluginToolsExtension>("skot")
        project.plugins.apply("java-library")
        project.plugins.apply("kotlin")

        project.dependencies {
            dependencies(project)
        }

        val trueProjectDir = project.parent?.projectDir ?: project.rootDir
        project.tasks.register<Delete>("skClearGenerated") {
            group = "Skot"
            delete(
                "$trueProjectDir/viewcontract/generated",
                "$trueProjectDir/modelcontract/generated",
                "$trueProjectDir/androidApp/generated",
                "$trueProjectDir/model/generated",
                "$trueProjectDir/view/generated",
                "$trueProjectDir/viewmodel/generated",
            )
            val appProvider = project.provider { extension.app }
            delete(appProvider.map { it.feature?.let { "$trueProjectDir/$it" + "/generated" } ?: emptyList<Any>() })
        }

        project.tasks.register<SkGenerateTask>("skGenerate") {
            val appProvider = project.provider { extension.app }
            packageName.set(appProvider.map { it.packageName })
            startScreen.set(appProvider.map { it.startScreen })
            rootState.set(appProvider.map { it.rootState })
            baseActivity.set(appProvider.map { it.baseActivity })
            projectDir.set(trueProjectDir.absolutePath)
            feature.set(appProvider.map { it.feature})
            baseActivityVar.set(appProvider.map { it.baseActivityVar})
            initializationPlans.set(appProvider.map {
                it.initializationPlans.joinToString("_")
            })
            referenceIconsByVariant.set(appProvider.map { it.referenceIconsByVariant })
            ktlintOnGeneratedFiles.set(appProvider.map { it.ktlintOnGeneratedFiles })
            runtimeClasspath.from(project.configurations.named("runtimeClasspath"))
            runtimeClasspath.from(
                project.extensions.getByType(SourceSetContainer::class.java)
                    .getByName("main").output
            )
            rootDir.set(project.layout.projectDirectory)

            val compileTask = project.tasks.named("compileKotlin")
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
