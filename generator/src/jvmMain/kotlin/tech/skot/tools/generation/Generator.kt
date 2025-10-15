package tech.skot.tools.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import org.w3c.dom.Document
import org.w3c.dom.Element
import tech.skot.core.components.SKComponentVC
import tech.skot.core.components.SKScreenVC
import tech.skot.tools.generation.model.generateModel
import tech.skot.tools.generation.resources.generateColors
import tech.skot.tools.generation.resources.generateDimens
import tech.skot.tools.generation.resources.generateFonts
import tech.skot.tools.generation.resources.generateIcons
import tech.skot.tools.generation.resources.generatePermissions
import tech.skot.tools.generation.resources.generatePlurals
import tech.skot.tools.generation.resources.generateStrings
import tech.skot.tools.generation.resources.generateStyles
import tech.skot.tools.generation.resources.generateTransitions
import tech.skot.tools.generation.viewlegacy.generateViewLegacy
import tech.skot.tools.generation.viewmodel.InitializationPlan
import tech.skot.tools.generation.viewmodel.generateModelInjectorMock
import tech.skot.tools.generation.viewmodel.generateModelMock
import tech.skot.tools.generation.viewmodel.generateModuleMock
import tech.skot.tools.generation.viewmodel.generateStatesMocks
import tech.skot.tools.generation.viewmodel.generateViewInjectorMock
import tech.skot.tools.generation.viewmodel.generateViewMock
import tech.skot.tools.generation.viewmodel.generateViewModel
import tech.skot.tools.generation.viewmodel.generateViewModelTests
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

class Generator(
    val appPackage: String,
    val startClass: KClass<SKScreenVC>,
    val rootStateClass: KClass<*>?,
    val baseActivity: ClassName,
    val rootPath: Path,
    val feature: String?,
    val baseActivityVar: String?,
    val initializationPlans: List<InitializationPlan>,
    val referenceIconsByVariant: Boolean,
) {
    class ModulesNames(
        val app: String = "androidApp",
        val viewcontract: String = "viewcontract",
        val modelcontract: String = "modelcontract",
        val view: String = "view",
        val viewmodel: String = "viewmodel",
        val model: String = "model",
    )

    val modules = ModulesNames(view = feature ?: "view", app = feature ?: "androidApp")

    val variantsCombinaison = skVariantsCombinaison(rootPath)
    val mainVariant = skReadVariants(rootPath).variants.firstOrNull()?.capitalizeAsciiOnly()

    @ExperimentalStdlibApi
    val rootState = rootStateClass?.let { StateDef("rootState", appPackage, it) }

    @ExperimentalStdlibApi
    val rootStatePropertyName =
        rootState?.let { rootState ->
            feature?.let { "$feature${rootState.nameAsProperty.capitalizeAsciiOnly()}" }
                ?: rootState.nameAsProperty
        }

    @ExperimentalStdlibApi
    fun StateDef.addToMap(map: MutableMap<String, StateDef>) {
        map[kclass.qualifiedName!!] = this
        subStates.forEach {
            it.addToMap(map)
        }
        compositeSubStates.forEach {
            it.addToMap(map)
        }
    }

    @ExperimentalStdlibApi
    val mapStateDefQualifiedNameStateDef =
        mutableMapOf<String, StateDef>().apply {
            rootState?.addToMap(this)
        }

    @ExperimentalStdlibApi
    fun KCallable<*>.stateDef() =
        mapStateDefQualifiedNameStateDef[returnType.jvmErasure.qualifiedName]

    val components =
        mutableSetOf<KClass<out SKComponentVC>>().apply {
            addLinkedComponents(startClass, appPackage)
        }.map { it.def() }

    val componentsWithModel =
        components.filter {
            it.modelClass != null
        }

    @ExperimentalStdlibApi
    fun StateDef.addBmsTo(map: MutableMap<ClassName, StateDef>) {
        bmS.forEach {
            map[it] = this
        }
        (subStates + compositeSubStates).forEach {
            it.addBmsTo(map)
        }
    }

    @ExperimentalStdlibApi
    val bmsMap = mutableMapOf<ClassName, StateDef>()

    val viewInjectorInterface = ClassName("$appPackage.di", "ViewInjector")
    val viewInjectorImpl = ClassName("$appPackage.di", "ViewInjectorImpl")
    val viewInjectorIntance = ClassName("$appPackage.di", "viewInjector")
    val viewInjectorMock = ClassName("$appPackage.di", "ViewInjectorMock")

    val modelInjectorInterface = ClassName("$appPackage.di", "ModelInjector")
    val modelInjectorImpl = ClassName("$appPackage.di", "ModelInjectorImpl")
    val modelInjectorMock = ClassName("$appPackage.di", "ModelInjectorMock")
    val modelInjectorIntance = ClassName("$appPackage.di", "modelInjector")

    val transitionsInterface = ClassName("$appPackage.view", "Transitions")
    val transitionsImpl = ClassName("$appPackage.view", "TransitionsImpl")
    val transitionsMock = ClassName("$appPackage.view", "TransitionsMock")

    val permissionsInterface = ClassName("$appPackage.view", "Permissions")
    val permissionsImpl = ClassName("$appPackage.view", "PermissionsImpl")
    val permissionsMock = ClassName("$appPackage.view", "PermissionsMock")

    val stringsInstance = ClassName(appPackage, "strings")
    val stringsInterface = ClassName(appPackage, "Strings")
    val stringsImpl = ClassName(appPackage, "StringsImpl")
    val stringsMock = ClassName(appPackage, "StringsMock")

    val pluralsInstance = ClassName(appPackage, "plurals")
    val pluralsInterface = ClassName(appPackage, "Plurals")
    val pluralsImpl = ClassName(appPackage, "PluralsImpl")
    val pluralsMock = ClassName(appPackage, "PluralsMock")

    val iconsInstance = ClassName(appPackage, "icons")
    val iconsInterface = ClassName(appPackage, "Icons")
    val iconsImpl = ClassName(appPackage, "IconsImpl")
    val iconsMock = ClassName(appPackage, "IconsMock")

    val colorsInstance = ClassName(appPackage, "colors")
    val colorsInterface = ClassName(appPackage, "Colors")
    val colorsImpl = ClassName(appPackage, "ColorsImpl")
    val colorsMock = ClassName(appPackage, "ColorsMock")

    val fontsInstance = ClassName(appPackage, "fonts")
    val fontsInterface = ClassName(appPackage, "Fonts")
    val fontsImpl = ClassName(appPackage, "FontsImpl")
    val fontsMock = ClassName(appPackage, "FontsMock")

    val stylesInstance = ClassName(appPackage, "styles")
    val stylesInterface = ClassName(appPackage, "Styles")
    val stylesImpl = ClassName(appPackage, "StylesImpl")
    val stylesMock = ClassName(appPackage, "StylesMock")

    val dimensInstance = ClassName(appPackage, "dimens")
    val dimensInterface = ClassName(appPackage, "Dimens")
    val dimensImpl = ClassName(appPackage, "DimensImpl")
    val dimensMock = ClassName(appPackage, "DimensMock")

    val initializeView = ClassName("$appPackage.di", "initializeView")

    val skBuild = ClassName(appPackage, "SKBuild")
    val generatedAppModules = ClassName("$appPackage.di", "generatedAppModules")
    val appFeatureInitializer =
        ClassName(
            appPackage,
            "${
                appPackage.substringAfterLast(".")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }Initializer",
        )
    val viewModelModuleMock = ClassName("$appPackage.di", "moduleMock")

    val statePersistenceManager = ClassName("$appPackage.states", "statePersistenceManager")
    val restoreStateFunction = ClassName(statePersistenceManager.packageName, "restoreState")
    val saveStateFunction = ClassName(statePersistenceManager.packageName, "saveState")

    val shortCuts = ClassName("$appPackage.di", "shortCuts")

    val moduleFun = ClassName(SKOT_DI_PACKAGE_NAME, "module")
    val module = ClassName(SKOT_DI_PACKAGE_NAME, "Module")
    val getFun = ClassName(SKOT_DI_PACKAGE_NAME, "get")
    val baseInjector = ClassName(SKOT_DI_PACKAGE_NAME, "BaseInjector")
    val mockInjector = ClassName(SKOT_DI_PACKAGE_NAME, "InjectorMock")

    companion object {
        const val SKOT_DI_PACKAGE_NAME = "tech.skot.core.di"
        const val VISIBILITY_LISTENER_VAR_NAME = "visibilityListener"
    }

    @ExperimentalStdlibApi
    fun generate() {
        deleteModuleGenerated(modules.app)
        deleteModuleGenerated(modules.viewcontract)
        deleteModuleGenerated(modules.modelcontract)
        deleteModuleGenerated(modules.model)
        deleteModuleGenerated(modules.view)
        deleteModuleGenerated(modules.viewmodel)

        rootState?.let { generateStates(it) }
        rootState?.let { generateStatesMocks(it) }

        println("can see: ")

        generateViewContract()
        generateViewLegacy()
        generateViewModel()
        generateViewMock()
        generateViewInjectorMock()
        generateViewModelTests()
        generateModuleMock()
        generateModelMock()
        generateModelInjectorMock()

        generateTransitions()
        generatePermissions()

        generateModelContract()
        generateModel()
        generateStrings()
        generatePlurals()
        generateIcons()
        generateColors()
        generateStyles()
        generateFonts()
        generateDimens()
        generateApp()
        generateCodeMap()
    }

    fun generatedCommonSources(
        module: String,
        combinaison: String? = null,
    ): Path = rootPath.resolve("$module/generated${combinaison ?: ""}/commonMain/kotlin")

    fun commonSources(module: String): Path = rootPath.resolve("$module/src/commonMain/kotlin")

    fun jvmTestSources(
        module: String,
        combinaison: String? = null,
    ): Path = rootPath.resolve("$module/src${combinaison ?: ""}/jvmTest/kotlin")

    fun generatedJvmTestSources(
        module: String,
        combinaison: String? = null,
    ): Path = rootPath.resolve("$module/generated${combinaison ?: ""}/jvmTest/kotlin")

    fun generatedAndroidTestSources(
        module: String,
        combinaison: String? = null,
    ): Path = rootPath.resolve("$module/generated${combinaison ?: ""}/androidTest/kotlin")

    fun generatedAndroidSources(
        module: String,
        combinaison: String? = null,
    ): Path = rootPath.resolve("$module/generated${combinaison ?: ""}/androidMain/kotlin")

    fun androidSources(module: String): Path = rootPath.resolve("$module/src/androidMain/kotlin")

    fun androidTestSources(module: String): Path =
        rootPath.resolve("$module/src/androidTest/kotlin")

    private fun deleteModuleGenerated(module: String) {
        rootPath.resolve("$module/generated").toFile().deleteRecursively()
    }

    private fun generateViewContract() {
        generateViewInjector()
        if (feature == null) {
            appFeatureInitializer.fileClassBuilder {
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(
                            ParameterSpec.builder(
                                "initialize",
                                LambdaTypeName.get(returnType = Unit::class.asTypeName())
                                    .copy(suspending = true),
                            )
                                .build(),
                        )
                        .addParameter(
                            ParameterSpec.builder(
                                "onDeepLink",
                                LambdaTypeName.get(
                                    null,
                                    parameters =
                                    listOf(
                                        ParameterSpec.builder(
                                            name = "uri",
                                            type = FrameworkClassNames.skUri,
                                        ).build(),
                                        ParameterSpec.builder(
                                            name = "fromWebView",
                                            type = Boolean::class.asTypeName(),
                                        ).build(),
                                    ),
                                    returnType = Boolean::class.asTypeName(),
                                ),
                            )
                                .build(),
                        )
                        .addParameter(
                            ParameterSpec.builder(
                                "start",
                                LambdaTypeName.get(
                                    returnType = Unit::class.asTypeName(),
                                    parameters =
                                    listOf(
                                        ParameterSpec(
                                            name = "action",
                                            type = String::class.asTypeName().nullable()
                                        ),
                                    ),
                                )
                                    .copy(suspending = true),
                            )
                                .build(),
                        )
                        .addParameter(
                            ParameterSpec.builder(
                                "resetToRoot",
                                LambdaTypeName.get(returnType = Unit::class.asTypeName()),
                            )
                                .build(),
                        )
                        .build(),
                )
                superclass(ClassName("tech.skot.core", "SKFeatureInitializer"))
                superclassConstructorParameters.add(CodeBlock.of("initialize, onDeepLink, start, resetToRoot"))
            }.writeTo(generatedCommonSources(modules.viewcontract))
        }
    }

    private fun generateViewInjector() {
        FileSpec.builder(
            viewInjectorInterface.packageName,
            viewInjectorInterface.simpleName,
        ).addType(
            TypeSpec.interfaceBuilder(viewInjectorInterface.simpleName)
                .addFunctions(
                    components.map {
                        FunSpec.builder(it.name.decapitalizeAsciiOnly())
                            .addModifiers(KModifier.ABSTRACT)
                            .apply {
                                if (it.isScreen) {
                                    addParameter(
                                        name = VISIBILITY_LISTENER_VAR_NAME,
                                        type = FrameworkClassNames.skVisiblityListener,
                                    )
                                }
                            }
                            .addParameters(
                                it.subComponents.map { it.asParam() },
                            )
                            .addParameters(
                                it.fixProperties.map { it.asParam() },
                            )
                            .addParameters(
                                it.mutableProperties.map {
                                    it.initial().asParam(withDefaultNullIfNullable = true)
                                },
                            )
                            .returns(it.vc)
                            .build()
                    },
                )
                .build(),
        )
            .build().writeTo(generatedCommonSources(modules.viewcontract))
    }

    @ExperimentalStdlibApi
    private fun generateModelContract() {
        generateModelInjector()
    }

    @ExperimentalStdlibApi
    private fun generateModelInjector() {
        modelInjectorInterface.fileInterfaceBuilder {
            addFunctions(
                componentsWithModel.map {
                    FunSpec.builder(it.name.replaceFirstChar { it.lowercase(Locale.getDefault()) })
                        .addParameter(
                            ParameterSpec.builder(
                                "coroutineContext",
                                FrameworkClassNames.coroutineContext,
                            )
                                .build(),
                        )
                        .addParameters(
                            it.states.map {
                                ParameterSpec.builder(it.name, it.stateDef()!!.contractClassName)
                                    .build()
                            },
                        )
                        .addModifiers(KModifier.ABSTRACT)
                        .returns(it.modelContract())
                        .build()
                },
            )
        }.writeTo(generatedCommonSources(modules.modelcontract))
    }

    fun ClassName.existsAndroidInModule(module: String) =
        Files.exists(
            androidSources(module).resolve(packageName.packageToPathFragment())
                .resolve("$simpleName.kt"),
        )

    fun ClassName.existsAndroidTestInModule(module: String) =
        Files.exists(
            androidTestSources(module).resolve(packageName.packageToPathFragment())
                .resolve("$simpleName.kt"),
        )

    fun ClassName.existsCommonInModule(module: String) =
        Files.exists(
            commonSources(module).resolve(packageName.packageToPathFragment())
                .resolve("$simpleName.kt"),
        )

    fun ClassName.existsJvmTestInModule(module: String) =
        Files.exists(
            jvmTestSources(module).resolve(packageName.packageToPathFragment())
                .resolve("$simpleName.kt"),
        )

    fun androidResLayoutPath(
        module: String,
        name: String,
    ): Path = rootPath.resolve("$module/src/androidMain/res/layout/$name.xml")

    val viewR = ClassName("$appPackage.view", "R")
    val appR = ClassName("$appPackage.android", "R")

    @ExperimentalStdlibApi
    fun generateApp() {
        if (feature == null) {
            generateAppModule()
        }
    }

    @ExperimentalStdlibApi
    fun generateAppModule() {
        FileSpec.builder(generatedAppModules.packageName, generatedAppModules.simpleName)
            .addProperty(
                PropertySpec.builder(
                    generatedAppModules.simpleName,
                    ClassName("kotlin.collections", "List").parameterizedBy(
                        module.parameterizedBy(baseInjector),
                    ),
                )
                    .initializer(
                        CodeBlock.builder()
                            .beginControlFlow("listOf(module")
                            .addStatement("single<${stringsInterface.simpleName}> { ${stringsImpl.simpleName}(androidApplication)}")
                            .addStatement("single<${pluralsInterface.simpleName}> { ${pluralsImpl.simpleName}(androidApplication)}")
                            .addStatement("single<${iconsInterface.simpleName}> { ${iconsImpl.simpleName}(androidApplication)}")
                            .addStatement("single<${colorsInterface.simpleName}> { ${colorsImpl.simpleName}(androidApplication)}")
                            .addStatement("single<${fontsInterface.simpleName}> { ${fontsImpl.simpleName}()}")
                            .addStatement("single<${stylesInterface.simpleName}> { ${stylesImpl.simpleName}()}")
                            .addStatement("single<${dimensInterface.simpleName}> { ${dimensImpl.simpleName}()}")
                            .addStatement("single<${viewInjectorInterface.simpleName}> { ${viewInjectorImpl.simpleName}()}")
                            .addStatement("single<${modelInjectorInterface.simpleName}> { ${modelInjectorImpl.simpleName}()}")
                            .addStatement("single<${transitionsInterface.simpleName}> { ${transitionsImpl.simpleName}()}")
                            .addStatement("single<${permissionsInterface.simpleName}> { ${permissionsImpl.simpleName}()}")
                            .beginControlFlow("single")
                            .addStatement("${appFeatureInitializer.simpleName}(")
                            .beginControlFlow("initialize = ")
                            .apply {
                                rootState?.let {
                                    beginControlFlow("restoreState().let")
                                    addStatement("$appPackage.states.${it.nameAsProperty} = it")
                                    addStatement("${shortCuts.packageName}.${it.nameAsProperty} = it")
                                    endControlFlow()
                                }
                            }
                            .addStatement("initializeView(androidApplication)")
                            .endControlFlow()
                            .addStatement(",")
                            .beginControlFlow("onDeepLink = { uri, fromWebView ->")
                            .addStatement("onDeeplink(uri, fromWebView)")
                            .endControlFlow()
                            .addStatement(",")
                            .beginControlFlow("start = { action ->")
                            .addStatement("start(startModel(action), action)")
                            .endControlFlow()
                            .addStatement(",")
                            .beginControlFlow("resetToRoot = ")
                            .addStatement("SKRootStack.resetToRoot()")
                            .endControlFlow()
                            .addStatement(")")
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement(",")
                            .addStatement("modelFrameworkModule,")
                            .addStatement("coreViewModule,")
                            .apply {
                                getUsedSKLibrariesModules()
                                    .forEach {
                                        addStatement("$it,")
                                    }
                            }
                            .addStatement(")")
                            .build(),
                    )
                    .build(),
            )
//                .addImportClassName(getFun)
            .addImportClassName(FrameworkClassNames.skRootStack)
            .addImportClassName(moduleFun)
            .addImportClassName(baseInjector)
            .addImportClassName(stringsImpl)
            .addImportClassName(stringsInterface)
            .addImportClassName(pluralsImpl)
            .addImportClassName(pluralsInterface)
            .addImportClassName(iconsImpl)
            .addImportClassName(iconsInterface)
            .addImportClassName(colorsInterface)
            .addImportClassName(colorsImpl)
            .addImportClassName(fontsInterface)
            .addImportClassName(fontsImpl)
            .addImportClassName(stylesInterface)
            .addImportClassName(stylesImpl)
            .addImportClassName(dimensInterface)
            .addImportClassName(dimensImpl)
            .addImportClassName(appFeatureInitializer)
            .addImportClassName(transitionsInterface)
            .addImportClassName(transitionsImpl)
            .addImportClassName(permissionsInterface)
            .addImportClassName(permissionsImpl)
            .addImport("tech.skot.di", "modelFrameworkModule")
            .addImport("tech.skot.core.di", "coreViewModule")
            .addImport(appPackage, "start")
            .addImport(appPackage, "onDeeplink")
            .apply {
                rootState?.let {
                    addImport("$appPackage.states", "restoreState")
                }
            }
            .build()
            .writeTo(generatedAndroidSources(modules.app))
    }

    fun getUsedSKLibrariesModules(): List<String> {
        return Files.readAllLines(rootPath.resolve("skot_librairies.properties"))
            .filterNot { it.startsWith("//") }
            .mapNotNull {
                val split = it.split(",")
                if (split.size > 1) {
                    split[1].let { it.ifBlank { null } }
                } else {
                    "$it.di.${it.substringAfterLast(".")}Module"
                }
            }
    }

    fun ComponentDef.hasModel() = componentsWithModel.contains(this)

    // Regarde si le fichier existe déjà, dans main ou dans une variante de main
    fun existsPath(
        path: Path,
        patternConbinable: String,
    ): Boolean {
        return Files.exists(path) ||
                variantsCombinaison.any {
                    Files.exists(path.replaceSegment(patternConbinable, "$patternConbinable$it"))
                }
    }

    fun migrate() {
        migrateTo29()
    }
}

fun Path.getDocument(): Document =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.toFile())

fun Path.getDocumentElement(): Element = getDocument().documentElement

fun Element.childElements(): List<Element> {
    val elements: MutableList<Element> = mutableListOf()
    for (i in 0 until childNodes.length) {
        (childNodes.item(i) as? Element)?.let { elements.add(it) }
    }
    return elements
}

fun Document.getElementsWithTagName(tagName: String): List<Element> {
    val list = getElementsByTagName(tagName)
    val res = mutableListOf<Element>()
    (0..<list.length).forEach {
        (list.item(it) as? Element)?.let { res.add(it) }
    }
    return res
}
