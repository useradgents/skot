package tech.skot.core.components.inputs

import tech.skot.core.components.SKComponentViewMock

class SKSimpleInputViewMock(
    onInputText: (newText: String?) -> Unit,
    type: SKInputVC.Type?,
    maxSize: Int?,
    onFocusChange: ((hasFocus: Boolean) -> Unit)?,
    onDone: ((text: String?) -> Unit)?,
    hintInitial: String?,
    textInitial: String?,
    errorInitial: String?,
    hiddenInitial: Boolean?,
    enabledInitial: Boolean?,
    showPasswordInitial: Boolean?,
) : SKComponentViewMock(), SKSimpleInputVC {
    override val onInputText: (newText: String?) -> Unit = onInputText
    override val type: SKInputVC.Type? = type
    override val maxSize: Int? = maxSize
    override val onFocusChange: ((hasFocus: Boolean) -> Unit)? = onFocusChange
    override val onDone: ((text: String?) -> Unit)? = onDone
    override var hint: String? = hintInitial
    override var text: String? = textInitial
    override var error: String? = errorInitial
    override var hidden: Boolean? = hiddenInitial
    override var enabled: Boolean? = enabledInitial
    override var showPassword: Boolean? = showPasswordInitial

    var requestFocusCounter = 0

    override fun requestFocus() {
        requestFocusCounter++
    }

    override fun getCursorSelection(onResult: (Pair<Int, Int>) -> Unit) {
        onResult((text?.length?:0) to (text?.length?:0))
    }
}

fun SKSimpleInputVC.requestFocusCount(): Int = (this as SKInputViewMock).requestFocusCounter
