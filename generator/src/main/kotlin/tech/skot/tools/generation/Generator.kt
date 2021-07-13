package tech.skot.tools.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.w3c.dom.Document
import org.w3c.dom.Element
import tech.skot.core.components.SKComponentVC
import tech.skot.core.components.SKScreenVC
import tech.skot.tools.generation.model.generateModel
import tech.skot.tools.generation.resources.generateTransitions
import tech.skot.tools.generation.viewlegacy.*
import tech.skot.tools.generation.viewmodel.generateViewModel
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

//val project by lazy {
//    KotlinCoreEnvironment.createForProduction(
//        Disposer.newDisposable(),
//        CompilerConfiguration(),
//        EnvironmentConfigFiles.JVM_CONFIG_FILES
//    ).project
//}

object Modules {
    const val app = "androidApp"
    const val viewcontract = "viewcontract"
    const val modelcontract = "modelcontract"
    const val view = "view"
    const val viewmodel = "viewmodel"
    const val model = "model"
}


class Generator(
    val appPackage: String,
    val startClass: KClass<SKScreenVC>,
    val rootStateClass: KClass<*>?,
    val baseActivity: ClassName,
    val rootPath: Path
) {


    val variantsCombinaison = skVariantsCombinaison(rootPath)

    @ExperimentalStdlibApi
    val rootState = rootStateClass?.let { StateDef("rootState", appPackage, it) }

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
    val mapStateDefQualifiedNameStateDef = mutableMapOf<String, StateDef>().apply {
        rootState?.addToMap(this)
    }

    @ExperimentalStdlibApi
    fun KCallable<*>.stateDef() =
        mapStateDefQualifiedNameStateDef[returnType.jvmErasure.qualifiedName]


    val components = mutableSetOf<KClass<out SKComponentVC>>().apply {
        addLinkedComponents(startClass, appPackage)
    }.map { it.def() }

    val componentsWithModel = components.filter {
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

    val modelInjectorInterface = ClassName("$appPackage.di", "ModelInjector")
    val modelInjectorImpl = ClassName("$appPackage.di", "ModelInjectorImpl")
    val modelInjectorIntance = ClassName("$appPackage.di", "modelInjector")

    val transisitonsInterface = ClassName("$appPackage.view", "Transitions")
    val transisitonsImpl = ClassName("$appPackage.view", "TransitionsImpl")

    val stringsInstance = ClassName(appPackage, "strings")
    val stringsInterface = ClassName(appPackage, "Strings")
    val stringsImpl = ClassName(appPackage, "StringsImpl")

    val pluralsInstance = ClassName(appPackage, "plurals")
    val pluralsInterface = ClassName(appPackage, "Plurals")
    val pluralsImpl = ClassName(appPackage, "PluralsImpl")

    val iconsInstance = ClassName(appPackage, "icons")
    val iconsInterface = ClassName(appPackage, "Icons")
    val iconsImpl = ClassName(appPackage, "IconsImpl")

    val colorsInstance = ClassName(appPackage, "colors")
    val colorsInterface = ClassName(appPackage, "Colors")
    val colorsImpl = ClassName(appPackage, "ColorsImpl")

    val skBuild = ClassName(appPackage, "SKBuild")
    val generatedAppModules = ClassName("$appPackage.di", "generatedAppModules")
    val appFeatureInitializer =
        ClassName(appPackage, "${appPackage.substringAfterLast(".").capitalize()}Initializer")

    val statePersistenceManager = ClassName("$appPackage.states", "statePersistenceManager")
    val restoreStateFunction = ClassName(statePersistenceManager.packageName, "restoreState")
    val saveStateFunction = ClassName(statePersistenceManager.packageName, "saveState")

    val shortCuts = ClassName("$appPackage.di", "shortCuts")

    val moduleFun = ClassName("tech.skot.core.di", "module")
    val module = ClassName("tech.skot.core.di", "Module")
    val getFun = ClassName("tech.skot.core.di", "get")
    val baseInjector = ClassName("tech.skot.core.di", "BaseInjector")

    @ExperimentalStdlibApi
    fun generate() {
        deleteModuleGenerated(Modules.viewcontract)
        deleteModuleGenerated(Modules.view)
        deleteModuleGenerated(Modules.model)
        deleteModuleGenerated(Modules.modelcontract)
        deleteModuleGenerated(Modules.viewmodel)

        rootState?.let { generateStates(it) }


        generateViewContract()
        generateViewLegacy()
        generateViewModel()
        generateTransitions()

        generateModelContract()
        generateModel()
        deleteModuleGenerated(Modules.app)
        generateStrings()
        generatePlurals()
        generateIcons()
        generateColors()
        generateApp()
        generateCodeMap()

//        components.forEach {
//            val file = it.viewModel().run {
//                commonSources(Modules.viewmodel).resolve(it.packageName.packageToPathFragment())
//                    .resolve("$simpleName.kt")
//            }
//            val text = String(Files.readAllBytes(file))
//            println("---- ${it.name}")
////            println(text)
//
//            try {
//                val vFile = LightVirtualFile(
//                    it.name,KotlinFileType.INSTANCE, text
//                )
//
////                println(project)
//
//                val viewProvider = PsiManager.getInstance(project).findViewProvider(vFile)
//                val ktFile = viewProvider?.getPsi(viewProvider.baseLanguage)  as KtFile
////
//////                val viewProvider: FileViewProvider = this.findViewProvider(vFile)
//////                return viewProvider.getPsi(viewProvider.baseLanguage)
////
//////                val ktFile = PsiManager.getInstance(project)
//////                    .findFile() as KtFile
////
//                ktFile.children.forEach {
//                    when (it) {
//                        is KtClass -> {
//                            println("analyse name: ${it.name}")
//                            it.children.forEach {
//                                when (it) {
//                                    is  KtFunction-> {
//
//                                    }
//                                }
//                            }
//                            println(it.text)
//                        }
//                    }
//                }
//            }
//            catch (ex:Exception) {
//                println("&&&&&&&&&&&& Exception     $ex")
//            }
//
//        }

    }

    fun generatedCommonSources(module: String) =
        rootPath.resolve("$module/generated/commonMain/kotlin")

    fun commonSources(module: String) =
        rootPath.resolve("$module/src/commonMain/kotlin")

    fun generatedAndroidSources(module: String) =
        rootPath.resolve("$module/generated/androidMain/kotlin")

    fun androidSources(module: String) =
        rootPath.resolve("$module/src/androidMain/kotlin")

    fun deleteModuleGenerated(module: String) {
        rootPath.resolve("$module/generated").toFile().deleteRecursively()
    }

    private fun generateViewContract() {
        generateViewInjector()
        appFeatureInitializer.fileClassBuilder {
            primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(
                            "initialize",
                            LambdaTypeName.get(returnType = Unit::class.asTypeName())
                                .copy(suspending = true)
                        )
                            .build()
                    )
                    .build()
            )
            superclass(ClassName("tech.skot.core", "SKFeatureInitializer"))
            superclassConstructorParameters.add(CodeBlock.of("initialize"))
        }.writeTo(generatedCommonSources(Modules.viewcontract))
    }

    private fun generateViewInjector() {
        FileSpec.builder(
            viewInjectorInterface.packageName,
            viewInjectorInterface.simpleName

        ).addType(TypeSpec.interfaceBuilder(viewInjectorInterface.simpleName)
            .addFunctions(
                components.map {
                    FunSpec.builder(it.name.decapitalize())
                        .addModifiers(KModifier.ABSTRACT)
                        .addParameters(
                            it.subComponents.map { it.asParam() }
                        )
                        .addParameters(
                            it.fixProperties.map { it.asParam() }
                        )
                        .addParameters(
                            it.mutableProperties.map {
                                it.initial().asParam(withDefaultNullIfNullable = true)
                            }
                        )
                        .returns(it.vc)
                        .build()
                }
            )
            .build())
            .build().writeTo(generatedCommonSources(Modules.viewcontract))
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
                    FunSpec.builder(it.name.decapitalize())
                        .addParameter(
                            ParameterSpec.builder(
                                "coroutineContext",
                                FrameworkClassNames.coroutineContext
                            )
                                .build()
                        )
                        .addParameters(
                            it.states.map {
                                ParameterSpec.builder(it.name, it.stateDef()!!.contractClassName)
                                    .build()
                            }
                        )
                        .addModifiers(KModifier.ABSTRACT)
                        .returns(it.modelContract())
                        .build()
                }
            )
        }.writeTo(generatedCommonSources(Modules.modelcontract))
    }

    fun ClassName.existsAndroidInModule(module: String) =
        Files.exists(
            androidSources(module).resolve(packageName.packageToPathFragment())
                .resolve("$simpleName.kt")
        )

    fun ClassName.existsCommonInModule(module: String) =
        Files.exists(
            commonSources(module).resolve(packageName.packageToPathFragment())
                .resolve("$simpleName.kt")
        )

    fun androidResLayoutPath(module: String, name: String) =
        rootPath.resolve("$module/src/androidMain/res/layout/$name.xml")

    val viewR = ClassName("$appPackage.${Modules.view}", "R")
    val appR = ClassName("$appPackage.android", "R")


    @ExperimentalStdlibApi
    fun generateApp() {
        generateAppModule()
        generateStartsIfNeeded()
    }


    @ExperimentalStdlibApi
    fun generateAppModule() {
        val librariesGroups = getUsedSKLibrariesGroups()

        FileSpec.builder(generatedAppModules.packageName, generatedAppModules.simpleName)
            .addProperty(
                PropertySpec.builder(
                    generatedAppModules.simpleName,
                    ClassName("kotlin.collections", "List").parameterizedBy(
                        module.parameterizedBy(baseInjector)
                    )
                )
                    .initializer(CodeBlock.builder()
                        .beginControlFlow("listOf(module")
                        .addStatement("single { ${stringsImpl.simpleName}(androidApplication) as ${stringsInterface.simpleName}}")
                        .addStatement("single { ${pluralsImpl.simpleName}(androidApplication) as ${pluralsInterface.simpleName}}")
                        .addStatement("single { ${iconsImpl.simpleName}() as ${iconsInterface.simpleName}}")
                        .addStatement("single { ${colorsImpl.simpleName}() as ${colorsInterface.simpleName}}")
                        .addStatement("single { ${viewInjectorImpl.simpleName}() as ${viewInjectorInterface.simpleName}}")
                        .addStatement("single { ${modelInjectorImpl.simpleName}() as ${modelInjectorInterface.simpleName}}")
                        .addStatement("single { ${transisitonsImpl.simpleName}() as ${transisitonsInterface.simpleName}}")
                        .beginControlFlow("single")
                        .beginControlFlow("${appFeatureInitializer.simpleName}")
                        .apply {
                            rootState?.let {
                                beginControlFlow("restoreState().let")
                                addStatement("$appPackage.states.${it.nameAsProperty} = it")
                                addStatement("${shortCuts.packageName}.${it.nameAsProperty} = it")
                                endControlFlow()
                            }
                        }
                        //.addStatement("${rootState?} restoreState()")
                        .addStatement("startView()")
                        .addStatement("start(startModel())")
                        .endControlFlow()
                        .endControlFlow()

                        .endControlFlow()
                        .addStatement(",")
                        .addStatement("modelFrameworkModule,")
                        .addStatement("coreViewModule,")
                        .apply {
                            librariesGroups
                                .map {
                                    "${it.substringAfterLast(".")}Module"
                                }.forEach {
                                    addStatement(it)
                                }

                        }
                        .addStatement(")")
                        .build())
                    .build()

            )
//                .addImportClassName(getFun)
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
            .addImportClassName(appFeatureInitializer)
            .addImportClassName(transisitonsInterface)
            .addImportClassName(transisitonsImpl)
            .addImport("tech.skot.di", "modelFrameworkModule")
            .addImport("tech.skot.core.di", "coreViewModule")
            .addImport(appPackage, "start")
            .apply {
                rootState?.let {
                    addImport("$appPackage.states", "restoreState")
                }
            }

            .apply {
                getUsedSKLibrariesGroups().map {
                    addImport("$it.di", "${it.substringAfterLast(".")}Module")
                }
            }
            .build()
            .writeTo(generatedAndroidSources(Modules.app))
    }

    fun generateStartsIfNeeded() {
        val startView = ClassName("$appPackage.di", "startView")
        if (!startView.existsAndroidInModule(Modules.view)) {
            FileSpec.builder(startView.packageName, startView.simpleName)
                .addImportClassName(FrameworkClassNames.skComponentView)
                .addImportClassName(ClassName("android.view", "Gravity"))
                .addImportClassName(AndroidClassNames.frameLayout)
                .addImportClassName(AndroidClassNames.snackBar)
                .addImportClassName(AndroidClassNames.build)
                .addFunction(
                    FunSpec.builder(startView.simpleName)
                        .addModifiers(KModifier.SUSPEND)
                        .addCode(
                            CodeBlock.of(
                                """SKComponentView.displayError = { message ->
        Snackbar.make(activity.window.decorView, message, Snackbar.LENGTH_LONG)
            .apply {
                view.apply {
                    (layoutParams as? FrameLayout.LayoutParams)?.let {
                        it.gravity = Gravity.TOP
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            it.topMargin = activity.window?.decorView?.rootWindowInsets?.systemWindowInsetTop
                                ?: 0
                        }

                        layoutParams = it
                    }
                }
                show()
            }
    }"""
                            )
                        )
                        .build()
                )
                .build()
                .writeTo(androidSources(Modules.view))
        }

        val startModel = ClassName("$appPackage.di", "startModel")
        if (!startView.existsCommonInModule(Modules.model)) {
            FileSpec.builder(startModel.packageName, startModel.simpleName)
                .addFunction(
                    FunSpec.builder(startModel.simpleName)
                        .addModifiers(KModifier.SUSPEND)
                        .build()
                )
                .build()
                .writeTo(commonSources(Modules.model))
        }
    }

    fun getUsedSKLibrariesGroups(): List<String> {
        return Files.readAllLines(rootPath.resolve("skot_librairies.properties"))
            .filterNot { it.startsWith("//") }
            .map { it.substringBeforeLast(":") }
    }

    fun ComponentDef.hasModel() = componentsWithModel.contains(this)


    //Regarde si le fichier existe déjà, dans main ou dans une variante de main
    fun existsPath(path: Path, patternConbinable: String): Boolean {
        return Files.exists(path)
                ||
                variantsCombinaison.any {
                    Files.exists(path.replaceSegment(patternConbinable, "$patternConbinable$it"))
                }
    }
}


fun getAndroidPackageName(path: Path): String {
    val manifest = path.resolve("AndroidManifest.xml")
    return manifest.getDocumentElement().getAttribute("package")
}

//fun List<String>.packageToPath() = map { it.replace('.','/') }.joinToString(separator = "/")


fun Path.getDocument(): Document =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.toFile())

fun Path.getDocumentElement(): Element =
    getDocument().documentElement

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
    (0..(list.length - 1)).forEach {
        (list.item(it) as? Element)?.let { res.add(it) }
    }
    return res
}