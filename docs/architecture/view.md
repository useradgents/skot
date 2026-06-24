# View Module

The `view` module is the **Android implementation** of the
[ViewContracts](viewcontract.md). It is the only module that knows about Android
(XML layouts, `View`, `ViewBinding`, `Activity`/`Fragment`). It is split between
**generated** plumbing and the **hand-written** view code where you touch real
Android widgets.

## Two halves: proxy (generated) and view (yours)

For a `MyScreenVC`, `skGenerate` produces:

- **`MyScreenViewProxy`** — implements the contract, holds the UI state, and can
  exist **with no Android view attached**. ViewModel writes go through it.
- **`MyScreenRAI`** — an interface (one `onX` method per contract member) that the
  hand-written view must implement.
- a `ViewBinding` from the XML, and a `viewInjectorImpl` factory.

You write **`MyScreenView`**, extending `SKComponentView<Binding>` (or
`SKScreenView<Binding>`) and implementing `MyScreenRAI` — this is where you push
values into widgets.

## The proxy mechanism (why the ViewModel never waits for a view)

The proxy stores each `var` as an `SKLiveData` and each function/`val` as a
message. When (and only when) a real view is bound, those are delivered. So the
ViewModel can mutate UI state at any time; nothing is lost if the view isn't
on-screen yet. See
[SKComponent — the proxy](../components/skcomponent.md#1-the-big-picture).

Generated `bindTo` (simplified, from a real proxy):

```kotlin
override fun bindTo(activity, fragment, binding): ComposeDemoView =
    ComposeDemoView(this, activity, fragment, binding).apply {
        onOnIncrement(onIncrement)        // function → delivered once
        onTitle(title)                    // val → delivered once
        counterLD.observe { onCounter(it) } // var → delivered on every change
    }
```

## Writing the view

Implement the `*RAI` callbacks to update Android widgets:

```kotlin
class ComposeDemoView(
    override val proxy: ComposeDemoViewProxy,
    activity: SKActivity,
    fragment: Fragment?,
    binding: ComposeDemoBinding,
) : SKScreenView<ComposeDemoBinding>(proxy, activity, fragment, binding),
    ComposeDemoRAI {

    override fun onTitle(title: String) { binding.title.text = title }
    override fun onCounter(counter: String) { binding.counter.text = counter }
    override fun onOnIncrement(onIncrement: () -> Unit) {
        binding.incrementButton.setOnClickListener { onIncrement() }
    }
}
```

### `onX` naming

The generated callback is `on` + the capitalized member name:
`title → onTitle`, `counter → onCounter`, a function `onIncrement → onOnIncrement`.
`val`/`fun` are delivered once at bind; `var` is delivered on every change.

## Layout binding

By default the proxy inflates the XML named after the component (snake_case) via
its generated `ViewBinding`. The [layout annotations](../annotations.md)
(`@SKLayoutNo`, `@SKLayoutIsRoot`, `@SKLayoutIsSimpleView`, `@SKLegacyViewIncluded`)
change how the binding root is resolved (e.g. no layout, root view, `<include>`).

## Lifecycle on the view side

`SKComponentView` provides `onFirstBind()` (one-time setup), `onRecycle()`
(release), `subViews`, observation helpers (`SKLiveData.observe`,
`SKMessage.observe`), permissions and message display. `SKScreenView` adds the
window options (`fullScreen`, `lightStatusBar`, `secured`, insets) and `onResume`/
`onPause` — see [SKScreen — SKScreenView](../components/skscreen.md#skscreenview--presentation-options).

## See also

- [ViewContract](viewcontract.md) · [ViewModel](viewmodel.md)
- [SKComponent in-depth guide](../components/skcomponent.md) · [SKScreen](../components/skscreen.md)
- [Annotations reference](../annotations.md)
- [Create a component](../start/createcomponent.md)