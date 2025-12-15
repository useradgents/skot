package tech.skot.tools.generation.resources

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import tech.skot.core.view.ColorRef
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
import java.nio.file.Path
import java.util.stream.Collectors

fun String.toAndroidResourcePropertyName() = replace('.', '_')

fun Generator.generateColors() {
    println("colors .........")
    println("generate Colors interface .........")

    fun Path.list(): List<String> =
        if (!Files.exists(this)) {
            emptyList<String>()
        } else {
            Files.list(this).flatMap {
                it.getDocumentElement().childElements().stream().filter { it.tagName == "color" }
                    .map { it.getAttribute("name") }
            }.collect(Collectors.toList())
        }

    val colors = (rootPath.resolve(modules.view).resolve("src/androidMain/res_referenced/values").list() +
    if (referenceIconsByVariant) {
        variantsCombinaison.flatMap {
            rootPath.resolve(modules.view)
                .resolve("src/androidMain/res${it}_referenced/values").list()
        }
    } else {
        emptyList()
    }).toSet()

    fun String.toColorsPropertyName() = decapitalizeAsciiOnly().replace('.', '_')

    colorsInterface.fileInterfaceBuilder(suppressWarnings = resourcesWarning){
        addProperties(
            colors.map {
                PropertySpec.builder(
                    it.toColorsPropertyName(),
                    ColorRef::class,
                )
                    .build()
            },
        )
        addFunction(
            FunSpec.builder("get")
                .addParameter("key", String::class)
                .returns(ColorRef::class.asTypeName().copy(nullable = true))
                .addModifiers(KModifier.ABSTRACT)
                .build(),
        )
    }.writeTo(generatedCommonSources(modules.viewcontract))

    val funGetSpec =
        FunSpec.builder("get")
            .addParameter("key", String::class)
            .returns(ColorRef::class.asTypeName().copy(nullable = true))
            .addAnnotation(
                AnnotationSpec.builder(ClassName("android.annotation", "SuppressLint"))
                    .addMember("value = [%S]", "DiscouragedApi")
                    .build(),
            )
            .addStatement("val id = applicationContext.resources.getIdentifier(key,\"color\",applicationContext.packageName)")
            .beginControlFlow("return if(id > 0)")
            .addStatement("ColorRef(id)")
            .endControlFlow()
            .beginControlFlow("else")
            .addStatement("null")
            .endControlFlow()
            .addModifiers(KModifier.OVERRIDE)
            .build()

    println("generate Colors android implementation .........")
    colorsImpl.fileClassBuilder(
        imports = listOf(viewR),
        suppressWarnings = resourcesWarning
    ) {
        addSuperinterface(colorsInterface)
        addPrimaryConstructorWithParams(
            listOf(
                ParamInfos(
                    "applicationContext",
                    AndroidClassNames.context,
                    listOf(KModifier.PRIVATE),
                ),
            ),
        )
        addProperties(
            colors.map {
                PropertySpec.builder(
                    it.toColorsPropertyName(),
                    ColorRef::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("ColorRef(R.color.${it.toAndroidResourcePropertyName()})")
                    .build()
            },
        )
            .addFunction(funGetSpec)
    }
        .writeTo(generatedAndroidSources(feature ?: modules.app))

    println("generate Colors fot View Android Test .........")
    colorsImpl.fileClassBuilder(
        imports = listOf(viewR),
        suppressWarnings = resourcesWarning
    ) {
        addSuperinterface(colorsInterface)
        addPrimaryConstructorWithParams(
            listOf(
                ParamInfos(
                    "applicationContext",
                    AndroidClassNames.context,
                    listOf(KModifier.PRIVATE),
                ),
            ),
        )
        addProperties(
            colors.map {
                PropertySpec.builder(
                    it.toColorsPropertyName(),
                    ColorRef::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("ColorRef(R.color.${it.toAndroidResourcePropertyName()})")
                    .build()
            },
        )
            .addFunction(funGetSpec)
    }
        .writeTo(generatedAndroidTestSources(modules.view))

    println("generate Colors jvm mock .........")
    colorsMock.fileClassBuilder(
        suppressWarnings = resourcesWarning
    ) {
        addSuperinterface(colorsInterface)
        addProperties(
            colors.map {
                PropertySpec.builder(
                    it.toColorsPropertyName(),
                    ColorRef::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("ColorRef(\"${it.toColorsPropertyName()}\".hashCode())")
                    .build()
            },
        )

        addProperty(
            PropertySpec.builder(name = "getReturnsNull", type = Boolean::class)
                .mutable(true)
                .initializer("false")
                .build(),
        )
        addFunction(
            FunSpec.builder("get")
                .addParameter("key", String::class)
                .returns(ColorRef::class.asTypeName().copy(nullable = true))
                .addStatement("return if (getReturnsNull) null else ColorRef(key.hashCode())")
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
    }
        .writeTo(generatedJvmTestSources(feature ?: modules.viewmodel))
}
