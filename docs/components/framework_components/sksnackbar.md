# SKSnackbar 

Show an Android [Snackbar](https://developer.android.com/reference/com/google/android/material/snackbar/Snackbar) as described in Android documentation

## Usage
In your ScreenVC, declare a SKSnackBarVC

```kotlin
interface MyAccountScreenVC : SKScreenVC, AccountActions {
    val snackBar: SKSnackBarVC
}
```
Launch the generation with the SKGenerate gradle task.
In your screen, you have to instanciate your component :

```kotlin
override val snackBar = SKSnackBar()
```
Then in your app, display the snack bar with the `show` function :

```kotlin
snackBar.show(strings.account_screen_delete_account_success)
```

You can specify some attributes, such as position or icons : 

```kotlin
snackBar.show(
    message = skSpannedString {
        font(fonts.quicksand_bold) {
            append(strings.demat_notif_text_enabled)
        }
    },
    position = SKSnackBarVC.Position.TopWithInsetPadding,
    background = colors.no_more_paper,
    rightIcon = icons.ic_img_plane_ok
)
```

## Properties

| name | type | description |  
|--|--|--|
| message | SKSpannedString or String | The message to display |
| action | Action | An action to apply to the snack bar |
| position | SKSnackBarVC.Position | The snack bar position. Default value is SKSnackBarVC.Position.TopWithInsetMargin |
| duration | Long | How long the snack bar is shown. Default value is 3000 milliseconds |
| leftIcon | Icon? | Left icon resource to display | 
| rightIcon | Icon? | Right icon resource to display |
| background | Resource | Background to apply to the snack bar. It could be a color or a drawable resource |
| textColor | Color? | Message text color |
| infiniteLines | Boolean | Set snack bar text multiline or not. Default value is false |
| centerText | Boolean | Center text. Default value is false |
| slideAnimation | Boolean | Set snack bar animation enabled or disabled. Default value is false |
| isGestureInsetBottomIgnored | Boolean | Sets whether this bottom bar should adjust it's position based on the system gesture area on Android Q and above. Default value is false |

| SKSnackBarVC.Position |
|--|
| Bottom |
| TopWithInsetMargin |
| TopWithInsetPadding |
| TopWithCustomMargin(val margin: Int) |
| BottomWithCustomMargin(val margin: Int) |
