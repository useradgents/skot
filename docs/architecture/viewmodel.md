# ViewModel Module

The `viewmodel` module holds the **business logic** of each component. A ViewModel
interacts with its View through the [ViewContract](viewcontract.md) and, when one
exists, with its Model through the [ModelContract](modelcontract.md). Its job is to
give the view what it needs to display — dispatching data to the sub-components it
owns — using data from its constructor or from the associated Model.

Every ViewModel is an [`SKComponent`](../components/skcomponent.md) (or
[`SKScreen`](../components/skscreen.md)); read that guide for the full runtime
behaviour (coroutines, `onData`, loaders, errors, lifecycle).

## Anatomy of a ViewModel

A concrete ViewModel extends the generated `*Gen` parent and provides its `view`:

```kotlin
class MyComponent(title: String) : MyComponentGen() {
    val button = SKButton()
    val myOther = MyOtherComponent()

    override val view: MyComponentVC = viewInjector.myComponent(
        visibilityListener = this,
        button = button.view,
        myOther = myOther.view,
        title = title,                       // a `val` of the VC
        subtitleInitial = model.getSubtitle() // initial value for a `var subtitle`
    )
}
```

Notice the conventions wired by the generator:

- **`val` contract members** are passed by name (`title = …`).
- **`var` contract members** get an `Initial` parameter (`subtitleInitial = …`);
  afterwards you update them with `view.subtitle = …`.
- **sub-components** are passed as their `.view` (`button = button.view`).
- **`visibilityListener = this`** binds the screen lifecycle.

## Accessing the Model

If a [`*MC`](modelcontract.md) exists, `*Gen` exposes a ready `model`:

```kotlin
init {
    model.subtitle.onData { view.subtitle = it }   // observe model data
}

fun onValidate() {
    launchWithLoaderAndErrors { model.doClickAction() }
}
```

States the Model needs are passed to the ViewModel constructor (and on to the
generated `model` factory) — see [SKState](../start/addstate.md).

## Where logic goes

- Observe Model data and push it into the view (`onData`/`onChange`).
- Handle view callbacks (button taps, input changes) and call Model actions.
- Own and coordinate sub-components (create them, wire their callbacks, remove them
  — the generated `*Gen` cascades `onRemove()` to declared sub-components).

Keep network/persistence out of the ViewModel — that belongs to the
[Model](model.md) / [BusinessModels](businessmodel.md).

## See also

- [SKComponent in-depth guide](../components/skcomponent.md) · [SKScreen](../components/skscreen.md)
- [ViewContract](viewcontract.md) · [ModelContract](modelcontract.md) · [View](view.md)
- [Error handling](../error-handling.md) · [SKData](../skdata.md)