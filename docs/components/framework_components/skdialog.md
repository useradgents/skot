# SKDialog

Show a [`SKScreen`](../skscreen.md) inside a modal dialog, backed by Android's
[DialogFragment](https://developer.android.com/reference/androidx/fragment/app/DialogFragment).
Like every presenter, it displays a full screen — not a custom layout — so the
content is just another `SKScreen` with its own ViewModel.

## Usage

In your `ScreenVC`, declare a `SKDialogVC`:

```kotlin
interface ProfileScreenVC : SKScreenVC {
    val dialog: SKDialogVC
}
```

Launch the generation with the `skGenerate` gradle task.
In your screen, instantiate the component:

```kotlin
override val dialog: SKDialog = SKDialog()
```

Then display a screen in it with `show`:

```kotlin
dialog.show(
    ConfirmDeleteScreen(itemId),
    cancelable = true,
    onDismiss = { /* user closed the dialog */ },
)
```

You need to provide a `SKScreen` to display into the dialog.

Call `dismiss` to close it:

```kotlin
dialog.dismiss()
```

> From inside the displayed screen you can also close it with
> `dismiss()` / `dismissIfPresented()` — see the
> [SKScreen navigation API](../skscreen.md#navigation-api). `SKDialog` sets the
> screen's `presenterDialog`, and calls `onRemove()` on the previous/closed
> screen automatically (no leak).

## Full-screen dialog

A second `show` overload renders the dialog full-screen using the
`skFullScreenDialogStyle` style:

```kotlin
dialog.show(
    EditProfileScreen(),
    cancelable = false,
    fullScreen = true,
)
```

You can also pass an explicit `Style` via the `style` parameter of the first
overload for custom theming.

## Functions

| name | signature | description |
|--|--|--|
| `show` | `show(screen, cancelable, onDismiss?, style?)` | Display `screen` in the dialog with an optional custom style. |
| `show` | `show(screen, cancelable, onDismiss?, fullScreen)` | Same, rendered full-screen when `fullScreen = true`. |
| `dismiss` | `dismiss()` | Close the dialog and release the shown screen. |

## Properties

| name | type | description |
|--|--|--|
| `shownScreen` | `SKScreen<*>?` | The screen currently displayed, or `null`. |
| `cancelable` | `Boolean` | Whether tapping outside / back dismisses the dialog (per `show` call). |
| `onDismiss` | `(() -> Unit)?` | Called when the dialog is dismissed (per `show` call). |

## See also

- [SKBottomSheet](skbottomsheet.md) — the same idea as a bottom sheet
- [SKScreen](../skscreen.md)
- [SKComponent in-depth guide](../skcomponent.md)