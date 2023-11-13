# SKBottomSheet

Show a bottom sheet dialog, as described in [BottomSheetDialogFragment](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetDialogFragment) Android documentation

## Usage

In your ScreenVC, declare a SKBottomSheetVC

```kotlin
interface AntiwasteBasketScreenVC : SKScreenVC, ExternalActions {
    val bottomSheet: SKBottomSheetVC
}
```

Launch the generation with the SKGenerate gradle task.
In your screen, you have to instanciate your component : 

```kotlin
override val bottomSheet: SKBottomSheet = SKBottomSheet()
```

Then in your app, display the bottom sheet dialog with the `show` function : 

```kotlin
 bottomSheet.show(
    ModifyQuantityBottomSheet( /* ... */),
    skipCollapsed = false,
    expanded = false
)
```

You need to provide a `SKScreen` to display into the bottom sheet.

Call `dismiss` to close the dialog : 

```kotlin
bottomSheet.dismiss()
```

## Properties

| name | type | description |  
|--|--|--|  
| screen | SKScreen<*> | SKScreen to display |
| onDismiss | (() -> Unit)? | Function called when bottom sheet is closed | 
| expanded | Boolean | Set bottom sheet expanded or not, as described into [STATE_EXPANDED Android documentation](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior#STATE_EXPANDED). Default value is true |
| skipCollapsed | Boolean | Set bottom sheet skip collapsed, as described into [setSkipCollapsed Android documentation](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior#setSkipCollapsed(boolean)). Default value is true |
| fullHeight | Boolean | Set bottom sheet full height. Default value is false |
| resizeOnKeyboard | Boolean | Adjust bottom sheet height when keyboard show up. Default value is false |
