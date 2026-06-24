# ViewContract Module

The `viewcontract` module holds the interfaces (`*VC`) that define how a **View
talks to its ViewModel**. It is pure Kotlin (no Android), which is what makes the
ViewModel testable and the View replaceable.

Every component has an interface named after it suffixed with **`VC`**, inheriting
from `SKComponentVC` or `SKScreenVC`. It is the **first file you write** when
creating a component — see [Create a component](../start/createcomponent.md).

## Member kinds

```kotlin
interface MyScreenVC : SKScreenVC {
    val title: String              // value  — set once at creation
    var subtitle: String?          // variable — mutable, reactive
    val button: SKButtonVC         // sub-component
    val myOther: MyOtherComponentVC
    fun scrollToTop()              // function — one-shot action
}
```

| Kind | Direction | Semantics |
|---|---|---|
| `val x: T` (value) | VM → View | Set once, at creation. Pushed to the view a single time. |
| `var x: T` (variable) | VM → View | Mutable & **reactive** — the view is updated on every change. |
| `fun f(…)` (function) | VM → View | A one-shot action. **Not replayed** if no view is attached at call time, and **cannot return** a value — use a callback parameter. |
| `val c: SomeVC` (sub-component) | — | A child component; the ViewModel provides its `view`. |

> ⚠️ This `val` vs `var` vs `fun` distinction is the core of the contract. Choose
> `var` for anything that changes over the screen's life, `val` for fixed
> creation data, and `fun` for fire-and-forget actions.

## Declaring sub-components

App components/screens used by this contract must be known to the generator via
[`@SKUses` / `@SKOpens`](../annotations.md) (framework components like `SKButtonVC`
are auto-known). See
[SKComponent — declaring sub-components](../components/skcomponent.md#declaring-sub-components-skuses-and-skopens).

## SKComponentVC vs SKScreenVC

`SKScreenVC` only adds navigation-related members (`onBackPressed`,
`statusBarColor`, `exit`) on top of `SKComponentVC`; otherwise a screen behaves
exactly like a component. See [SKScreen](../components/skscreen.md).

## What the generator produces

Running `skGenerate` turns a `*VC` into:

- `*Gen` — the abstract ViewModel parent (in `viewmodel`).
- `*ViewProxy` + `*View` + the XML layout (in `view`) — see [View](view.md).
- a `viewInjector` factory used to create the `view`.

## See also

- [ViewModel Module](viewmodel.md) · [View](view.md)
- [SKComponent in-depth guide](../components/skcomponent.md)
- [Annotations reference](../annotations.md)
- [Create a component](../start/createcomponent.md) · [Create a screen](../start/createscreen.md)