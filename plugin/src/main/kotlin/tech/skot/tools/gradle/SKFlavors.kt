package tech.skot.tools.gradle

import java.io.File
import java.util.Properties

/**
 * Environnements et marques d'un projet SKot, déclarés dans [FILE_NAME] à la racine. Chaque valeur
 * devient un product flavor Android ; la première de chaque dimension est sélectionnée par défaut.
 * Copie conforme de tech.skot.tools.generation.SKFlavors (le plugin ne dépend pas du générateur).
 */
data class SKFlavors(val brands: List<String>, val environments: List<String>) {
    /** Dimensions dans l'ordre de priorité AGP : la marque d'abord, comme les anciennes combinaisons (kotlinSezaneProd). */
    val dimensions: List<String>
        get() = flavorsByDimension.keys.toList()

    val flavorsByDimension: Map<String, List<String>>
        get() = if (brands.isEmpty()) mapOf(ENVIRONMENT to environments) else mapOf(BRAND to brands, ENVIRONMENT to environments)

    /** Variantes marque×environnement, chacune avec ses source sets propres (main exclu). */
    val variants: List<SKVariant>
        get() =
            if (brands.isEmpty()) {
                environments.map { SKVariant(it, listOf(it)) }
            } else {
                brands.flatMap { brand ->
                    environments.map { environment ->
                        val combination = brand + environment.replaceFirstChar { it.uppercaseChar() }
                        SKVariant(combination, listOf(brand, environment, combination))
                    }
                }
            }

    /** Source sets propres aux variantes : marques, environnements et combinaisons. */
    val variantSourceSets: List<String>
        get() = variants.flatMap { it.sourceSets }.distinct()

    companion object {
        const val FILE_NAME = "skot_flavors.properties"
        const val BRAND = "brand"
        const val ENVIRONMENT = "environment"
        private val validName = Regex("[a-z][a-zA-Z0-9]*")
        private val reservedNames = setOf("main", "test", "androidTest", "testFixtures", "debug", "release")

        /** Flavors déclarés à la racine du projet, null si le projet n'en déclare pas. */
        fun read(rootDir: File): SKFlavors? = rootDir.resolve(FILE_NAME).takeIf { it.isFile }?.let { parse(it.readText()) }

        fun parse(text: String): SKFlavors {
            val properties = Properties().apply { load(text.reader()) }

            fun values(key: String) = properties.getProperty(key).orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val brands = values(BRAND)
            val environments = values(ENVIRONMENT)
            require(environments.isNotEmpty()) { "$FILE_NAME : la propriété $ENVIRONMENT doit lister au moins un environnement" }
            (brands + environments).forEach { name ->
                require(validName.matches(name)) { "$FILE_NAME : « $name » n'est pas un nom de flavor valide (lowerCamelCase, lettres et chiffres)" }
                require(name !in reservedNames) { "$FILE_NAME : « $name » est un nom réservé par Android Gradle Plugin" }
            }
            val duplicates = (brands + environments).groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            require(duplicates.isEmpty()) { "$FILE_NAME : flavors déclarés plusieurs fois : ${duplicates.joinToString()}" }
            return SKFlavors(brands, environments)
        }
    }
}

/** Une variante (sans le build type) et les source sets qu'elle ajoute à main. */
data class SKVariant(val name: String, val sourceSets: List<String>)
