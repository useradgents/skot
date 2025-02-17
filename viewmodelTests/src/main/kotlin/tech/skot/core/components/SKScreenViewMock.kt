package tech.skot.core.components

import tech.skot.core.view.Color

open class SKScreenViewMock : SKComponentViewMock(), SKScreenVC {
    override var onBackPressed: (() -> Unit)? = null

    fun backPressed() {
        onBackPressed?.invoke()
    }

    override var statusBarColor: Color? = null
    override fun exit() {
        // Do Nothing
    }
}

fun SKScreenVC.backPressed() {
    (this as SKScreenViewMock).backPressed()
}
