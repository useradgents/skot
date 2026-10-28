package tech.skot.tools.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/**
 * Module :variant (facultatif) : le code propre à chaque variante dont viewmodel, model et view ont
 * besoin, que les contrats JVM ne peuvent plus porter (TargetConstants…). Il reçoit les mêmes flavors
 * que les autres modules et ne dépend d'aucun module SKot.
 */
class PluginVariant : Plugin<Project> {
    override fun apply(project: Project) {
        project.skFailOnLegacyLayout()
        project.skFailOnKotlinMultiplatform()
        project.plugins.apply("com.android.library")
        project.skKotlinCompilerOptions()

        project.extensions.getByType<LibraryExtension>().apply {
            androidBaseConfig(project)
            skDeclareFlavors(project.skFlavors())
        }
    }
}
