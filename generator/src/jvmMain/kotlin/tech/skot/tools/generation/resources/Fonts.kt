package tech.skot.tools.generation.resources

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import tech.skot.tools.generation.Generator
import tech.skot.tools.generation.fileClassBuilder
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.nameWithoutExtension

fun Generator.generateFonts() {
    println("fonts .........")
    println("generate Fonts interface .........")
    val fontsDirectory =
        rootPath.resolve(modules.view).resolve("src/androidMain/res_referenced/font")

    val fonts =
        if (!Files.exists(fontsDirectory)) {
            emptyList()
        } else {
            Files.list(fontsDirectory).map {
                it.nameWithoutExtension
            }.collect(Collectors.toList()).filter { it.isNotBlank() }
        }

    fun String.toFontsPropertyName() = this

    FileSpec.builder(
        fontsInterface.packageName,
        fontsInterface.simpleName,
    ).addType(
        TypeSpec.interfaceBuilder(fontsInterface.simpleName)
            .addProperties(
                fonts.map {
                    PropertySpec.builder(it.toFontsPropertyName(), tech.skot.core.view.Font::class)
                        .build()
                },
            )
            .build(),
    )
        .build()
        .writeTo(generatedCommonSources(modules.viewcontract))

    println("generate Fonts android implementation .........")
    fontsImpl.fileClassBuilder(listOf(viewR)) {
        addSuperinterface(fontsInterface)
        addProperties(
            fonts.map {
                PropertySpec.builder(
                    it.toFontsPropertyName(),
                    tech.skot.core.view.Font::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("Font(R.font.${it.toAndroidResourcePropertyName()})")
                    .build()
            },
        )
    }
        .writeTo(generatedAndroidSources(feature ?: modules.app))

    println("generate Fonts fot View Android Test .........")
    fontsImpl.fileClassBuilder(listOf(viewR)) {
        addSuperinterface(fontsInterface)
        addProperties(
            fonts.map {
                PropertySpec.builder(
                    it.toFontsPropertyName(),
                    tech.skot.core.view.Font::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("Font(R.font.${it.toAndroidResourcePropertyName()})")
                    .build()
            },
        )
    }
        .writeTo(generatedAndroidTestSources(modules.view))

    println("generate Fonts jvm mock .........")
    fontsMock.fileClassBuilder {
        addSuperinterface(fontsInterface)
        addProperties(
            fonts.map {
                PropertySpec.builder(
                    it.toFontsPropertyName(),
                    tech.skot.core.view.Font::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("Font(\"${it.toFontsPropertyName()}\".hashCode())")
                    .build()
            },
        )
    }
        .writeTo(generatedJvmTestSources(feature ?: modules.viewmodel))
}
