package tech.skot.tools.generation.resources

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import tech.skot.tools.generation.Generator
import tech.skot.tools.generation.SuppressWarningsNames.resourcesWarning
import tech.skot.tools.generation.fileClassBuilder
import tech.skot.tools.generation.fileInterfaceBuilder
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.nameWithoutExtension

fun Generator.generateFonts() {
    println("fonts .........")
    println("generate Fonts interface .........")


    val fontsBySourceSet =
        referencedResDirs(rootPath.resolve(modules.view), flavors)
            .mapValues { (_, resDir) -> resDir.resolve("font") }
            .filterValues { Files.exists(it) }
            .mapValues { (sourceSet, dir) ->
                println("font directory ($sourceSet) : $dir")
                Files.list(dir).map { it.nameWithoutExtension }.collect(Collectors.toList()).filter { it.isNotBlank() }
            }
    checkReferencedInEveryVariant("font", fontsBySourceSet, flavors)

    val hasFont = fontsBySourceSet.isNotEmpty()

    val fonts = fontsBySourceSet.values.flatten().toSet()

    fonts.forEach {
        println("font found : $it")
    }

    fun String.toFontsPropertyName() = this

    fontsInterface.fileInterfaceBuilder(suppressWarnings = resourcesWarning){
        addProperties(
            fonts.map {
                PropertySpec.builder(it.toFontsPropertyName(), tech.skot.core.view.Font::class)
                    .build()
            },
        )
    }.writeTo(generatedCommonSources(modules.viewcontract))

    println("generate Fonts android implementation .........")
    fontsImpl.fileClassBuilder(imports = listOf(viewR),
        suppressWarnings = resourcesWarning) {
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
