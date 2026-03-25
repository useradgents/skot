package tech.skot.tools.generation.viewlegacy

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import tech.skot.tools.generation.ComponentDef
import tech.skot.tools.generation.FrameworkClassNames
import tech.skot.tools.generation.Generator
import tech.skot.tools.generation.addImportClassName
import tech.skot.tools.generation.addImportTypeName
import tech.skot.tools.generation.simpleName
import java.util.Locale

fun Generator.generateViewLegacyInjectorImpl(module: String) {
    FileSpec.builder(viewInjectorImpl.packageName, viewInjectorImpl.simpleName)
        .addKotlinDefaultImports()
        .indent("    ")
        .addType(
            TypeSpec.classBuilder(viewInjectorImpl.simpleName)
                .addSuperinterface(viewInjectorInterface)
                .addFunctions(
                    components.map { it.asViewLegacyInjection(this) },
                )
                .build(),
        )
        .apply {
            components.forEach {
                addImportClassName(it.proxy())
                it.subComponents.forEach {
                    addImportTypeName(it.type.toProxy())
                }
            }
        }
        .build()
        .writeTo(generatedAndroidSources(module))
}

fun ComponentDef.asViewLegacyInjection(generator: Generator) =
    FunSpec.builder(name.replaceFirstChar { it.lowercase(Locale.getDefault()) })
        .addModifiers(KModifier.OVERRIDE)
        .apply {
            if (isScreen) {
                addParameter(name = Generator.VISIBILITY_LISTENER_VAR_NAME, type = FrameworkClassNames.skVisiblityListener)
            }
            subComponents.forEach {
                addParameter(it.asParam())
            }
            fixProperties.forEach {
                addParameter(it.asParam())
            }
            mutableProperties.forEach {
                addParameter(it.initial().asParam())
            }
        }
        .returns(vc)
        .addCode(
            "return ${proxy().simpleName}(${if (isScreen) "${Generator.VISIBILITY_LISTENER_VAR_NAME}," else ""} ${(subComponents.map { "${it.name} as ${it.type.toProxy().simpleName()}" } + (fixProperties.map { it.name } + mutableProperties.map { it.initial().name })).joinToString(
                separator = ",",
            )})",
        )
        .build()
