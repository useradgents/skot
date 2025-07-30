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
import java.util.stream.Collectors

fun Generator.generateIcons() {
    println("icons .........")
    println("@@@@@@@@@ with referenceIconsByVariant = $referenceIconsByVariant")
    println("generate Icons interface .........")
    val drawableDir =
        rootPath.resolve(modules.view).resolve("src/androidMain/res_referenced/drawable")
    val drawableXhdpiDir =
        rootPath.resolve(modules.view).resolve("src/androidMain/res_referenced/drawable-xhdpi")
    val drawableNodpiDir =
        rootPath.resolve(modules.view).resolve("src/androidMain/res_referenced/drawable-nodpi")

    fun Path.listRes(): List<String> =
        if (!Files.exists(this)) {
            emptyList<String>()
        } else {
            Files.list(this).map { it.fileName.toString().substringBeforeLast(".") }
                .collect(Collectors.toList())
        }

    val icons: List<String> =
        drawableDir.listRes() + drawableXhdpiDir.listRes() + drawableNodpiDir.listRes() +
                if (referenceIconsByVariant) {
                    variantsCombinaison.flatMap {
                        rootPath.resolve(modules.view)
                            .resolve("src/androidMain/res${it}_referenced/drawable-xhdpi")
                            .listRes() +
                                rootPath.resolve(modules.view)
                                    .resolve("src/androidMain/res${it}_referenced/drawable")
                                    .listRes() +
                                rootPath.resolve(modules.view)
                                    .resolve("src/androidMain/res${it}_referenced/drawable-nodpi")
                                    .listRes()
                    }
                } else {
                    emptyList()
                }

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
        generatedCommonSources(
            modules.viewcontract,
            if (referenceIconsByVariant) mainVariant else null,
        ),
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
            generatedAndroidSources(
                feature ?: modules.app,
                if (referenceIconsByVariant) mainVariant else null,
            ),
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
            generatedAndroidTestSources(
                modules.view,
                if (referenceIconsByVariant) mainVariant else null,
            ),
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
                    .initializer("IconMock(\"${it.toIconsPropertyName()}\")")
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
                .addStatement("return if (getReturnsNull) null else IconMock(key)")
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
    }
        .writeTo(
            generatedJvmTestSources(
                feature ?: modules.viewmodel,
                if (referenceIconsByVariant) mainVariant else null,
            ),
        )
}
