package tech.skot.core.components.inputs

import tech.skot.core.components.SKComponentVC
import tech.skot.core.view.Color

interface SKComboVC : SKComponentVC {
    val hint: String?
    val onSelected: ((choice: Any?) -> Unit)?
    var choices: List<Choice>
    var selected: Choice?
    var enabled: Boolean?
    var hidden: Boolean?
    var dropDownDisplayed: Boolean
    var error: String?

    val oldSchoolModeHint: Boolean

    class Choice(
        val data: Any?,
        val text: String = data.toString(),
        val strikethrough: Boolean = false,
        val textColor: Color? = null,
        val inputText: String = text,
    )
}
