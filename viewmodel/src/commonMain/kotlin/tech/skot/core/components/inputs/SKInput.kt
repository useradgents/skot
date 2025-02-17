package tech.skot.core.components.inputs

import tech.skot.core.components.SKComponent
import tech.skot.core.di.coreViewInjector

open class SKInput(
    hint: String? = null,
    protected val nullable: Boolean = true,
    onDone: ((text: String?) -> Unit)? = null,
    viewType: SKInputVC.Type? = null,
    showPassword: Boolean? = null,
    protected val defaultErrorMessage: String? = null,
    private val maxSize: Int? = null,
    private val regex: Regex? = null,
    private val modeErrorOnTap: Boolean = false,
    private val afterValidation: ((validity: SKInput.Validity) -> Unit)? = null,
) : SKComponent<SKInputVC>() {
    sealed class Validity(val errorMessage: String?) {
        abstract val isValid: Boolean

        data object Valid : Validity(null) {
            override val isValid = true
        }

        class Error(errorMessage: String?) : Validity(errorMessage) {
            override val isValid = false
        }
    }

    public var setErrorLambda: ((error: String?) -> Unit)? = null

    protected open fun setError(error: String?) {
        setErrorLambda?.invoke(error) ?: run {
            view.error = error
        }
    }

    private var _value: String? = null
    var value: String?
        get() = _value
        set(newVal) {
            onNewValue(newVal)
        }

    protected open fun format(str: String?): String? {
        return str
    }

    protected open fun validate(str: String?): Validity {
        return if ((nullable && str.isNullOrEmpty()) ||
            (
                !str.isNullOrBlank() && (maxSize == null || str.length <= maxSize) && (
                    regex == null ||
                        regex.matches(
                            str,
                        )
                )
            )
        ) {
            Validity.Valid
        } else {
            Validity.Error(defaultErrorMessage)
        }
    }

    val isValid: Boolean
        get() = validity.isValid

    protected fun onNewValue(str: String?) {
        if (_value != str && !(str == "" && _value == null)) {
            val formated = format(str)
            view.text = formated
            _value = formated
            updateValidityWith(formated)
        }
    }

    private fun updateValidityWith(value: String?) {
        validate(value).let { newValidity ->
            validity = newValidity
            if (modeErrorOnTap) {
                setError(validity.errorMessage)
            } else {
                setError(null)
            }
            afterValidation?.invoke(newValidity)
        }
    }

    public fun updateValidy() {
        updateValidityWith(value)
        setError(validity.errorMessage)
    }

    protected val _error = Validity.Error(defaultErrorMessage)

    private var validity: Validity = if (nullable) Validity.Valid else _error

    protected open fun onFocus() {
    }

    protected open fun onFocusLost() {
        setError(validity.errorMessage)
    }

    fun resetWithoutValidate() {
        view.text = null
        setError(null)
        _value = null
    }

    override val view =
        coreViewInjector.input(
            onInputText = {
                onNewValue(it)
            },
            type = viewType,
            maxSize = maxSize,
            onFocusChange =
                {
                    if (it) {
                        onFocus()
                    } else {
                        onFocusLost()
                    }
                },
            onDone = onDone,
            hintInitial = hint,
            textInitial = null,
            errorInitial = null,
            hiddenInitial = false,
            enabledInitial = true,
            showPasswordInitial = showPassword,
        )
}

@Deprecated("Use SKinput with regex param instead")
open class SKInputRegExp(
    hint: String? = null,
    nullable: Boolean = true,
    onDone: ((text: String?) -> Unit)? = null,
    viewType: SKInputVC.Type? = null,
    showPassword: Boolean? = null,
    defaultErrorMessage: String? = null,
    maxSize: Int? = null,
    regex: Regex? = null,
    modeErrorOnTap: Boolean = false,
    afterValidation: ((validity: SKInput.Validity) -> Unit)? = null,
) : SKInput(
        hint,
        nullable,
        onDone,
        viewType,
        showPassword,
        defaultErrorMessage,
        maxSize,
        regex,
        modeErrorOnTap,
        afterValidation,
    )

open class SKSimpleInput(
    hint: String? = null,
    nullable: Boolean = true,
    onDone: ((text: String?) -> Unit)? = null,
    viewType: SKInputVC.Type? = null,
    showPassword: Boolean? = null,
    defaultErrorMessage: String? = null,
    maxSize: Int? = null,
    regex: Regex? = null,
    modeErrorOnTap: Boolean = false,
    afterValidation: ((validity: SKInput.Validity) -> Unit)? = null,
) : SKInput(
        hint,
        nullable,
        onDone,
        viewType,
        showPassword,
        defaultErrorMessage,
        maxSize,
        regex,
        modeErrorOnTap,
        afterValidation,
    ) {
    override val view: SKSimpleInputVC =
        coreViewInjector.inputSimple(
            onInputText = {
                onNewValue(it)
            },
            type = viewType,
            maxSize = maxSize,
            onFocusChange =
                {
                    if (it) {
                        onFocus()
                    } else {
                        onFocusLost()
                    }
                },
            onDone = onDone,
            hintInitial = hint,
            textInitial = null,
            errorInitial = null,
            hiddenInitial = false,
            enabledInitial = true,
            showPasswordInitial = showPassword,
        )
}
