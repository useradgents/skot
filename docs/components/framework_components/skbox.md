# SKBox

A simple **container** that holds one or more `SKComponent`s and lets you swap or
hide them at runtime. Use it to group components in a region of the layout, to
show a single piece of content that changes over time, or to toggle a part of the
UI on/off.

`SKBox` manages the lifecycle of its children: when an item is removed from the
box, its [`onRemove()`](../skcomponent.md#2-lifecycle--memory-management) is called
automatically (no leak).

## Usage

In your `ScreenVC`, declare a `SKBoxVC`:

```kotlin
interface HomeScreenVC : SKScreenVC {
    val banner: SKBoxVC
}
```

Launch the generation with the `skGenerate` gradle task.

In your screen, instantiate it with initial children (or none):

```kotlin
override val banner = SKBox()                 // empty
// or
override val banner = SKBox(promoComponent)   // vararg
// or
override val banner = SKBox(listOf(a, b))     // list
```

In your xml, add a container (e.g. a `FrameLayout`/`LinearLayout`) with id
`banner`.

## Driving it at runtime

```kotlin
banner.content = PromoComponent(offer)   // show a single component (replaces all)
banner.items = listOf(a, b, c)           // show several components
banner.content = null                    // clear
banner.hidden = true                     // hide the whole box
```

- `content` is a convenience for the single-child case: setting it wraps the value
  in a one-element `items` list (or empties it when `null`).
- Reassigning `items` / `content` calls `onRemove()` on any child that is no longer
  present.

## Properties

| name | type | description |
|--|--|--|
| `items` | `List<SKComponent<*>>` | The children currently displayed. |
| `content` | `SKComponent<*>?` | Single-child shortcut (get last/set as the only item). |
| `hidden` | `Boolean?` | Hide or show the whole box. |

## See also

- [SKFrame](skframe.md) — swap between a fixed set of screens
- [SKList](sklist.md) — a scrollable list/grid of components
- [SKComponent in-depth guide](../skcomponent.md)