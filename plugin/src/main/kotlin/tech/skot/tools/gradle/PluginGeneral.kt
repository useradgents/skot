package tech.skot.tools.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginGeneral : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("initialize"){
            group = "SKot"
            doLast {
                println("------- Generate from Plugin General essai")
            }
        }
    }
}
