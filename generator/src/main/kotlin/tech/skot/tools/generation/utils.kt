package tech.skot.tools.generation

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.superclasses

class ParamInfos(
    val name: String,
    val typeName: TypeName,
    val modifiers: List<KModifier> = emptyList(),
    val isVal: Boolean = true,
    val mutable: Boolean = false,
    val isPrivate: Boolean = false,
    val default: String? = null,
)

internal fun FileSpec.Builder.suppressWarningTypes(types: List<String>) {
    suppressWarningTypes(*types.toTypedArray())
}

internal fun FileSpec.Builder.suppressWarningTypes(vararg types: String) {
    if (types.isEmpty()) {
        return
    }

    val format = "%S,".repeat(types.size).trimEnd(',')
    addAnnotation(
        AnnotationSpec.builder(ClassName("", "Suppress"))
            .addMember(format, *types)
            .build()
    )
}

fun TypeSpec.Builder.addPrimaryConstructorWithParams(vals: List<ParamInfos>): TypeSpec.Builder {
    primaryConstructor(
        FunSpec.constructorBuilder()
            .apply {
                vals
                    .forEach {
                        addParameter(
                            ParameterSpec.builder(
                                it.name,
                                it.typeName,
                                it.modifiers.filter {
                                    it == KModifier.VARARG || it == KModifier.NOINLINE || it == KModifier.CROSSINLINE
                                },
                            )
                                .apply {
                                    if (it.default != null) {
                                        defaultValue(it.default)
                                    }
                                }
                                .build(),
                        )
                    }
            }
            .build(),
    )
    addProperties(
        vals.filter { it.isVal }.map {
            PropertySpec.builder(it.name, it.typeName)
                .mutable(it.mutable)
                .addModifiers(it.modifiers)
                .initializer(it.name)
                .apply {
                    if (it.isPrivate) {
                        addModifiers(KModifier.PRIVATE)
                    }
                }
                .build()
        },
    )
    return this
}

fun TypeName.nullable() = this.copy(true)

/**
 * Converts a reflected [KType] into a KotlinPoet [TypeName] fit for code generation.
 *
 * KotlinPoet's [asTypeName] copies the annotations of the *resolved classifier declaration* onto
 * the *type usage* it builds (see `ParameterizedTypeName.get(KClass, Boolean, List<KTypeProjection>)`,
 * which passes `effectiveType.annotations` to the resulting `ParameterizedTypeName`). That is wrong
 * in general: annotations borne by a class declaration are not applicable to usages of that type,
 * and nothing guarantees they are even visible or legal at that position.
 *
 * A concrete failure: since kotlin-stdlib 2.3.0, `kotlin.Pair` carries a runtime-visible
 * `@kotlin.js.JsImplicitExport`, an `internal`, `@Target(CLASS)` compiler annotation. Emitting it on
 * a parameter type made the generated mocks fail to compile with `Unresolved reference 'JsImplicitExport'`.
 * Rather than blacklisting that one annotation, we drop *every* annotation coming from the classifier.
 *
 * No user-written *use-site* annotation can regress here: [asTypeName] never reads
 * `KType.annotations`, so use-site annotations were never propagated in the first place. What this
 * does change — deliberately — is that *declaration* annotations of the classifier are now all
 * dropped, not just the `kotlin.js` ones: `kotlin.Result` carries `@JvmInline` and `java.util.Comparator`
 * carries `@FunctionalInterface`, both `@Target(CLASS)` and equally illegal at a type-usage position.
 */
fun KType.asCleanTypeName(): TypeName = asTypeName().withoutTypeAnnotations()

/**
 * Recursively drops the annotations carried by a [TypeName] and by every type nested inside it.
 *
 * Only meant for type names freshly derived from reflection (see [asCleanTypeName]); never apply it
 * to type names the generator itself built and deliberately annotated.
 */
fun TypeName.withoutTypeAnnotations(): TypeName =
    when (this) {
        is ParameterizedTypeName ->
            rawType.parameterizedBy(typeArguments.map { it.withoutTypeAnnotations() })
                .copy(nullable = isNullable, annotations = emptyList())
        is WildcardTypeName -> {
            // `producerOf` round-trips STAR (`out Any?`) unchanged, so no special case is needed.
            val bare =
                if (inTypes.size == 1) {
                    WildcardTypeName.consumerOf(inTypes.single().withoutTypeAnnotations())
                } else {
                    WildcardTypeName.producerOf(outTypes.single().withoutTypeAnnotations())
                }
            bare.copy(nullable = isNullable, annotations = emptyList())
        }
        // Les types fonction reviennent de la reflection en `Function1<...>` (ParameterizedTypeName),
        // jamais en LambdaTypeName : le cas général suffit et préserve la structure telle quelle.
        else -> copy(nullable = isNullable, annotations = emptyList())
    }

fun TypeName.simpleName(): String? =
    when {
        (this is ClassName) -> simpleName
        (this is ParameterizedTypeName) -> this.rawType.simpleName
        else -> null
    }

fun FileSpec.Builder.addImportClassName(className: ClassName) = addImport(className.packageName, className.simpleName)

fun FileSpec.Builder.addImportTypeName(typeName: TypeName) =
    when (typeName) {
        is ParameterizedTypeName -> addImportClassName(typeName.rawType)
        else -> addImportClassName(typeName as ClassName)
    }

fun String.packageToPathFragment() = replace('.', '/')

fun String.fullNameAsClassName() = ClassName(substring(0, lastIndexOf('.')), substring(lastIndexOf('.') + 1))

fun ClassName.fileClassBuilder(
    imports: List<ClassName> = emptyList(),
    suppressWarnings : List<String> = emptyList(),
    block: TypeSpec.Builder.() -> Unit,
) = FileSpec.builder(packageName, simpleName)
    .addKotlinDefaultImports()
    .indent("    ")
    .addType(
        TypeSpec.classBuilder(simpleName)
            .apply(block)
            .build(),
    )
    .addKotlinDefaultImports()
    .apply {
        imports.forEach {
            addImportClassName(it)
        }
        suppressWarningTypes(suppressWarnings)
    }
    .build()

fun ClassName.fileInterfaceBuilder(
    imports: List<ClassName> = emptyList(),
    suppressWarnings : List<String> = emptyList(),
    block: TypeSpec.Builder.() -> Unit,
) = FileSpec.builder(packageName, simpleName)
    .addKotlinDefaultImports()
    .indent("    ")
    .addType(
        TypeSpec.interfaceBuilder(simpleName)
            .apply(block)
            .build(),
    )
    .addKotlinDefaultImports()
    .apply {
        imports.forEach {
            addImportClassName(it)
        }
        suppressWarningTypes(suppressWarnings)
    }
    .build()

fun ClassName.fileObjectBuilder(
    imports: List<ClassName> = emptyList(),
    suppressWarnings : List<String> = emptyList(),
    block: TypeSpec.Builder.() -> Unit,
) = FileSpec.builder(packageName, simpleName)
    .addKotlinDefaultImports()
    .indent("    ")
    .addType(
        TypeSpec.objectBuilder(simpleName)
            .apply(block)
            .build(),
    )
    .addKotlinDefaultImports()
    .apply {
        imports.forEach {
            addImportClassName(it)
        }
        suppressWarningTypes(suppressWarnings)
    }
    .build()

fun Path.replaceSegment(
    segment: String,
    replacement: String,
): Path =
    map {
        if (it.toString() == segment) {
            replacement
        } else {
            it.toString()
        }
    }.let {
        Paths.get(it.joinToString("/", prefix = if (this.startsWith("/")) "/" else ""))
    }

fun KClass<*>.ownMembers(): List<KCallable<*>> {
    val superMembersNames = superclasses.flatMap { it.members.map { it.name } }
    return members.filter { !superMembersNames.contains(it.name) }
}

fun KClass<*>.ownProperties(): List<KCallable<*>> {
    val superTypePropertiesNames =
        superclasses[0].members.filterIsInstance<KProperty<*>>().map { it.name }
    return members.filterIsInstance<KProperty<*>>().filter { !superTypePropertiesNames.contains(it.name) }
}

fun KClass<*>.ownFuncs() = ownMembers().filterIsInstance(KFunction::class.java)

fun KClass<*>.funcs() = members.filterIsInstance(KFunction::class.java)

fun ClassName.withSuffix(suffix: String): ClassName = ClassName(packageName, simpleName.suffix(suffix))
