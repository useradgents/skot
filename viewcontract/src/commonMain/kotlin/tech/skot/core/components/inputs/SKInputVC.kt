package tech.skot.core.components.inputs

import tech.skot.core.components.SKComponentVC
import tech.skot.core.components.SKLayoutIsSimpleView

@SKLayoutIsSimpleView
interface SKInputVC: SKComponentVC {

    sealed class Type {
        data object Normal : Type()
        data object Number : Type()
        data object Phone : Type()
        data object Password: Type()
        data object PasswordWithDefaultHintFont: Type()
        data object NumberPassword : Type()
        data object VisiblePassword : Type()
        data object LongText : Type()
        data object EMail: Type()
        data object TextCapSentences: Type()
        data object AllCaps: Type()
    }

    val onInputText: (newText: String?) -> Unit

    val type: Type?
    val maxSize: Int?
    val onFocusChange: ((hasFocus:Boolean) -> Unit)?
    val onDone: ((text: String?) -> Unit)?

    var hint: String?
    var text: String?
    var error: String?
    var hidden: Boolean?
    var enabled: Boolean?
    var showPassword:Boolean?

    fun requestFocus()

}

@SKLayoutIsSimpleView
interface SKSimpleInputVC:SKInputVC