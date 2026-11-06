package tech.skot.tools.generation.viewmodel

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import tech.skot.core.components.SKGenerics
import tech.skot.core.components.SKNoVM
import tech.skot.tools.generation.AndroidClassNames
import tech.skot.tools.generation.FrameworkClassNames
import tech.skot.tools.generation.Generator
import tech.skot.tools.generation.fileClassBuilder
import kotlin.reflect.full.hasAnnotation

@ExperimentalStdlibApi
fun Generator.generateViewModelTests() {
    val abstractTestScreen = ClassName(packageName = "$appPackage.screens", "TestScreen")
    if (!abstractTestScreen.existsJvmTestInModule(modules.viewmodel)) {
        abstractTestScreen.fileClassBuilder(
            listOfNotNull(
                viewModelModuleMock,
                rootState?.mockClassName,
                rootState?.let { ClassName(shortCuts.packageName, rootStatePropertyName!!) },
            ),
        ) {
            superclass(FrameworkClassNames.skTestViewModel)
            addModifiers(KModifier.ABSTRACT)
            addSuperclassConstructorParameter("${viewModelModuleMock.simpleName}/*add here libraries' modules*/")

            rootState?.let {
                addFunction(
                    FunSpec.builder("initStates")
                        .addAnnotation(AndroidClassNames.Annotations.before)
                        .addCode("$rootStatePropertyName = ${it.mockClassName.simpleName}()")
                        .build(),
                )
            }
        }.writeTo(jvmTestSources(modules.viewmodel))
    }
    components.forEach {
        // un ViewModel générique non annoté @SKGenerics est détecté dans sa source
        val typeParametersCount =
            typeParametersCountOf(it.viewModel().commonSourceInModule(modules.viewmodel), it.viewModel().simpleName)
                .coerceAtLeast(if (it.vc.hasAnnotation<SKGenerics>()) 1 else 0)
        if (!it.vc.hasAnnotation<SKNoVM>()) {
            it.viewModelTester().fileClassBuilder(
                suppressWarnings = listOf("unused")
            ) {
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("component", it.viewModel().withStarProjections(typeParametersCount))
                        .build()
                )
                superclass(
                    FrameworkClassNames.skViewModelTester.parameterizedBy(
                        it.viewMock(),
                        if (it.hasModel()) it.modelMock() else Unit::class.asTypeName(),
                    ),
                )
                addSuperclassConstructorParameter("component")
            }.writeTo(generatedJvmTestSources(modules.viewmodel))
        }
        // do not create class with suffix if prefix class already exists
        if (!it.vc.hasAnnotation<SKNoVM>() &&
            !it.testViewModel().existsJvmTestInModule(
                modules.viewmodel,
            ) && !it.oldTestViewModel().existsJvmTestInModule(modules.viewmodel)
        ) {
            it.testViewModel().fileClassBuilder(suppressWarnings = listOf("unused")) {
                superclass(abstractTestScreen)
                val componentVarName = if (it.isScreen) "screen" else "component"
                addFunction(
                    FunSpec.builder("tester")
                        .addParameter(componentVarName, it.viewModel().withStarProjections(typeParametersCount))
                        .returns(it.viewModelTester())
                        .addCode("return ${it.viewModelTester().simpleName}($componentVarName)")
                        .build()
                )
            }.writeTo(jvmTestSources(modules.viewmodel))
        }
    }
}

/**
 * Nombre de paramètres de type de la classe [simpleName] déclarée dans [source], 0 si elle n'est
 * pas générique ou si la source est absente. Seuls les paramètres de premier niveau sont comptés.
 */
fun typeParametersCountOf(source: String?, simpleName: String): Int {
    val declaration = source?.let { Regex("""\bclass\s+$simpleName\s*<""").find(it) } ?: return 0
    var depth = 1
    var count = 1
    for (char in source.substring(declaration.range.last + 1)) {
        when (char) {
            '<' -> depth++
            '>' -> if (--depth == 0) return count
            ',' -> if (depth == 1) count++
        }
    }
    return 0
}

fun ClassName.withStarProjections(count: Int): TypeName =
    if (count == 0) this else parameterizedBy(List(count) { STAR })
