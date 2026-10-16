package tech.skot.tools.gradle

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope
import java.nio.file.Files
import java.nio.file.Path

data class SKLibrary(val group: String, val version: String, val viewModule: String) {
    companion object {
        fun getDeclaredLibraries(path: Path): List<SKLibrary> {
            println("-------- getDeclaredLibraries   $path")
            return Files.readAllLines(path.resolve("skot_librairies.properties"))
                .filterNot {
                    it.startsWith("//")
                }
                .map { line ->
                    println("line: $line")
                    line.substringBefore(",").split(":")
                        .let {
                            try {
                                val viewModule = if (it.getOrNull(2).isNullOrBlank()) "viewlegacy" else it[2]
                                SKLibrary(it[0], it[1], viewModule)
                            } catch (ex: Exception) {
                                throw IllegalArgumentException("The library declaration \"$line\" is not wel formated")
                            }
                        }
                }
        }

        fun addDependenciesToLibraries(
            dependencies: DependencyHandler,
            path: Path,
            configuration: String,
            module: String,
        ) {
            getDeclaredLibraries(path).forEach {
                dependencies.add(configuration, "${it.group}:$module:${it.version}")
            }
        }

        fun addDependenciesToViewLegacy(
            dependenciesHandler: DependencyHandlerScope,
            path: Path,
        ) {
            getDeclaredLibraries(path)
                .forEach {
                    dependenciesHandler.skApiViewLegacy(it)
                }
        }

        fun DependencyHandlerScope.skApiViewLegacy(library: SKLibrary) =
            add("api", "${library.group}:${library.viewModule}:${library.version}")
    }
}
