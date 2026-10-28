package tech.skot.tools.generation.resources

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import tech.skot.core.view.Icon
import tech.skot.tools.generation.AndroidClassNames
import tech.skot.tools.generation.FrameworkClassNames
import tech.skot.tools.generation.Generator
import tech.skot.tools.generation.ParamInfos
import tech.skot.tools.generation.SuppressWarningsNames.resourcesWarning
import tech.skot.tools.generation.addPrimaryConstructorWithParams
import tech.skot.tools.generation.fileClassBuilder
import tech.skot.tools.generation.fileInterfaceBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.listDirectoryEntries

fun Generator.generateIcons() {
    println("icons .........")
    println("generate Icons interface .........")
    fun Path.listRes(): List<String> =
        if (!Files.exists(this)) {
            emptyList()
        } else {
            listDirectoryEntries()
                .map { it.fileName.toString() }
                .filter { !it.startsWith(".") }
                .map { it.substringBeforeLast(".") }
        }

    val iconsBySourceSet =
        referencedResDirs(rootPath.resolve(modules.view), flavors)
            .mapValues { (_, resDir) ->
                listOf("drawable", "drawable-xhdpi", "drawable-nodpi").flatMap { resDir.resolve(it).listRes() }
            }
    checkReferencedInEveryVariant("drawable", iconsBySourceSet, flavors)
    val icons: List<String> = iconsBySourceSet.values.flatten().distinct()

    fun String.toIconsPropertyName() = replaceFirstChar { it.lowercase(Locale.getDefault()) }

    iconsInterface.fileInterfaceBuilder(suppressWarnings = resourcesWarning) {
        addProperties(
            icons.map {
                PropertySpec.builder(it.toIconsPropertyName(), Icon::class)
                    .build()
            },
        )
        addFunction(
            FunSpec.builder("get")
                .addParameter("key", String::class)
                .returns(Icon::class.asTypeName().copy(nullable = true))
                .addModifiers(KModifier.ABSTRACT)
                .build(),
        )
    }.writeTo(
        generatedCommonSources(modules.viewcontract),
    )

    val funGetSpec =
        FunSpec.builder("get")
            .addAnnotation(
                AnnotationSpec.builder(ClassName("android.annotation", "SuppressLint"))
                    .addMember("value = [%S]", "DiscouragedApi")
                    .build(),
            )
            .addParameter("key", String::class)
            .returns(Icon::class.asTypeName().copy(nullable = true))
            .addStatement("val id = applicationContext.resources.getIdentifier(key,\"drawable\",applicationContext.packageName)")
            .beginControlFlow("return if(id > 0)")
            .addStatement("Icon(id)")
            .endControlFlow()
            .beginControlFlow("else")
            .addStatement("null")
            .endControlFlow()
            .addModifiers(KModifier.OVERRIDE)
            .build()

    println("generate Icons android implementation .........")
    iconsImpl.fileClassBuilder(
        imports = listOf(viewR),
        suppressWarnings = resourcesWarning
    ) {
        addSuperinterface(iconsInterface)
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
            icons.map {
                PropertySpec.builder(
                    it.toIconsPropertyName(),
                    Icon::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("Icon(R.drawable.$it)")
                    .build()
            },
        )
            .addFunction(funGetSpec)
    }
        .writeTo(
            generatedAndroidSources(feature ?: modules.app),
        )

    println("generate Icons android for view androidTests .........")
    iconsImpl.fileClassBuilder(
        imports = listOf(viewR),
        suppressWarnings = resourcesWarning
    ) {
        addSuperinterface(iconsInterface)
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
            icons.map {
                PropertySpec.builder(
                    it.toIconsPropertyName(),
                    Icon::class,
                    KModifier.OVERRIDE,
                )
                    .initializer("Icon(R.drawable.$it)")
                    .build()
            },
        )
            .addFunction(funGetSpec)
    }
        .writeTo(
            generatedAndroidTestSources(modules.view),
        )

    println("generate Icons mock  .........")
    iconsMock.fileClassBuilder(
        suppressWarnings = resourcesWarning
    ) {
        addSuperinterface(iconsInterface)
        addProperties(
            icons.map {
                PropertySpec.builder(
                    it.toIconsPropertyName(),
                    FrameworkClassNames.iconMock,
                    KModifier.OVERRIDE,
                )
                    .initializer("%T(%S)", FrameworkClassNames.iconMock, it.toIconsPropertyName())
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
                .returns(Icon::class.asTypeName().copy(nullable = true))
                .addStatement("return if (getReturnsNull) null else %T(key)", FrameworkClassNames.iconMock)
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
    }
        .writeTo(
            generatedJvmTestSources(feature ?: modules.viewmodel),
        )
}
