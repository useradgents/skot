//package tech.skot.generator
//
//import com.squareup.kotlinpoet.*
//import org.w3c.dom.Element
//import tech.skot.components.ComponentView
//import java.nio.file.Files
//import java.nio.file.Path
//import java.nio.file.Paths
//import javax.xml.parsers.DocumentBuilderFactory
//import kotlin.reflect.KCallable
//import kotlin.reflect.KClass
//import kotlin.reflect.KType
//import kotlin.reflect.full.superclasses
//
//fun getPackageName(path: Path): String {
//    val manifest = path.resolve("AndroidManifest.xml")
//    return manifest.getDocumentElement().getAttribute("package")
//}
//
//fun List<String>.packageToPath() = map { it.replace('.','/') }.joinToString(separator = "/")
//
//
//fun Path.getDocumentElement(): Element =
//        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.toFile()).documentElement
//
//fun Element.childElements(): List<Element> {
//    val elements: MutableList<Element> = mutableListOf()
//    for (i in 0 until childNodes.length) {
//        (childNodes.item(i) as? Element)?.let { elements.add(it) }
//    }
//    return elements
//}
//
//fun KClass<*>.`package`() = this.java.`package`
//fun KClass<*>.packageName() = this.java.`package`.name
//fun KType.isAny() = this.classifier == Any::class
//
//fun KCallable<*>.hasAnnotation(annotation:KClass<out Annotation>) = annotations.any { annotation.isInstance(it) }
//
//val appPackageName by lazy {
//    getPackageName(Paths.get("../viewmodel/src/androidMain")).substringBefore(".viewmodel")
//}
//
//val viewPackageName by lazy {
//    getPackageName(Paths.get("../view/src/main"))
//}
//
//
//fun cleanGenerated(moduleName:String) {
//    Paths.get("../$moduleName/generated").toFile().deleteRecursively()
//}
//
//
//fun Path.existsClass(pack: String, name: String) =
//        Files.exists(pathOfClass(pack, name))
//
//fun Path.pathOfClass(pack: String, name: String) = resolvePackage(pack)
//        .resolve("$name.kt")
//
//
//fun Path.resolvePackage(packageName: String): Path {
//    var path = this
//    if (packageName.isNotEmpty()) {
//        for (packageComponent in packageName.split('.').dropLastWhile { it.isEmpty() }) {
//            path = path.resolve(packageComponent)
//        }
//    }
//    return path
//}
//
//val toDoToGenerate = "TODO(\"SK: generated and not implemented\")"
//
//val generatedKotlinComment = "//### Generated by SKot do not modify"
//
//class ParamInfos(val name:String, val typeName: TypeName, val modifiers:List<KModifier> = emptyList(), val isVal:Boolean = true)
//
//fun TypeSpec.Builder.addPrimaryConstructorWithParams(vals: List<ParamInfos>): TypeSpec.Builder {
//    primaryConstructor(
//            FunSpec.constructorBuilder()
//                    .apply {
//                        vals
//                                .forEach {
//                                    addParameter(it.name, it.typeName)
//                                }
//                    }
//                    .build()
//    )
//    addProperties(
//            vals.filter { it.isVal }.map {
//                PropertySpec.builder(it.name, it.typeName)
//                        .addModifiers(it.modifiers)
//                        .initializer(it.name)
//                        .build()
//            }
//    )
//    return this
//}
//
//
//fun String.toClassName() = ClassName(this.substringBeforeLast('.'), this.substringAfterLast('.'))
//
//val lifecycleOwnerClassName = ClassName("androidx.lifecycle", "LifecycleOwner")
//val actionClassName = ClassName("tech.skot.view", "Action")
//val viewBindingClassName = ClassName("androidx.viewbinding", "ViewBinding")
//val layoutInflaterClassName = ClassName("android.view", "LayoutInflater")
//
//
//fun KClass<out ComponentView>.superView() = superclasses[0] as KClass<ComponentView>
//
//fun FileSpec.Builder.addImportClassName(className: ClassName) {
//    addImport(className.packageName, className.simpleName)
//}