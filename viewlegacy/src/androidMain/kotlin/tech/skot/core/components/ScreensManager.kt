package tech.skot.core.components

import tech.skot.view.SKPermissionsRequestResultAndroid
import tech.skot.view.live.SKMessage

object ScreensManager {
    private var counter: Long = 0
    private val instances: MutableMap<Long, SKScreenViewProxy<*>> = mutableMapOf()

    fun getInstance(key: Long): SKScreenViewProxy<*>? = instances[key]

    fun addScreen(screen: SKScreenViewProxy<*>): Long {
        val key = counter++
        instances[key] = screen
        return key
    }

    fun removeScreen(screen: SKScreenViewProxy<*>) {
        instances.remove(screen.key)
    }

    val backPressed = SKMessage<Unit>(true)
    val permissionsResults = SKMessage<SKPermissionsRequestResultAndroid>()

    const val SK_EXTRA_VIEW_KEY = "SK_EXTRA_VIEW_KEY"
    const val SK_ARGUMENT_VIEW_KEY = "SK_ARGUMENT_VIEW_KEY"
    const val SK_ARGUMENT_DIALOG_STYLE = "SK_ARGUMENT_DIALOG_STYLE"
    const val SK_ARGUMENT_CAN_SET_FULL_SCREEN = "SK_ARGUMENT_CAN_SET_FULL_SCREEN"
}
