package tech.skot.tools.gradle

import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

private val skEnv = com.squareup.kotlinpoet.ClassName("tech.skot.core", "SKEnv")

fun copyBuildFileToImplementation(
    build: Any,
    outputDir: java.io.File,
    versionCode: Int,
    addingVersionCodeAndDebug: Boolean,
) {
    println("Copy of build object :${build::class.simpleName} addingVersionCodeAndDebug: $addingVersionCodeAndDebug")
    val stringType = String::class.createType()
    val intType = Int::class.createType()

    val buildObjectType = build::class.asTypeName()
    val file =
        com.squareup.kotlinpoet.FileSpec.builder(
            buildObjectType.packageName,
            buildObjectType.simpleName,
        )
            .addKotlinDefaultImports()
            .indent("    ")
    val classBuilderCommon =
        com.squareup.kotlinpoet.TypeSpec.objectBuilder(buildObjectType.simpleName)
            .apply {
                if (addingVersionCodeAndDebug) {
                    addProperty(
                        com.squareup.kotlinpoet.PropertySpec.builder(
                            "versionCode",
                            Int::class,
                            com.squareup.kotlinpoet.KModifier.CONST,
                        )
                            .initializer(versionCode.toString())
                            .build(),
                    )

                    // Valeur d'exécution, posée par SKEnvInitProvider : une constante de
                    // compilation ne peut pas être juste ici, le fichier étant partagé par
                    // toutes les variantes.
                    addProperty(
                        com.squareup.kotlinpoet.PropertySpec.builder(
                            "debug",
                            Boolean::class,
                        )
                            .getter(
                                com.squareup.kotlinpoet.FunSpec.getterBuilder()
                                    .addStatement("return %T.debug", skEnv)
                                    .build(),
                            )
                            .build(),
                    )
                }

                build::class.memberProperties.forEach {
                    when (it.returnType) {
                        stringType -> {
                            addProperty(
                                com.squareup.kotlinpoet.PropertySpec.builder(
                                    it.name,
                                    String::class,
                                    com.squareup.kotlinpoet.KModifier.CONST,
                                )
                                    .initializer("\"${it.call()}\"")
                                    .build(),
                            )
                        }

                        intType -> {
                            addProperty(
                                com.squareup.kotlinpoet.PropertySpec.builder(
                                    it.name,
                                    Int::class,
                                    com.squareup.kotlinpoet.KModifier.CONST,
                                )
                                    .initializer(it.call().toString())
                                    .build(),
                            )
                        }
                    }
                }
            }
    file.addType(classBuilderCommon.build())
    file.build().writeTo(outputDir)
}
