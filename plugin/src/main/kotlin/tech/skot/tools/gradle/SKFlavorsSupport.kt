package tech.skot.tools.gradle

import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryProductFlavor
import org.gradle.api.GradleException
import org.gradle.api.Project
import tech.skot.Versions

/** Flavors du projet (fichier racine), null s'il n'en déclare pas. Une déclaration invalide arrête le build. */
internal fun Project.skFlavors(): SKFlavors? =
    try {
        SKFlavors.read(rootProject.rootDir)
    } catch (e: IllegalArgumentException) {
        throw GradleException("SKot ${Versions.skot} : ${e.message}", e)
    }

/**
 * Déclare les flavors du projet. Tous les modules Android SKot reçoivent exactement les mêmes, sans quoi
 * AGP ne sait pas associer les variantes d'un module à celles des modules dont il dépend.
 */
internal fun CommonExtension.skDeclareFlavors(flavors: SKFlavors?) {
    if (flavors == null) return
    flavorDimensions.addAll(flavors.dimensions)
    flavors.flavorsByDimension.forEach { (dimension, names) ->
        names.forEachIndexed { index, name ->
            productFlavors.create(name).apply {
                this.dimension = dimension
                if (index == 0) {
                    when (this) {
                        is LibraryProductFlavor -> isDefault = true
                        is ApplicationProductFlavor -> isDefault = true
                    }
                }
            }
        }
    }
}

/**
 * Arborescence standard d'AGP, plus les dossiers écrits par le générateur et, pour les modules de vue,
 * le dossier res_referenced de chaque source set (main et variantes) que le générateur scanne.
 */
internal fun CommonExtension.skStandardSourceSets(
    flavors: SKFlavors?,
    withReferencedResources: Boolean,
) {
    sourceSets.getByName("main").kotlin.directories.add("generated/main/kotlin")
    sourceSets.getByName("test").kotlin.directories.add("generated/test/kotlin")
    sourceSets.getByName("androidTest").kotlin.directories.add("generated/androidTest/kotlin")
    if (withReferencedResources) {
        (listOf("main") + (flavors?.variantSourceSets ?: emptyList())).forEach { sourceSet ->
            sourceSets.maybeCreate(sourceSet).res.directories.add("src/$sourceSet/res_referenced")
        }
    }
}

/** Le module :variant, s'il existe, porte le code propre à chaque variante. */
internal fun Project.skAddVariantModule(configuration: String) {
    rootProject.findProject(":variant")?.let { dependencies.add(configuration, it) }
}
