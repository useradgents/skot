package tech.skot.tools

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import tech.skot.tools.generation.ParamInfos
import tech.skot.tools.generation.addPrimaryConstructorWithParams
import tech.skot.tools.generation.asCleanTypeName
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Non-régression : KotlinPoet recopie les annotations de la *déclaration* du classifieur sur
 * l'*usage* du type (cf. ParameterizedTypeName.get(KClass, Boolean, List<KTypeProjection>)).
 * Le générateur ne doit jamais les propager dans le code généré.
 */
class TypeNameAnnotationsTests {
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class DeclarationOnly

    @DeclarationOnly
    class Box<T>(val value: T)

    /** Le contrat exact demandé : un paramètre `Pair<Double, Double>` en commonMain. */
    interface GeoContract {
        suspend fun getLatLngAddress(latLng: Pair<Double, Double>): String
    }

    private val latLngType
        get() = GeoContract::class.memberFunctions
            .single { it.name == "getLatLngAddress" }
            .parameters
            .single { it.name == "latLng" }
            .type

    /**
     * Garde-fou : si ce test échoue, KotlinPoet a cessé de propager les annotations du classifieur
     * et les tests ci-dessous seraient devenus tautologiques.
     */
    @Test
    fun kotlinPoetPropagatesDeclarationAnnotationsOntoTypeUsages() {
        val raw = Box::class.constructors.first().parameters.first().type // T
        // Cas générique explicite : Box<String> porte @DeclarationOnly sur l'usage.
        val boxType = BoxHolder::class.memberFunctions.single { it.name == "take" }
            .parameters.single { it.name == "box" }.type
        assertTrue(
            boxType.asTypeName().annotations.isNotEmpty(),
            "KotlinPoet ne propage plus les annotations du classifieur : ce test de garde est à revoir (raw=$raw)",
        )
    }

    interface BoxHolder {
        fun take(box: Box<String>)
    }

    @Test
    fun asCleanTypeNameDropsDeclarationAnnotations() {
        val boxType = BoxHolder::class.memberFunctions.single { it.name == "take" }
            .parameters.single { it.name == "box" }.type

        val clean = boxType.asCleanTypeName()
        assertTrue(clean.annotations.isEmpty(), "annotation résiduelle sur l'usage du type : $clean")
        assertTrue(
            (clean as ParameterizedTypeName).typeArguments.all { it.annotations.isEmpty() },
            "annotation résiduelle sur un argument de type : $clean",
        )
        assertFalse('@' in clean.toString(), "le type rendu contient une annotation : $clean")
    }

    /**
     * Le correctif ne doit pas se réduire à une liste noire de `kotlin.js` : d'autres annotations
     * de déclaration, internes au compilateur, sont déjà propagées aujourd'hui. `kotlin.Result` porte
     * `@JvmInline` (cible CLASS) — illégale elle aussi en position d'usage de type.
     */
    interface ResultContract {
        fun handle(result: Result<String>)
    }

    @Test
    fun nonJsCompilerAnnotationsAreStrippedToo() {
        val resultType = ResultContract::class.memberFunctions.single { it.name == "handle" }
            .parameters.single { it.name == "result" }.type

        assertTrue(
            "JvmInline" in resultType.asTypeName().toString(),
            "garde-fou : kotlin.Result ne porte plus @JvmInline, ce test est à revoir",
        )
        assertEquals("kotlin.Result<kotlin.String>", resultType.asCleanTypeName().toString())
    }

    /** Les annotations imbriquées (arguments de type) doivent être retirées récursivement. */
    interface NestedContract {
        fun handle(items: List<Pair<Int, Int>>, callback: (Pair<Int, Int>) -> Unit)
    }

    @Test
    fun nestedAnnotationsAreStrippedRecursively() {
        val fn = NestedContract::class.memberFunctions.single { it.name == "handle" }
        fn.parameters.filter { it.name != null }.forEach { p ->
            val rendered = p.type.asCleanTypeName().toString()
            assertFalse('@' in rendered, "annotation imbriquée résiduelle sur ${p.name} : $rendered")
        }
    }

    @Test
    fun pairParameterRendersWithoutAnyAnnotation() {
        assertEquals("kotlin.Pair<kotlin.Double, kotlin.Double>", latLngType.asCleanTypeName().toString())
    }

    /** Reproduit la forme exacte du mock généré : l'override et la data class `*Call`. */
    @Test
    fun generatedMockShapeContainsNoAnnotation() {
        val paramType = latLngType.asCleanTypeName()

        val overrideFun = FunSpec.builder("getLatLngAddress")
            .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
            .addParameter(ParameterSpec("latLng", paramType))
            .returns(String::class.asTypeName())
            .addStatement("return getLatLngAddressMock(GetLatLngAddressCall(latLng))")
            .build()

        val callClass = TypeSpec.classBuilder("GetLatLngAddressCall")
            .addModifiers(KModifier.DATA)
            .addPrimaryConstructorWithParams(listOf(ParamInfos("latLng", paramType)))
            .build()

        listOf(overrideFun.toString(), callClass.toString()).forEach { rendered ->
            assertFalse(
                "JsImplicitExport" in rendered,
                "annotation interne au compilateur dans le code généré :\n$rendered",
            )
            assertFalse('@' in rendered, "annotation inattendue dans le code généré :\n$rendered")
        }
    }
}
