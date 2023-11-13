# SKAlert

Show an alert dialog as described in [Alert Dialog](https://developer.android.com/reference/android/app/AlertDialog) Android documentation 

## Usage

In your ScreenVC, declare a SKAlertVC

```kotlin
interface AccountScreenVC : SKScreenVC {
    val alert: SKAlertVC
}
```

Launch the generation with the SKGenerate gradle task. 
In your screen, you have to instanciate your component : 

```kotlin
override val alert = SKAlert()
```

Then in your app, when you have to display an alert, you can use `show` function :

```kotlin
alert.show(
    title = strings.account_screen_delete_account_alert_title,
    message = strings.account_screen_delete_account_alert_message,
    mainButton = SKAlertVC.Button(
        strings.account_screen_delete_account_alert_btn_delete
    ) {
        launchWithLoaderAndErrors {
            model.deleteAccount()
            snackBar.show(strings.account_screen_delete_account_success)
        }
    },
    secondaryButton = SKAlertVC.Button(
        strings.generic_cancel
    )
)
```

You can also set an input text when showing the dialog, as shown below : 

```kotlin
alert.show(
    title = strings.popin_forgot_password_title,
    message = strings.popin_forgot_password_desc,
    withInput = true,
    secondaryButton = SKAlertVC.Button(
        label = strings.cta_cancel
    ),
    mainButton = SKAlertVC.Button(
        label = strings.cta_send
    ) {
        alert.inputText.let {
            if (it.isNullOrBlank()) {
                onResetPasswordError(true)
            } else {
                resetPaswword(it)
            }
        }
    },
)
```

## Properties

| name | type | description |  
|--|--|--|  
| title | String? | Text to display for alert title |
| message |  String? | Text to display for alert message |
| cancelable |  Boolean | Set dialog cancelable or no. Default value is false |
| withInput |  Boolean | Enable or disable dialog input text. Default value is false |
| mainButton | SKAlertVC.Button | Default button. This is required, default value is `SKAlertVC.Button(label = "Ok", action = null)` |
| secondaryButton | SKAlertVC.Button? | Secondary button |
| neutralButton | SKAlertVC.Button? | Neutral button |

