# SKData & the Model layer — in-depth guide

`SKData<D>` is SKot's **observable, time-aware data primitive**. The Model layer
exposes data to ViewModels exclusively as `SKData`, and components consume it with
[`onData` / `onChange`](components/skcomponent.md#5-reacting-to-data--skdata).

This guide covers the `SKData` contract, its caching/validity semantics, every
provided implementation, the combinators (`map`, `combine`, `wrap`), and how it
all ties into [BusinessModels](architecture/businessmodel.md) and
[SKState](start/addstate.md).

> For the conceptual layering (Model / ModelContract / BusinessModel), see the
> [architecture docs](architecture/readme.md). This page is about the data types
> themselves.

---

## 1. The `SKData<D>` contract

```kotlin
interface SKData<D : Any?> {
    val flow: Flow<DatedData<D>?>     // emits on every change (null = no value yet)
    val defaultValidity: Long         // how long a value stays "fresh" (ms)
    val _current: DatedData<D>?       // current value + timestamp, if any

    suspend fun update(): D           // force a refresh
    suspend fun fallBackValue(): D?   // best available value without refreshing
    suspend fun get(validity: Long? = null): D
    suspend fun getDirect(): D        // _current if present, else get(MAX_VALUE)
}
```

Every value is wrapped in a **`DatedData`** — the data plus the timestamp it was
produced:

```kotlin
data class DatedData<D>(val data: D, val timestamp: Long = currentTimeMillis())
```

### Validity & the `get` semantics

`get(validity)` is the heart of the cache logic:

```
if (_current == null
    || validity == 0L
    || now > _current.timestamp + validity)   ->  update()   // stale → refresh
else                                          ->  _current.data   // fresh → reuse
```

- `validity = null` → uses `defaultValidity`.
- `validity = 0L` → **always** refresh.
- `validity = Long.MAX_VALUE` → never expires (use the cached value if any).

So a screen typically calls `onData(validity = …)`; SKot returns the cached value
instantly when it is still fresh, and refreshes only when needed.

---

## 2. Choosing an implementation

| You need… | Use | Cached to disk? | Mutable from code |
|---|---|---|---|
| Remote data, refreshed on demand | `SKDistantData` | no | no |
| Remote data + offline cache | `SKDistantDataWithCache` | ✅ | no |
| Remote data whose key changes at runtime | `SKDistantDataWithCacheAndLiveKey` | ✅ | no |
| Paginated remote list | `SKPaginatedDataWithCache` | ✅ | via page ops |
| In-memory app state | `SKManualData` | no | `value` |
| Persisted app state | `SKManualDataWithCache` | ✅ | `setValue` |
| Any of the above, but nullable | the `Nullable…` variant | — | — |

In practice you rarely instantiate these directly — you declare them through the
[`SKBM` factories](#6-declaring-data-in-a-businessmodel).

---

## 3. Remote data

### `SKDistantData`

The base for fetched data. `defaultValidity` is **5 minutes**. `update()` is
guarded by a mutex and de-duplicates concurrent refreshes.

```kotlin
SKDistantData(validity = 60_000) { api.getUser(id) }
```

### `SKDistantDataWithCache`

Adds a persistent cache (an [`SKPersistor`](#7-the-cache-skpersistor), SQLDelight
by default). It keys data by `name` + `key`, serializes with a `KSerializer`, and:

- loads the cached value on first access (`initWithCache`) so the UI has something
  immediately;
- writes through to the cache on every new value (off the main thread);
- **cache read/write failures are logged only** — they never break a fetch
  (see [Error handling](error-handling.md#5-the-model-layer-is-resilient-by-design)).

```kotlin
class UserBM : SKBM(key = null) {
    val user = DistantData("user", User.serializer(), validity = 60_000) {
        api.getUser()
    }
}
```

### `SKDistantDataWithCacheAndLiveKey`

Like the cached version, but the cache key is **recomputed at runtime** via
`liveKey()`. When the live key changes, the next `get` refreshes, and
`fallBackValue()` returns `null` if the cached value belongs to a different key.
Use it when the same `SKData` instance serves different keys over time (e.g. the
"current selected item").

### `SKPaginatedDataWithCache`

A cached `SKData<List<D>>` with pagination:

| Member | Role |
|---|---|
| `oneMorePage()` | Fetch and append the next page. |
| `mayHaveAnotherPage` | `false` once a short/empty page came back. |
| `invalidateFrom(item)` | Drop everything from the page containing `item` onward. |
| `replace(old, new)` | Swap an item in place. |

Implements `SKPaginatedData<D>`. The first page is fetched as the normal value;
subsequent pages extend the current list.

---

## 4. Manual data (app state)

### `SKManualData` (in-memory)

A simple, settable, observable value. `defaultValidity` is `Long.MAX_VALUE` (never
stale). Supports change listeners and Kotlin property delegation.

```kotlin
val isDarkMode = SKManualData(false)
isDarkMode.value = true                 // emits to observers
var theme by SKManualData("light")      // delegate form
isDarkMode.onChange { applyTheme(it) }
```

### `SKManualDataWithCache` (persisted)

Same idea, but the value survives restarts via the cache. Set it with
`setValue(...)` (which writes through to the cache); it initializes from cache or
from `initialDefaultValue`.

```kotlin
class PrefsBM : SKBM(key = null) {
    val lastSeenTab = ManualData("lastTab", Int.serializer(), initialDefaultValue = 0)
}
prefsBM.lastSeenTab.setValue(2)
```

---

## 5. Combinators

`SKData` composes, so the Model can derive new reactive values without manual
wiring.

### `map`

Transform the value (lazily, with `_current` tracking so it stays consistent):

```kotlin
val userName: SKData<String> = userBM.user.map { it.name }
```

> `mapSuspend` is deprecated — `map` already accepts a `suspend` transform.

### `combine` / `combineSKData`

Combine 2 or 3 `SKData`s into a `Pair`/`Triple`. `get`/`update` run the sources
**in parallel**; `defaultValidity` is the minimum of the sources.

```kotlin
val header = userBM.user.combine(cartBM.count)        // SKData<Pair<User, Int>>
val triple = combineSKData(a, b, c)                   // SKData<Triple<…>>
```

### `wrap` — dynamic switching

`SKDataWrapper` follows a *source* `SKData` and re-subscribes to whichever target
`SKData` the source currently points to. Use it when the data you want depends on
another reactive value (e.g. "the orders of the currently selected account").

```kotlin
// in a CoroutineScope-aware model:
val currentOrders = selectedAccount.wrap(scope, defaultValue = emptyList()) {
    ordersBM.ordersOf(this.id)   // returns an SKData<List<Order>>
}
```

It exposes a `cancel()` to stop following (mainly for tests / manual scope
control).

### `getWithFallBackIfError`

A convenience `get` that returns the fallback value if the refresh throws:

```kotlin
val value = mySKData.getWithFallBackIfError()   // get(), or fallBackValue() on error
```

---

## 6. Declaring data in a BusinessModel

`SKBM` is the base class for [BusinessModels](architecture/businessmodel.md). It
exposes inner-class factories so you declare data with the right cache key in one
line:

| Factory | Produces |
|---|---|
| `DistantData(name, serializer, validity?, fetchData)` | `SKDistantDataWithCache` |
| `NullableDistantData(...)` | nullable distant data |
| `DistantDataWithLiveKey(..., liveKey, fetchData)` | live-key distant data |
| `PaginatedData(..., nbItemsInPage, fetchPage)` | paginated data |
| `ManualData(name, serializer, initialDefaultValue)` | `SKManualDataWithCache` |
| `NullableManualData(...)` | nullable manual data |

```kotlin
class AccountBM(key: String?) : SKBM(key) {
    val profile = DistantData("profile", Profile.serializer()) { api.profile() }
    val avatar  = NullableManualData("avatar", String.serializer(), initialDefaultValue = null)
}
```

The `key` passed to `SKBM` is propagated to every data it creates, so the same BM
attached to different [states](start/addstate.md) caches under distinct keys.

---

## 7. The cache (`SKPersistor`)

Persistence is handled by `SKPersistor` (SQLDelight-backed `globalPersistor` by
default). You normally don't touch it — the cached `SKData` types use it
transparently. It stores serialized values keyed by `name` + `key` with a
timestamp, which is what powers `DatedData` validity across restarts.

---

## 8. SKState — scoping data to app states

`SKData`/`SKBM` are usually reached through **states**. A `SKStateDef` describes a
state of the app (e.g. *connected*, *in checkout*); BusinessModels are attached to
a state via [`@SKBms`](annotations.md), and a state's data is only reachable while
that state exists. States form a hierarchy under a `rootState`.

This is its own topic — see **[SKState](start/addstate.md)** for the full
walkthrough, and [`@SKBms` / `@SKCompositeStateParts`](annotations.md) for the
annotations involved.

---

## 9. Consuming `SKData` from a component

From the ViewModel side you never poll — you observe:

```kotlin
init {
    model.profile.onData(validity = 60_000) { view.render(it) }
    model.isDarkMode.onChange { view.applyTheme(it) }
}
```

See [SKComponent — reacting to data](components/skcomponent.md#5-reacting-to-data--skdata)
for the full `onData` options (loaders, fallbacks, error routing).

---

## See also

- [SKComponent in-depth guide](components/skcomponent.md)
- [Error handling](error-handling.md)
- [BusinessModel](architecture/businessmodel.md) · [Model](architecture/model.md) · [ModelContract](architecture/modelcontract.md)
- [SKState](start/addstate.md)
- [Annotations reference](annotations.md)
