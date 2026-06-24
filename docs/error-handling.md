# Error handling

SKot has **no custom exception hierarchy**. Failures — network errors, timeouts,
serialization problems, your own `throw` — propagate as ordinary Kotlin
`Exception`s from the Model layer up to the component that triggered them. What
SKot gives you is a **consistent place to catch and treat** those exceptions, so
you don't scatter `try/catch` across your ViewModels.

Everything below lives on [`SKComponent`](components/skcomponent.md), so it is
available in every component and screen.

---

## 1. The mental model

```
Model (fetchData / SKData.get / update)   --- throws --->
ViewModel coroutine (launchWith… / onData) --- catches --->
treatError(exception, message)             --- delegates -->
SKComponent.errorTreatment (app-wide handler)  --- shows UI / logs
```

Two rules to remember:

1. **Always launch through a helper** (`launchWithLoaderAndErrors`,
   `launchWithOptions`, `onData`) so exceptions are routed. A raw `launch { }` is
   **not** caught and will crash the app.
2. **`CancellationException` is never an error.** It is used internally (and by
   `cancelOnData()`) to stop work cleanly, so every layer re-throws / ignores it.

---

## 2. The app-wide handler — `SKComponent.errorTreatment`

Configure **once**, typically at app startup, the single function that decides
what happens when any component reports an error:

```kotlin
SKComponent.errorTreatment = { component, exception, errorMessage ->
    SKLog.e(exception, "Error in ${component::class.simpleName}")
    component.displayMessageError(errorMessage ?: defaultMessageFor(exception))
}
```

- `component` — the component that raised the error (use it to show a message,
  navigate, etc.).
- `exception` — the original exception (inspect its type to react to network vs
  other failures).
- `errorMessage` — the optional message passed by the call site (see below).

> ⚠️ If you never set `errorTreatment` **and** never override `treatError`,
> calling `treatError` throws `IllegalStateException`. Set the handler early.

### Per-component override

For local behaviour, override `treatError` on a specific component instead of (or
in addition to) the global handler:

```kotlin
override fun treatError(exception: Exception, errorMessage: String?) {
    if (exception is MyDomainException) {
        view.showInlineError(exception.reason)
    } else {
        super.treatError(exception, errorMessage)   // fall back to the app handler
    }
}
```

---

## 3. Launching work with error routing

| Helper | Catches errors? | Loader? | Use it for |
|---|---|---|---|
| `launchWithLoaderAndErrors { }` | ✅ → `treatError` | ✅ | The default: any user-triggered action. |
| `launchWithOptions(...) { }` | ✅ (configurable) | optional | Fine-grained control. |
| `launchNoCrash { }` | logs only | ❌ | Fire-and-forget background work. |
| `launch { }` | ❌ **crashes** | ❌ | Avoid unless you `try/catch` yourself. |

### `launchWithLoaderAndErrors`

```kotlin
fun save() {
    launchWithLoaderAndErrors(errorMessage = "Could not save your changes") {
        userModel.save(form)   // any exception here → loader stops + treatError
        finish()
    }
}
```

`errorMessage` is forwarded to `treatError` (and thus to your global handler) so
the same call site can provide a user-friendly message.

### `launchWithOptions` — full control

```kotlin
launchWithOptions(
    withLoader = true,
    errorMessage = "Could not load",
    specificErrorTreatment = { ex ->        // overrides treatError for THIS call
        if (ex is TimeoutException) view.showRetry() else treatError(ex, "Oops")
    },
) {
    // suspend work
}
```

Behaviour: start loader → run block → on a non-`CancellationException`
exception call `specificErrorTreatment` (if given) else `treatError(ex, errorMessage)`
→ **always** stop the loader in `finally`.

### `launchNoCrash`

```kotlin
launchNoCrash {
    analytics.track(event)   // failures are only logged, never surfaced or crash
}
```

Backed by a `CoroutineExceptionHandler` that just logs. Use for non-critical work
where an error should be silent.

---

## 4. Errors while observing data — `onData`

When binding [`SKData`](components/skcomponent.md#5-reacting-to-data--skdata),
error handling is built into the call:

```kotlin
userModel.profile(id).onData(
    treatErrors = true,                  // route load errors to treatError (default)
    defaultErrorMessage = "Profile unavailable",
    fallBackDataIfError = true,          // show cached/stale data if the load fails
    fallBackDataBeforeFirstDataLoaded = true,  // show stale data while loading
) { profile ->
    view.render(profile)
}
```

- `treatErrors` (default `true`) — failures of the initial `get(...)` go to
  `treatError`. Set `false` to swallow them silently (e.g. you only care about the
  fallback).
- `defaultErrorMessage` — message passed to `treatError`.
- `fallBackDataIfError` — on error, emit `fallBackValue()` (typically the cached
  value) so the UI isn't empty.
- Errors thrown later, while **collecting** the flow, are logged (not sent to
  `treatError`) — keep your `map`/transform blocks defensive.

### Aborting cleanly — `cancelOnData`

Inside an `onData`/`onChange` block, call `cancelOnData("reason")` to stop the
current treatment. It throws a `CancellationException`, which is swallowed safely
and **never** treated as an error.

```kotlin
items.onData { list ->
    if (list.isEmpty()) cancelOnData("nothing to show")
    view.items = list.map(::row)
}
```

---

## 5. The Model layer is resilient by design

The provided `SKData` implementations already isolate non-fatal failures so they
never reach your component:

- `SKDistantDataWithCache` — **cache read/write errors are caught and logged
  only**; they never break a fetch. Only the network `fetchData()` failure
  propagates (so it can be treated or fall back to cache).
- `fallBackValue()` returns the cached/last value and powers the `fallBack*`
  options of `onData`.

This means a typical screen shows cached data instantly, attempts a refresh, and
only reports an error if the refresh fails *and* there is nothing to fall back to.

---

## 6. Logging

Always log with [`SKLog`](components/skcomponent.md#11-logging) (or the component
helpers `logD` / `logE`) rather than `println`, so logging is centralized and
routed by your `SKLogger`:

```kotlin
logE(exception, "save failed")   // "MyComponent -- save failed"
```

---

## 7. Form/input validation (not exceptions)

Input validation is a separate concern from exception handling. `SKInput` carries
its own validity state (`SKInput.Validity.Error(message)`) for inline field
errors — use that for "this email is invalid", and keep `treatError` for actual
exceptions. See [SKInput](components/framework_components/skinput.md).

---

## Checklist

- [ ] Set `SKComponent.errorTreatment` once at startup.
- [ ] Launch user actions via `launchWithLoaderAndErrors` (never raw `launch`).
- [ ] Pass a meaningful `errorMessage` at the call site.
- [ ] Use `onData(fallBackDataIfError = true)` for resilient screens.
- [ ] Use `launchNoCrash` for silent background work.
- [ ] Never treat `CancellationException` as an error; use `cancelOnData` to abort.

## See also

- [SKComponent in-depth guide](components/skcomponent.md) — launchers, `onData`, loaders
- [SKScreen](components/skscreen.md)
- [Annotations reference](annotations.md)