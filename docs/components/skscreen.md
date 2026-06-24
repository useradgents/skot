# SKScreen

`SKScreen` is the component type used for **navigable pages**. With SKot you no
longer think in terms of `Activity` or `Fragment`: every navigable view is an
`SKScreen`, whether it is shown in a [SKStack](framework_components/skstack.md),
a [SKDialog](framework_components/skdialog.md) or a
[SKBottomSheet](framework_components/skbottomsheet.md). Parameters are simply
passed through the constructor — no `Bundle` required.

```kotlin
class ProductScreen(private val productId: String) : ProductScreenGen()
```

`SKScreen` **extends [`SKComponent`](skcomponent.md)** and inherits everything
from it (coroutine scope, `onData`/`onChange`, loaders, error handling,
permissions, teardown…). This page only covers what is **specific to screens**:
navigation, the screen lifecycle, and the `SKScreenView` presentation options.

> For the base behaviour shared by all components, read the
> [SKComponent in-depth guide](skcomponent.md).

---

## Creating a screen

It is exactly like creating a [SKComponent](readme.md) — the contract inherits
from `SKScreenVC` instead of `SKComponentVC`, and nothing else is required.

```kotlin
interface ProductScreenVC : SKScreenVC {
    var title: String?
}
```

See [Create my first screen](../start/createscreen.md).

> :bulb: We recommend suffixing screen names with `Screen` for readability. It is
> a convention, not a requirement.

---

## Navigation API

A screen knows its `parent` stack and (when presented) its presenter. These
methods drive navigation:

| Method | Effect |
|---|---|
| `push(screen)` | Add `screen` on top of the current stack (falls back to the root stack if this screen has no parent). |
| `replaceWith(screen)` | Replace this screen by another in the stack. |
| `finish(transition?)` | Close this screen (pop it from its stack). Throws if it has no stack parent. |
| `finishIfInAStack()` | Like `finish()` but does nothing instead of throwing when there is no parent. |
| `removeAllScreensOnTop()` | Pop every screen stacked above this one, making it the top again. |
| `dismiss()` | Dismiss this screen when shown in a `SKDialog`/`SKBottomSheet`. Throws if it is not presented. |
| `dismissIfPresented()` | Dismiss if presented in a dialog or bottom sheet; no-op otherwise. |
| `kill()` | Reset the whole root stack (clears the entire navigation). |
| `exit()` | Exit the app task (finishes and removes the task). |

```kotlin
fun openDetails() = push(DetailsScreen(itemId))

fun onSaved() {
    displayMessageInfo("Saved")
    finish()
}

fun onLogout() = kill()   // wipe the stack and start over
```

> Most of these throw an `IllegalStateException` if the screen currently has no
> stack parent (it was never pushed). Use the `…IfInAStack` / `…IfPresented`
> variants when you are not sure of the context.

### Presentation context

When a screen is shown over content, its presenter is tracked for you:

- `presenterDialog: SKDialog?` — set when shown via [`SKDialog`](framework_components/skdialog.md).
- `presenterBottomSheet: SKBottomSheet?` — set when shown via `SKBottomSheet`.

`dismiss()` / `dismissIfPresented()` use these to close the right presenter.

---

## Screen lifecycle

`SKScreen` implements `SKVisiblityListener` and is driven by the host lifecycle:

```kotlin
override fun onResume() {        // CallSuper — fires onStart() once
    if (!resumedOnce) { resumedOnce = true; onStart() }
}
override fun onPause() { }
open fun onStart() { }           // called once, the first time the screen resumes
```

- **`onStart()`** — override for one-time setup that needs the screen to be live
  (e.g. start observing data). Called only on the first resume.
- **`onResume()` / `onPause()`** — override for repeated foreground/background
  work. Always call `super`.
- **`onRemove()`** — teardown (cancels coroutines, detaches observers). Inherited
  from `SKComponent`; the generated `*Gen` parent already cascades it to declared
  sub-components. See [SKComponent — lifecycle](skcomponent.md#2-lifecycle--memory-management).

---

## SKScreenVC — contract members

`SKScreenVC` adds these to `SKComponentVC`:

| Member | Role |
|---|---|
| `var onBackPressed: (() -> Unit)?` | Callback invoked on hardware/gesture back while the screen is resumed. |
| `var statusBarColor: Color?` | Status bar color for this screen. |
| `fun exit()` | Exit the app task (backing `SKScreen.exit()`). |

---

## SKScreenView — presentation options

The generated Android view (`SKScreenView`) exposes overridable properties that
control how the screen interacts with the system window:

| Property | Default | Effect |
|---|---|---|
| `fullScreen: Boolean` | `false` | Draw edge-to-edge, behind the status bar. |
| `lightStatusBar: Boolean?` | `null` | Force light (dark icons) or dark status bar content. |
| `secured: Boolean` | `false` | Apply `FLAG_SECURE` — blocks screenshots & screen recording. |
| `withWindowsInsetsPaddingTop: Boolean` | `false` | Automatically add top padding equal to the system-bars inset. |
| `onWindowInset: ((WindowInsetsCompat) -> Unit)?` | `null` | Custom handling of window insets (overrides the auto top padding). |

```kotlin
class ProductScreenView(/* … */) : SKScreenView<ProductScreenBinding>(/* … */) {
    override val fullScreen = true
    override val lightStatusBar = false
    override val secured = true   // e.g. a screen showing sensitive data
}
```

The status bar **color** is set through the contract (`statusBarColor`), not the
view, so the ViewModel can change it: `view.statusBarColor = ColorRes(R.color.x)`.

---

## See also

- [SKComponent in-depth guide](skcomponent.md)
- [SKStack](framework_components/skstack.md) — the navigation stack
- [SKDialog](framework_components/skdialog.md) / [SKBottomSheet](framework_components/skbottomsheet.md) — presenting a screen
- [Annotations reference](../annotations.md)
- [Create my first screen](../start/createscreen.md)