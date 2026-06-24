# SKComponent — In-depth guide

`SKComponent` is the base building block of the **ViewModel layer** in SKot.
Every interactive piece of UI — a full screen, a button, a list, a dialog — is
backed by an `SKComponent`. This document explains what an `SKComponent` *is*,
how it behaves at runtime, and how to use everything it offers.

> If you just want to create your first component, read
> [Create a component](../start/createcomponent.md) first, then come back here
> for the details.

---

## 1. The big picture

```
ViewContract (*VC)            ViewModel                    View (Android)
interface MyScreenVC   <----  class MyScreen      ---->    MyScreenViewProxy
  : SKScreenVC                 : SKScreen<MyScreenVC>        + MyScreenView
        ▲                            │                            │
        └──── view: MyScreenVC ──────┘   (talks only to the VC)   │
                                                          renders / binds
```

- **The ViewContract** (`*VC` interface) declares *what* the screen exposes:
  values, variables, functions and sub-components. It is pure Kotlin, no Android.
- **The ViewModel** (`SKComponent` subclass) holds the business logic. It only
  ever talks to its `view` property, which is typed as the ViewContract. **It has
  no knowledge of Android.**
- **The View** (generated `*ViewProxy` + `*View`) implements the contract on
  Android. The *proxy* lets the ViewModel mutate UI state even when no view is
  currently attached — changes are queued and applied on bind.

Because the ViewModel only depends on the contract, it is fully unit-testable on
the JVM with a mocked view.

### Declaring sub-components: `@SKUses` and `@SKOpens`

The generator needs to know which **app-defined** components to build. It already
auto-detects any component you declare as a typed property *in your app package*.
Two annotations cover the remaining cases:

```kotlin
@SKUses([CartBadgeVC::class])         // app components used but not exposed as a property
@SKOpens([ProductScreenVC::class])    // app screens this screen can navigate to (push)
interface HomeScreenVC : SKScreenVC {
    val header: HeaderVC               // auto-detected (typed property, app package)
    fun showCartBadge(count: Int)      // CartBadge is used in code, so declare it in @SKUses
}
```

- **`@SKUses([XxxVC::class])`** — lists the app components this component uses so
  they get generated and their `onRemove()` is wired into the cascade. Use it for
  components that are not exposed as a direct typed property (created in code,
  used inside functions, etc.).
- **`@SKOpens([XxxScreenVC::class])`** — lists the app screens this screen can
  `push` / navigate to. It guarantees those screens are generated and reachable
  even though they are not held as a property.

> ⚠️ **Framework components are already known to the generator.** Never put
> `SKLoaderVC`, `SKListVC`, `SKButtonVC`, etc. in `@SKUses`/`@SKOpens` — only your
> own app components belong there. Listing them as a typed property (e.g.
> `val loader: SKLoaderVC`) is enough.
>
> See the [annotations reference](../annotations.md) for every SKot annotation.

```kotlin
abstract class SKComponent<out V : SKComponentVC> : CoroutineScope {
    abstract val view: V
}
```

The single abstract member is `view`. Everything else is behaviour provided for
free.

---

## 2. Lifecycle & memory management

This is the most important part to understand — most memory leaks come from
ignoring it.

### `SKComponent` is a `CoroutineScope`

```kotlin
protected val job = SupervisorJob()
final override val coroutineContext = (Dispatchers.Main + job)
```

- Every coroutine you launch inside a component runs on **`Dispatchers.Main`** and
  is tied to the component's `job`.
- It is a **`SupervisorJob`**: if one child coroutine fails, the others keep
  running (a failing data load does not tear down the whole component).

### `onRemove()` is the teardown hook

```kotlin
open fun onRemove() {
    removeObservers.forEach { it.invoke() }  // unsubscribe pokers/observers
    view.onRemove()                          // tell the view to release
    job.cancel()                             // cancel ALL coroutines
}
```

When a component is removed, **all of its coroutines are cancelled** and all
registered observers are detached. This is automatic — you rarely call
`onRemove()` yourself.

**Container components propagate `onRemove()` to their children.** This is the
chain that prevents leaks:

- `SKStack` / `SKRootStack` → removes screens dropped from the stack
- `SKBox` → removes items no longer in `items`
- `SKList` → removes items no longer in `items`
- `SKPager` → removes its screens
- The generated `*Gen` parent class → removes every declared sub-component

```kotlin
// Generated automatically for a screen that declares `bottomNav`:
override fun onRemove() {
    bottomNav.onRemove()
    super.onRemove()
}
```

> ⚠️ **Rule of thumb:** if you create a child component *outside* the generated
> contract (e.g. you build it manually and swap it in/out yourself), you are
> responsible for calling its `onRemove()` when you discard it. Otherwise its
> coroutines and observers leak. Putting it inside a managed container
> (`SKBox`, `SKList`, `SKStack`…) does this for you.

### Screen visibility (SKScreen only)

`SKScreen` implements `SKVisiblityListener` and is driven by the host's lifecycle:

```kotlin
override fun onResume() {     // CallSuper — keep it if you override
    if (!resumedOnce) { resumedOnce = true; onStart() }
}
override fun onPause() { }
open fun onStart() { }        // called once, the first time the screen resumes
```

- `onStart()` — override for one-time setup that needs the screen to be live.
- `onResume()` / `onPause()` — override for repeated foreground/background work
  (always call `super`).

---

## 3. Launching work (coroutines)

You can call `launch { }` directly (it *is* a scope), but prefer the helpers:

| Helper | When to use it |
|---|---|
| `launch { }` | Plain coroutine, you handle errors yourself. |
| `launchNoCrash { }` | Fire-and-forget; exceptions are only logged, never crash. |
| `launchWithLoaderAndErrors { }` | The common case: show a loader + route errors. |
| `launchWithOptions(...) { }` | Full control over loader / error handling. |

### `launchWithOptions`

```kotlin
launchWithOptions(
    withLoader = true,                 // requires a `loader` (see below)
    specificErrorTreatment = { ex -> /* custom */ },
    errorMessage = "Could not load",   // passed to treatError if no specific one
    specificLoader = anotherLoader,    // override the default loader
) {
    // suspend work
}
```

Behaviour: starts the loader → runs the block → on exception (except
`CancellationException`) calls `specificErrorTreatment` or falls back to
`treatError(...)` → always stops the loader in `finally`.

`launchWithLoaderAndErrors { }` is simply `launchWithOptions(withLoader = true)`.

---

## 4. Loaders

```kotlin
open val loader: SKLoader? = null   // override to enable withLoader = true
```

`SKLoader` is **counter-based**: nested/parallel loads are handled correctly —
the indicator is visible while at least one job is running.

```kotlin
override val loader = SKLoader().apply {
    onStateChanged = { visible -> /* react to spinner on/off */ }
}
```

- `workStarted()` / `workEnded()` — increment/decrement the counter (called for
  you by `launchWith...`).
- `isLoading()` / `isNotLoading()` — query the state.

If you call `withLoader = true` without providing a `loader` (and no
`specificLoader`), an `IllegalStateException` is thrown.

---

## 5. Reacting to data — `SKData`

The Model layer exposes observable data as `SKData<T>`. Two extensions on
`SKComponent` connect it to your UI. Both **deduplicate**: the block only runs
when the value actually changes.

### `onData` — load + observe (the main one)

```kotlin
myModel.userData.onData(
    validity = 60_000,                         // cache validity window (ms)
    withLoaderForFirstData = true,             // show loader until first value
    fallBackDataBeforeFirstDataLoaded = false, // show stale fallback meanwhile
    fallBackDataIfError = false,               // show fallback if load fails
    treatErrors = true,                        // route errors to treatError
    defaultErrorMessage = "Loading failed",
) { user ->
    view.name = user.name                      // runs on every change
}
```

What it does, in order:
1. (optional) emits fallback data immediately so the UI isn't empty.
2. calls `get(validity)` for the first fresh value (under a loader).
3. then collects the `flow` and re-runs your block on every subsequent change.

Use `cancelOnData("reason")` inside the block to abort the current treatment
(throws a `CancellationException`, which is swallowed safely).

### `onChange` — observe only

```kotlin
mySwitch.isOn.onChange { on -> view.checked = on }
```

Lighter than `onData`: it only collects the flow (no initial `get`, no loader,
no error routing). Use it when the data is already loaded elsewhere.

> See the [SKData & Model layer guide](../skdata.md) for the data types behind
> these observations (caching, validity, `map`/`combine`/`wrap`, BusinessModels).

---

## 6. One-shot events — `Poker` / `observe`

A `Poker` is a value-less event source ("something happened, refresh"). Observing
through `observe` auto-unsubscribes on `onRemove()`:

```kotlin
observe(refreshPoker) {
    reload()   // called every time the poker is poked
}
```

Never subscribe to a `Poker` manually inside a component — use `observe` so the
subscription is cleaned up automatically.

---

## 7. Errors

```kotlin
treatError(exception, errorMessage)
```

By default this delegates to a single, app-wide handler you configure once:

```kotlin
SKComponent.errorTreatment = { component, exception, message ->
    // central error UI / logging
}
```

If you never set `errorTreatment` *and* never override `treatError`, calling it
throws `IllegalStateException`. Override `treatError` on a component for
local behaviour.

> See the dedicated [Error handling guide](../error-handling.md) for the full
> picture (launchers, `onData` fallbacks, model-layer resilience).

---

## 8. User messages

Short feedback messages, routed to the view:

```kotlin
displayMessageInfo("Saved")
displayMessageWarning("Check your connection")
displayMessageError("Something went wrong")
displayMessageDebug("verbose dev message")
```

For richer dialogs use `SKComponentVC.Message.Alert` (title, message, buttons,
optional input) via the view, or the dedicated `SKAlert` component.

---

## 9. Permissions

```kotlin
doWithPermission(SKPermission.Camera, onKo = { /* denied */ }) {
    openCamera()   // runs only if granted
}

doWithPermissions(SKPermission.Camera, SKPermission.Microphone) {
    startRecording()   // runs only if ALL granted
}

if (hasPermission(SKPermission.Camera)) { /* ... */ }
```

Notifications have dedicated helpers: `hasNotificationsPermission()`,
`requestNotificationsPermissions(onOk, onKo)`, `notificationsPermissionManaged()`.

---

## 10. Building lists of components

When feeding an `SKList`, you assemble `List<SKComponent<*>>`. Helpers make this
readable:

```kotlin
val items = header + sectionA + sectionB              // plus operators
    .plusIfNotNull(footerIfAny)                       // append nullable
    .join { Separator() }                             // separator between each
    .joinByGroups(count = 3) { GroupSeparator() }     // separator every N
```

Override `computeItemId()` when items need stable identity for diffing/animation
(defaults to the component instance itself). Set `onSwipe` to enable
swipe-to-action inside an `SKList`.

---

## 11. Logging

```kotlin
logD("state = $state")        // "MyComponent -- state = ..."
logE(throwable, "load failed")
```

Both prefix the message with the component's simple class name via `SKLog`.

---

## 12. The component catalog

`SKComponent` has many ready-made subclasses. See
[Framework components](framework_components/readme.md) for each one. Quick map:

| Component | Role |
|---|---|
| [`SKScreen`](skscreen.md) | A navigable page. Adds `push/replaceWith/finish/dismiss/kill`. |
| [`SKStack`](framework_components/skstack.md) / `SKRootStack` | A navigation stack of screens with transitions. |
| [`SKFrame`](framework_components/skframe.md) | Shows one screen from a fixed set (swap, no stack). |
| [`SKList`](framework_components/sklist.md) | List or grid of components. |
| [`SKPager`](framework_components/skpager.md) / `SKPagerWithTabs` | Swipeable pages. |
| `SKBox` | A container whose `items` / `content` / `hidden` you control. |
| [`SKLoader`](framework_components/skloader.md) | Counter-based loading indicator. |
| [`SKButton`](framework_components/skbutton.md), [`SKInput`](framework_components/skinput.md), [`SKCombo`](framework_components/skcombo.md), `SKImageButton` | Inputs. |
| [`SKDialog`](framework_components/skdialog.md), [`SKBottomSheet`](framework_components/skbottomsheet.md), [`SKAlert`](framework_components/skalert.md), [`SKSnackBar`](framework_components/sksnackbar.md), `SKWindowPopup` | Presented over content. |
| [`SKWebView`](framework_components/skwebview.md) | Embedded web content. |

---

## 13. End-to-end example

```kotlin
// ViewContract (viewcontract module)
// Note: SKLoaderVC is a framework component, so it does NOT go in @
// s.
// @SKUses is only for app-defined components you want the generator to build.
interface ProfileScreenVC : SKScreenVC {
    val loader: SKLoaderVC
    var name: String?
    var email: String?
    fun onSaved()
}

// ViewModel (viewmodel module) — extends the generated *Gen class
class ProfileScreen(private val userId: String) : ProfileScreenGen() {

    override val view = viewInjector.profileScreen(
        visibilityListener = this,
        loader = loaderComponent.view,
    )

    private val loaderComponent = SKLoader()
    override val loader get() = loaderComponent

    override fun onStart() {
        userModel.user(userId).onData(
            withLoaderForFirstData = true,
            defaultErrorMessage = "Could not load profile",
        ) { user ->
            view.name = user.name
            view.email = user.email
        }
    }

    fun save(newName: String) {
        launchWithLoaderAndErrors(errorMessage = "Save failed") {
            userModel.updateName(userId, newName)
            view.onSaved()
            finish()   // pop this screen off the stack
        }
    }
}
```

---

## See also

- [SKComponent overview](readme.md)
- [SKScreen](skscreen.md)
- [Annotations reference](../annotations.md)
- [Create a component](../start/createcomponent.md)
- [Create a screen](../start/createscreen.md)
- [Framework components](framework_components/readme.md)