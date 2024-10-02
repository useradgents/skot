group = Versions.group
version = Versions.version

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
    // id("com.gradle.plugin-publish") version "1.3.0"
    signing
}

dependencies {
    api(libs.gradle)
    api(libs.kotlin.gradle.plugin)
    api(libs.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json)

    api(libs.kotlinpoet)

    testImplementation(libs.jetbrains.kotlin.test.junit)
}

// configurations {
//    all {
//        attributes {
//            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
//        }
//    }
// }

val gradlePortal = false

//pluginBundle {
//    website = "https://github.com/skot-framework/skot"
//    vcsUrl = "https://github.com/skot-framework/skot/tree/master"
//    tags = listOf("skot", "kotlin", "kmm")
//    //mavenCoordinates { }
//}

gradlePlugin {
    plugins {

        create("SkotStarter") {
            id = "tech.skot.starter"
            displayName = "SKot starter"
            description = "Starting a Skot Project"
            implementationClass = "tech.skot.tools.gradle.PluginStarter"
        }

        if (!gradlePortal) {
            create("SkotModelContract") {
                id = "tech.skot.modelcontract"
                displayName = "SKot modelcontract"
                description = "Plugin skot pour le module modelcontract"
                implementationClass = "tech.skot.tools.gradle.PluginModelContract"
            }

            create("SkotViewContract") {
                id = "tech.skot.viewcontract"
                displayName = "SKot viewcontract"
                description = "Plugin skot pour le module viewcontract"
                implementationClass = "tech.skot.tools.gradle.PluginViewContract"
            }

            create("SkotViewModel") {
                id = "tech.skot.viewmodel"
                displayName = "SKot viewmodel"
                description = "Plugin skot pour le module viewmodel"
                implementationClass = "tech.skot.tools.gradle.PluginViewModel"
            }

            create("SkotModel") {
                id = "tech.skot.model"
                displayName = "SKot model"
                description = "Plugin skot pour le module model"
                implementationClass = "tech.skot.tools.gradle.PluginModel"
            }

            create("SkotViewLegacy") {
                id = "tech.skot.viewlegacy"
                displayName = "SKot viewlegacy"
                description = "Plugin skot pour le module viewlegacy"
                implementationClass = "tech.skot.tools.gradle.PluginViewLegacy"
            }

            create("SkotApp") {
                id = "tech.skot.app"
                displayName = "SKot androidApp"
                description = "Plugin skot pour le module androidApp"
                implementationClass = "tech.skot.tools.gradle.PluginApp"
            }

            create("SkotFeature") {
                id = "tech.skot.feature"
                displayName = "SKot feature"
                description = "Plugin skot pour un module feature"
                implementationClass = "tech.skot.tools.gradle.PluginFeature"
            }

            create("SkotTools") {
                id = "tech.skot.tools"
                displayName = "SKot tools"
                description = "Plugin skot pour un module skot"
                implementationClass = "tech.skot.tools.gradle.PluginTools"
            }

            create("Skot") {
                id = "tech.skot.general"
                displayName = "SKot general"
                description = "Plugin skot pour un projet skot"
                implementationClass = "tech.skot.tools.gradle.PluginGeneral"
            }

            create("SkotLibraryContract") {
                id = "tech.skot.library-contract"
                displayName = "SKot library-contract"
                description = "Plugin skot pour un module contract de library"
                implementationClass = "tech.skot.tools.gradle.PluginLibraryContract"
            }

            create("SkotLibrary") {
                id = "tech.skot.library"
                displayName = "SKot library"
                description = "Plugin skot pour un module de library"
                implementationClass = "tech.skot.tools.gradle.PluginLibrary"
            }

            create("SkotLibraryViewlegacy") {
                id = "tech.skot.library-viewlegacy"
                displayName = "SKot library-viewlegacy"
                description = "Plugin skot pour un module viewlegacy de library"
                implementationClass = "tech.skot.tools.gradle.PluginLibraryViewLegacy"
            }
        }
    }
}

fun buildVersionsFile() {
    fun com.squareup.kotlinpoet.TypeSpec.Builder.addStringConst(
        name: String,
        value: String,
    ): com.squareup.kotlinpoet.TypeSpec.Builder {
        return addProperty(
            com.squareup.kotlinpoet.PropertySpec.builder(
                name,
                String::class,
                com.squareup.kotlinpoet.KModifier.CONST,
            )
                .initializer("\"$value\"")
                .build(),
        )
    }

    fun com.squareup.kotlinpoet.TypeSpec.Builder.addIntConst(
        name: String,
        value: Int,
    ): com.squareup.kotlinpoet.TypeSpec.Builder {
        return addProperty(
            com.squareup.kotlinpoet.PropertySpec.builder(
                name,
                Int::class,
                com.squareup.kotlinpoet.KModifier.CONST,
            )
                .initializer(value.toString())
                .build(),
        )
    }

    val file = com.squareup.kotlinpoet.FileSpec.builder("tech.skot", "Versions")
    val classBuilderCommon =
        com.squareup.kotlinpoet.TypeSpec.objectBuilder("Versions")
            .addKdoc("This code is generated from build.gradle.kts")
            .addStringConst("skot", Versions.version)
            .addStringConst("group", Versions.group)
            .addStringConst("serialization", libs.versions.kotlinx.serialization.get())
            .addStringConst("kotlinxDateTime", libs.versions.kotlinx.datetime.get())
            .addStringConst("ktor", libs.versions.ktor.get())
            .addStringConst("kotlin", libs.versions.kotlin.get())
            .addStringConst("kotlinCoroutines", libs.versions.kotlinx.coroutines.asProvider().get())
            .addStringConst("kotlinpoet", libs.versions.kotlinpoet.get())
            .addIntConst("android_minSdk", libs.versions.android.minSdk.get().toInt())
            .addIntConst("android_compileSdk", libs.versions.android.compileSdk.get().toInt())
            .addIntConst("android_targetSdk", libs.versions.android.targetSdk.get().toInt())
            .addStringConst("android_app_compat", libs.versions.appcompat.get())
            .addStringConst("sqldelight", libs.versions.sqldelight.get())
            .addStringConst("desugaring", libs.versions.desugar.get())
            .addStringConst("leakcanary", libs.versions.leakcanary.get())
            .addStringConst("ktlint", libs.versions.ktlint.get())


    file.addType(classBuilderCommon.build())
    file.build().writeTo(rootProject.projectDir.resolve("plugin/src/main/kotlin"))
}

buildVersionsFile()
