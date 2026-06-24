# SKImageButton

Show an image button (an icon that reacts to taps), as a counterpart to
[SKButton](skbutton.md) for icon-only actions.

## Usage

In your `ScreenVC`, declare a `SKImageButtonVC`:

```kotlin
interface DetailScreenVC : SKScreenVC {
    val favoriteButton: SKImageButtonVC
}
```

Launch the generation with the `skGenerate` gradle task.

In your screen, instantiate the component (an initial `Icon` is required):

```kotlin
override val favoriteButton = SKImageButton(icon = Icons.heartOutline) {
    toggleFavorite()   // onTap
}
```

In your xml, add an `ImageButton` (or any `ImageView`) with id `favoriteButton`.

You can change the icon and behaviour at runtime from the ViewModel:

```kotlin
favoriteButton.icon = if (isFav) Icons.heartFilled else Icons.heartOutline
favoriteButton.enabled = canFavorite
```

## Properties

| name | type | description |
|--|--|--|
| `icon` | `Icon` | The icon shown on the button. |
| `onTap` | `(() -> Unit)?` | Called when the button is tapped. |
| `enabled` | `Boolean?` | Whether the button is enabled. |
| `hidden` | `Boolean?` | Whether the button is hidden. |
| `debounce` | `Long?` | Debounce window (ms) ignoring repeated taps. Default `500`. |

## See also

- [SKButton](skbutton.md) — text button
- [SKComponent in-depth guide](../skcomponent.md)