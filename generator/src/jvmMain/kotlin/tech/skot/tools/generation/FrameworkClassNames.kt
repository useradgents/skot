package tech.skot.tools.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.jvm.jvmWildcard
import kotlinx.coroutines.GlobalScope
import java.lang.reflect.WildcardType

object FrameworkClassNames {
//    val icon = ClassName("")

    val skComponentVC = ClassName("tech.skot.core.components", "SKComponentVC")
    val skUri = ClassName("tech.skot.core", "SKUri")
    val skVisiblityListener = ClassName("tech.skot.core.components", "SKVisiblityListener")
    val skComponentView = ClassName("tech.skot.core.components", "SKComponentView")
    val skComponent = ClassName("tech.skot.core.components", "SKComponent").parameterizedBy(WildcardTypeName.producerOf(skComponentVC))
    val coroutineScope = ClassName("kotlinx.coroutines", "CoroutineScope")
    val coroutineContext = ClassName("kotlin.coroutines", "CoroutineContext")

    val skComponentViewMock = ClassName("tech.skot.core.components", "SKComponentViewMock")
    val skScreenViewMock = ClassName("tech.skot.core.components", "SKScreenViewMock")

    val coreViewInjector = ClassName("tech.skot.core.di", "CoreViewInjector")
    val coreViewInjectorMock = ClassName("tech.skot.core.di", "CoreViewInjectorMock")

    val skManualData = ClassName("tech.skot.model","SKManualData")
    val skData = ClassName("tech.skot.model","SKData")
    val globalCache = ClassName("tech.skot.model","globalCache")
    val skActivity = ClassName("tech.skot.core.components","SKActivity")
    val get = ClassName("tech.skot.core.di","get")
    val toSKUri = ClassName("tech.skot.core","toSKUri")


    val globalScope = ClassName("kotlinx.coroutines","GlobalScope")
    val dispatchers = ClassName("kotlinx.coroutines","Dispatchers")
    val launch = ClassName("kotlinx.coroutines","launch")

    val mapSKData = ClassName("tech.skot.model", "map")
    val combineSKData = ClassName("tech.skot.model", "combineSKData")

    val bm = ClassName("tech.skot.model", "SKBM")

    val transistionAndroidLegacy = ClassName("tech.skot.view", "SKTransitionAndroidLegacy")
    val permissionAndroidLegacy = ClassName("tech.skot.view", "SKPermissionAndroid")
    val skTransitionMock = ClassName("tech.skot.core.view", "SKTransitionMock")
    val skPermissionMock = ClassName("tech.skot.core.view", "SKPermissionMock")
    val skFunMock = ClassName("tech.skot.core.test", "SKFunMock")
    val skFunUnitMock = ClassName("tech.skot.core.test", "SKFunUnitMock")
    val skDataMock = ClassName("tech.skot.core.test", "SKDataMock")
}