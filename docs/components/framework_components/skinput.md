# SKInput

Show
a [TextInputEditText](https://developer.android.com/reference/com/google/android/material/textfield/TextInputEditText)
wrapped into
a [TextInputLayout](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout)
as described in Android documentation

This component allows the user to type in some text, which could be checked by a Regex.
If type in value does not match the regex, an error could be displayed

## Usage

In your ScreenVC, declare a SKInputVC

```kotlin
interface SignUpScreenVC : SKScreenVC {
    val lastName: SKInputVC
}
```

Launch the generation with the SKGenerate gradle task

In your screen, you have to instanciate your component :

```kotlin
override val lastName: SKInput = SKInput(
    hint = strings.signup_form_last_name,
    nullable = false,
    defaultErrorMessage = strings.signup_form_error_message_lastname,
    afterValidation = {
        enableSignUp(formIsValid())
    }
)
```

In your xml, you have to declare a TextInputLayout with the id `lastName` and a TextInputEditText as
below :

```xml

<com.google.android.material.textfield.TextInputLayout android:id="@+id/lastName"
    style="@style/NormalInput" android:layout_width="match_parent"
    android:layout_height="wrap_content" android:layout_marginBottom="10dp" tools:hint="Nom">

    <com.google.android.material.textfield.TextInputEditText android:layout_width="match_parent"
        android:layout_height="wrap_content" android:imeOptions="actionNext"
        android:inputType="text" />
</com.google.android.material.textfield.TextInputLayout>
```

As you can see, the input text could be nullable or not. You can use Regex to validate it :

```kotlin
override val birthDate: SKInput = SKInput(
    hint = strings.signup_form_birth_date,
    nullable = false,
    regex = """\d{2}\.\d{2}\.\d{4}""".toRegex(),
    afterValidation = {
        enableSignUp(formIsValid())
    },
)
```

## Properties

| name | type | description |  
|--|--|--|  
| hint | String? | Text to display before user type in text |
| nullable | Boolean | Indicate if input could be null. Default value is true |
| onDone | ((text: String?) -> Unit)? | Function called when user clicked on "done" Input Method
Action |
| viewType | SKInputVC.Type? | Select keyboard view type, defined by class SKInputVC.Type |
| showPassword | Boolean? | Show or hide password |
| defaultErrorMessage | String? |  Error Text to display when value is invalid |
| maxSize | Int? | Set a max text size for the input |
| regex | Regex? | Regex to use for input validation |
| modeErrorOnTap | Boolean | Display error each time user tap new character | 
| afterValidation |  ((validity: SKInput.Validity) -> Unit)? | Function called when typed value is validated |

| SKInputVC.Type | Input type | 
|--|--|
| Normal | InputType.TYPE_CLASS_TEXT |
| Number | InputType.TYPE_CLASS_NUMBER | 
| Phone | InputType.TYPE_CLASS_PHONE |
| Password | InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD |
| PasswordWithDefaultHintFont | InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD |
| NumberPassword | InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD |
| LongText | InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE |
| EMail | InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS |
| TextCapSentences | InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
| AllCaps | InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS |



## Other usage

You can also use an `SKSimpleInput` instead of a `SKInput`. You have to declare an EditText
in your xml file or a TextInputEditText (without TextInputLayout)

⚠️ Caution : use SKSimpleInput may cause error for displaying error, because error is manage by a SKData. 
Since EditText reset error on new text, error label will not be display again 
 
