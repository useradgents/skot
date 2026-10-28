package tech.skot.tools.generation.resources

import tech.skot.tools.generation.SKFlavors
import java.nio.file.Path

/** Dossier res_referenced de chaque source set du module view : main, puis ceux des variantes. */
fun referencedResDirs(
    viewModuleDir: Path,
    flavors: SKFlavors?,
): Map<String, Path> =
    (listOf("main") + (flavors?.variantSourceSets ?: emptyList()))
        .associateWith { viewModuleDir.resolve("src/$it/res_referenced") }

/**
 * Les ressources référencées forment une seule interface, commune à toutes les variantes : chaque nom
 * doit donc être accessible dans chaque variante (par main, sa marque, son environnement ou leur
 * combinaison), sinon R.<type>.<nom> manque à la compilation de certaines variantes seulement.
 */
fun checkReferencedInEveryVariant(
    kind: String,
    namesBySourceSet: Map<String, Collection<String>>,
    flavors: SKFlavors?,
) {
    if (flavors == null) return
    val all = namesBySourceSet.values.flatten().toSet()
    val main = namesBySourceSet["main"].orEmpty().toSet()
    val missing =
        flavors.variants.mapNotNull { variant ->
            val visible = main + variant.sourceSets.flatMap { namesBySourceSet[it].orEmpty() }
            (all - visible).takeIf { it.isNotEmpty() }?.let { variant.name to it.sorted() }
        }
    check(missing.isEmpty()) {
        "Ressources $kind référencées absentes de certaines variantes :\n" +
            missing.joinToString("\n") { (variant, names) -> "  $variant : ${names.joinToString()}" }
    }
}
