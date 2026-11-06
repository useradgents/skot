package tech.skot.tools.generation.viewmodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import tech.skot.model.SKData
import tech.skot.model.SKManualData
import tech.skot.model.SKPaginatedData
import tech.skot.tools.generation.FrameworkClassNames
import tech.skot.tools.generation.Generator
import tech.skot.tools.generation.ParamInfos
import tech.skot.tools.generation.addPrimaryConstructorWithParams
import tech.skot.tools.generation.fileClassBuilder
import tech.skot.tools.generation.isNullableString
import tech.skot.tools.generation.isString
import tech.skot.tools.generation.isUnit
import tech.skot.tools.generation.ownFuncs
import tech.skot.tools.generation.ownProperties
import tech.skot.tools.generation.primitiveDefaultInit
import tech.skot.tools.generation.asCleanTypeName
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

@ExperimentalStdlibApi
fun Generator.generateModelMock() {
    println("-----generateModelMock")
    components.forEach {
        if (it.hasModel()) {
            // un model a été défini (par convention de nommage)

            if (!it.modelMock().existsJvmTestInModule(modules.viewmodel)) {
                // pas d'implémentation spécifique trouvée on génère

                it.modelMock().fileClassBuilder {
                    addSuperinterface(it.modelContract())
                    addSuperinterface(FrameworkClassNames.coroutineScope)
                    addPrimaryConstructorWithParams(
                        listOf(
                            ParamInfos(
                                "coroutineContext",
                                FrameworkClassNames.coroutineContext,
                                listOf(KModifier.OVERRIDE),
                            ),
                        ) +
                            it.states.map {
                                ParamInfos(
                                    it.name,
                                    it.stateDef()!!.mockClassName,
                                    listOf(KModifier.OVERRIDE),
                                )
                            },
                    )

                    addModelMockMembers(it.modelClass!!, setOf("coroutineContext") + it.states.map { it.name })
                }
                    .writeTo(generatedJvmTestSources(modules.viewmodel))
            }
        }
    }
}

/**
 * Ajoute au mock d'un ModelContract ses propriétés et ses fonctions, sauf celles nommées dans
 * [constructorPropertiesNames], déjà implémentées par les paramètres du constructeur.
 */
@ExperimentalStdlibApi
fun TypeSpec.Builder.addModelMockMembers(
    modelClass: KClass<*>,
    constructorPropertiesNames: Set<String>,
) {
    // Les membres propres au contrat, plus les membres abstraits hérités de ses super-interfaces,
    // redéclarés en override ou non : le mock doit tous les implémenter.
    val ownMembers: Set<KCallable<*>> = (modelClass.ownProperties() + modelClass.ownFuncs()).toSet()
    val mockedMembers =
        modelClass.members.filter {
            (it.isAbstract || it in ownMembers) && it.name !in constructorPropertiesNames
        }

    mockedMembers.filterIsInstance<KProperty<*>>().forEach {
        // une propriété var du contrat ne peut être surchargée que par une var du même type
        val isVar = it is KMutableProperty<*>
        when {
            it.returnType.isString() || it.returnType.isNullableString() -> {
                addProperty(
                    PropertySpec.builder(it.name, it.returnType.asCleanTypeName())
                        .addModifiers(KModifier.OVERRIDE)
                        .mutable(true)
                        .initializer("\"${it.name}\"")
                        .build(),
                )
            }
            it.returnType.isSubtypeOf(typeOf<SKManualData<*>>()) -> {
                // SKManualData est une classe : elle sert elle-même de mock, sa valeur se pose
                addProperty(
                    PropertySpec.builder(it.name, it.returnType.asCleanTypeName())
                        .addModifiers(KModifier.OVERRIDE)
                        .mutable(true)
                        .apply {
                            if (it.returnType.jvmErasure == SKManualData::class &&
                                it.returnType.arguments.first().type?.isMarkedNullable == true
                            ) {
                                initializer("%T(null)", FrameworkClassNames.skManualData)
                            } else {
                                addModifiers(KModifier.LATEINIT)
                            }
                        }
                        .build(),
                )
            }
            it.returnType.isSubtypeOf(typeOf<SKPaginatedData<*>>()) -> {
                addProperty(
                    PropertySpec.builder(
                        it.name,
                        if (isVar) {
                            it.returnType.asCleanTypeName()
                        } else {
                            FrameworkClassNames.skPaginatedDataMock.parameterizedBy(
                                it.returnType.arguments.first().type!!.asCleanTypeName(),
                            )
                        },
                    )
                        .addModifiers(KModifier.OVERRIDE)
                        .mutable(isVar)
                        .initializer("%T(%S)", FrameworkClassNames.skPaginatedDataMock, it.name)
                        .build(),
                )
            }
            it.returnType.isSubtypeOf(typeOf<SKData<*>>()) -> {
                addProperty(
                    PropertySpec.builder(
                        it.name,
                        if (isVar) {
                            it.returnType.asCleanTypeName()
                        } else {
                            FrameworkClassNames.skDataMock.parameterizedBy(it.returnType.arguments.first().type!!.asCleanTypeName())
                        },
                    )
                        .addModifiers(KModifier.OVERRIDE)
                        .mutable(isVar)
                        .initializer("%T(%S)", FrameworkClassNames.skDataMock, it.name)
                        .build(),
                )
            }
            else -> {
                val primInit = it.returnType.primitiveDefaultInit()
                if (primInit != null) {
                    addProperty(
                        PropertySpec.builder(it.name, it.returnType.asCleanTypeName())
                            .addModifiers(KModifier.OVERRIDE)
                            .mutable(true)
                            .apply {
                                initializer(primInit)
                            }
                            .build(),
                    )
                } else {
                    addProperty(
                        PropertySpec.builder(it.name, it.returnType.asCleanTypeName())
                            .addModifiers(KModifier.OVERRIDE)
                            .mutable(true)
                            .apply {
                                if (it.returnType.isMarkedNullable) {
                                    initializer("null")
                                } else {
                                    addModifiers(KModifier.LATEINIT)
                                }
                            }
                            .build(),
                    )
                }
            }
        }
    }

    val functions = mockedMembers.filterIsInstance<KFunction<*>>()
    // chaque surcharge d'une fonction a son propre mock, numéroté dans l'ordre de ses paramètres
    val mockBaseNames: Map<KFunction<*>, String> =
        functions.groupBy { it.name }.values.flatMap { overloads ->
            if (overloads.size == 1) {
                listOf(overloads.first() to overloads.first().name)
            } else {
                overloads.sortedBy { it.parameters.joinToString { param -> param.type.toString() } }
                    .mapIndexed { index, overload -> overload to "${overload.name}${index + 1}" }
            }
        }.toMap()

    functions.forEach {
        val mockBaseName = mockBaseNames.getValue(it)
        val withParams = it.parameters.size > 1
        val returnsUnit = it.returnType.isUnit()
        val callClassName =
            if (withParams) {
                ClassName("", "${mockBaseName.capitalizeAsciiOnly()}Call")
            } else {
                Unit::class.asTypeName()
            }
        if (withParams) {
            addType(
                TypeSpec.classBuilder(callClassName)
                    .addModifiers(KModifier.DATA)
                    .addPrimaryConstructorWithParams(
                        it.parameters.mapNotNull { kParam ->
                            kParam.name?.let {
                                ParamInfos(it, kParam.type.asCleanTypeName())
                            }
                        },
                    )
                    .build(),
            )
        }
        addProperty(
            PropertySpec.builder(
                name = "${mockBaseName}Mock",
                type =
                    if (returnsUnit) {
                        FrameworkClassNames.skFunUnitMock.parameterizedBy(callClassName)
                    } else {
                        FrameworkClassNames.skFunMock.parameterizedBy(
                            callClassName,
                            it.returnType.asCleanTypeName(),
                        )
                    },
            )
                .initializer(
                    "${
                        (if (returnsUnit) FrameworkClassNames.skFunUnitMock else FrameworkClassNames.skFunMock).simpleName
                    }(\"${mockBaseName}Mock\")",
                )
                .build(),
        )

        fun callObject(func: KFunction<*>) =
            if (!withParams) {
                "Unit"
            } else {
                it.parameters
                    .mapNotNull { it.name }
                    .joinToString(
                        prefix = "${callClassName.simpleName}(",
                        postfix = ")",
                        separator = ", ",
                    )
            }
        addFunction(
            FunSpec.builder(it.name)
                .apply {
                    if (it.isSuspend) {
                        addModifiers(KModifier.SUSPEND)
                    }
                }
                .addParameters(
                    it.parameters.filter { it.kind == KParameter.Kind.VALUE }.map {
                        ParameterSpec(it.name!!, it.type.asCleanTypeName())
                    },
                )
                .returns(it.returnType.asCleanTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .addStatement("return ${mockBaseName}Mock(${callObject(it)})")
                .build(),
        )
    }
}
