package tech.skot.tools.generation.resources

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import tech.skot.tools.generation.AndroidClassNames
import tech.skot.tools.generation.Generator
import tech.skot.tools.generation.ParamInfos
import tech.skot.tools.generation.SuppressWarningsNames.resourcesWarning
import tech.skot.tools.generation.addPrimaryConstructorWithParams
import tech.skot.tools.generation.childElements
import tech.skot.tools.generation.fileClassBuilder
import tech.skot.tools.generation.fileInterfaceBuilder
import tech.skot.tools.generation.getDocumentElement
import java.nio.file.Files
import java.util.stream.Collectors

fun Generator.generatePlurals() {
    val values = rootPath.resolve(modules.view).resolve("src/androidMain/res_referenced/values")

    println("plurals .........")
    val plurals =
        if (!Files.exists(values)) {
            emptyList()
        } else {
            Files.list(values).filter { it.fileName.toString().startsWith("strings") }.flatMap {
                it.getDocumentElement().childElements().stream().filter { it.tagName == "plurals" }
                    .map { it.getAttribute("name") }
            }.collect(Collectors.toList())
        }

    fun String.toPluralsFunNAme() = decapitalizeAsciiOnly().replace('.', '_')

    pluralsInterface.fileInterfaceBuilder(
        suppressWarnings = resourcesWarning
    ) {
        addFunctions(
            plurals.map {
                FunSpec.builder(it.toPluralsFunNAme())
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameter("quantity", Int::class)
                    .addParameter("formatArgs", Any::class, KModifier.VARARG)
                    .returns(String::class)
                    .build()
            },
        )
    }.writeTo(generatedCommonSources(modules.modelcontract))

    fun TypeSpec.Builder.pluralsImplTypeSpec(override: Boolean) {
        if (override) {
            addSuperinterface(pluralsInterface)
        }
        addPrimaryConstructorWithParams(
            listOf(
                ParamInfos(
                    "applicationContext",
                    AndroidClassNames.context,
                    listOf(KModifier.PRIVATE),
                ),
            ),
        )
        addFunction(
            FunSpec.builder("compute")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("pluralId", Int::class)
                .addParameter("quantity", Int::class)
                .addParameter("formatArgs", Any::class, KModifier.VARARG)
                .returns(String::class)
                .beginControlFlow("return if (formatArgs.isEmpty())")
                .addStatement("applicationContext.resources.getQuantityString(pluralId, quantity)")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("applicationContext.resources.getQuantityString(pluralId, quantity, *formatArgs)")
                .endControlFlow()
                .build(),
        )
        addFunctions(
            plurals.map {
                FunSpec.builder(it.toPluralsFunNAme())
                    .apply {
                        if (override) {
                            addModifiers(KModifier.OVERRIDE)
                        }
                    }
                    .addParameter("quantity", Int::class)
                    .addParameter("formatArgs", Any::class, KModifier.VARARG)
                    .returns(String::class)
                    .addCode(
                        "return compute(R.plurals.${
                            it.replace(
                                '.',
                                '_',
                            )
                        }, quantity, *formatArgs)",
                    )
                    .build()
            },
        )
    }

    pluralsImpl.fileClassBuilder(
        imports = listOf(viewR),
        suppressWarnings = resourcesWarning
    ) {
        pluralsImplTypeSpec(true)
    }
        .apply {
            writeTo(generatedAndroidSources(feature ?: modules.app))
        }

    pluralsImpl.fileClassBuilder(
        imports = listOf(viewR),
        suppressWarnings = resourcesWarning
    ) {
        pluralsImplTypeSpec(false)
    }
        .apply {
            writeTo(generatedAndroidTestSources(modules.view))
        }

    println("generate Plurals jvm mock .........")
    pluralsMock.fileClassBuilder(
        suppressWarnings = resourcesWarning
    ) {
        addSuperinterface(pluralsInterface)
        addFunctions(
            plurals.map {
                FunSpec.builder(it.toPluralsFunNAme())
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("quantity", Int::class)
                    .addParameter("formatArgs", Any::class, KModifier.VARARG)
                    .returns(String::class)
                    .addCode("return \"${it.toPluralsFunNAme()}_\${quantity}_\${formatArgs.joinToString(\"_\")}\"")
                    .build()
            },
        )
    }
        .writeTo(generatedJvmTestSources(feature ?: modules.viewmodel))
}
