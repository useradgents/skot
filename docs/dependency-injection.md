# Dependency Injection

SKot ships a small, explicit DI container — no annotations, no reflection-heavy
framework. You declare bindings in **modules**, they are loaded into a single
**injector** at startup, and you resolve dependencies with `get()` /
`getByName()`.

It is the mechanism behind the generated `viewInjector`, `modelInjector`,
`coreViewInjector` and the resource accessors (`strings`, `colors`, …).

---

## 1. Resolving dependencies

Two global helpers, available everywhere:

```kotlin
val api: Api = get()                       // by type
val style: Style = getByName("skFullScreenDialogStyle")  // by name
```

- `get<T>()` — resolve a binding by type. Throws `IllegalStateException` if no
  binding is registered for `T`.
- `getByName<T>(key)` — resolve a named binding. Throws if the key is unknown.

Both delegate to the global `injector`. Prefer `get<T>()` (type-safe);
`getByName` exists for cases like theming where the type alone isn't enough.

---

## 2. Declaring bindings — `Module`

Build modules with the `module { }` DSL:

```kotlin
val appModule = module {
    single { Api(get()) }            // one shared instance (lazy singleton)
    single<Clock> { SystemClock() }  // bind an interface to an impl
    factory { RequestBuilder() }     // a new instance on every get()
}
```

| Declaration | Lifetime |
|---|---|
| `single { … }` | Created once on first `get()`, then cached and reused. |
| `factory { … }` | A fresh instance is built on **every** `get()`. |

Inside a binding lambda you can call `get()` to resolve other dependencies, and —
on Android — access `androidApplication` (the lambda receiver is the injector,
whose context is the `BaseInjector`). `androidApplication` is an extension from `coreAndroid`, so
add `import tech.skot.core.di.androidApplication`.

Named bindings are set directly on the module's `byName` map:

```kotlin
val themeModule = module<BaseInjector> { }.apply {
    byName["skFullScreenDialogStyle"] = myFullScreenStyle
}
```

---

## 3. The injector & startup

There is **one** global injector, created in your `Application.onCreate` and
assigned to the global `injector`:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        injector = BaseInjector(
            this,
            generatedAppModules + listOf(
                appModule,
                themeModule,
            ),
        )
    }
}
```

- `BaseInjector(application, modules)` — the Android implementation; its `context`
  is the injector itself, so bindings reach `androidApplication` (with
  `import tech.skot.core.di.androidApplication`).
- `generatedAppModules` — generated for you; it binds the `ViewInjector` /
  `ModelInjector` implementations and the resource accessors. **Append your own
  modules** to the `listOf(...)`.
- `injector.loadModules(extraModules)` — register more modules later (e.g. when a
  feature module starts).

---

## 4. What SKot already injects for you

After `skGenerate`, a `shortCuts.kt` exposes ready-to-use accessors resolved via
`get()`:

```kotlin
val viewInjector: ViewInjector = get()     // builds the *View proxies
val modelInjector: ModelInjector = get()   // builds the *Model implementations
val strings: Strings = get()
val colors: Colors = get()
// … plurals, icons, fonts, styles, dimens, transitions, permissions
```

- `viewInjector` / `modelInjector` are used by generated ViewModels
  (`viewInjector.myScreen(...)`, `modelInjector.myScreen(...)`).
- `coreViewInjector` (lazy `get()`) is used internally by framework components
  (`SKLoader`, `SKList`, `SKDialog`, …).

You normally consume these accessors; the bindings behind them live in
`generatedAppModules`.

---

## 5. Where to use it

- **Models / BusinessModels** — inject your `Api`, database, clock, etc. with
  `get()`.
- **App wiring** — bind interfaces to implementations in a module so the rest of
  the code depends only on the interface.

> Design note (from the framework source): named injection (`getByName`) is
> considered a last resort — prefer typed bindings and dedicated interfaces over
> string keys, to keep application logic out of the DI configuration.

---

## See also

- [Model](architecture/model.md) / [BusinessModel](architecture/businessmodel.md)
- [Architecture overview](architecture/readme.md)
- [SKComponent in-depth guide](components/skcomponent.md)