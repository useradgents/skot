# SKCombo

Show a [MaterialAutoCompleteTextView](https://developer.android.com/reference/com/google/android/material/textfield/MaterialAutoCompleteTextView) as described in Android documentation

This component allows the user to choose an item in a list, which could be filtered by an input text

## Usage

In your ScreenVC, declare a SKComboVC 
```kotlin
interface SignUpScreenVC : SKScreenVC {
    val gender: SKComboVC
}
```
Launch the generation with the SKGenerate gradle task

In your screen, you have to instanciate your component : 

```kotlin
override val gender = SKCombo<String>(
    hint = strings.signup_form_gender,
    initialChoices = listOf(
        strings.signup_form_civility_miss,
        strings.signup_form_civility_madam,
        strings.signup_form_civility_sir
    ),
    onSelected = {
        enableSignUp(formIsValid())
        lastName.view.requestFocus()
    }
)
```

* in your xml, you can include a layout with id `gender` and layout `sk_combo` as below : 

```xml
<include
    android:id="@+id/gender"
    style="@style/ComboChoiceView"
    layout="@layout/sk_combo"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"/>
```

SKCombo could be a simple text, but you can use this component with a complex entity :

```kotlin
data class PhoneAreaCodeEntity(
    val label : String,
    val phoneAreaCode : String,
    val phoneMinLength : Int,
    val phoneMaxLength : Int,
)

override val phoneAreas = SKCombo<PhoneAreaCodeEntity>(
    onSelected = {
        enableSignUp(formIsValid())
        phone.view.requestFocus()
    },
    label = {
        it.phoneAreaCode
    }
)
```

## Properties
| name | type | description |  
|--|--|--|  
| hint | String? | Text to display before user type in text |
| error | String? | Text to display when there is an error |
| initialChoices |  List<D> | Available choices. Default value is an empty list | 
| enabled | Boolean | Enable or disable component. Default value is true |
| label | ((data: D) -> String)? |  Text to display in drop down list. If null, default `toString` function will be apply |
| inputText | ((data: D) -> String)? | Color applied for selected value |
| textColor |  ((data: D) -> Color)? | Color applied for drop down value |,
| striked | ((data: D) -> Boolean)? | Strike text or not |
| onSelected |  ((data: D) -> Unit)? | Function called when value is selected |