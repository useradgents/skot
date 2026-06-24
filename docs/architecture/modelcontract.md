# ModelContract

The `modelcontract` module holds the interfaces (`*MC`) that define how a
**ViewModel talks to its Model** — the mirror image of how the
[ViewContract](viewcontract.md) defines how the View talks to the ViewModel. It is
pure Kotlin (no Android, no network), so the ViewModel can be unit-tested against
a mocked Model.

## The contract

Any component can have an interface named after it suffixed with **`MC`**. It
declares everything the ViewModel may ask of the Model:

```kotlin
interface MyScreenMC {
    val subtitle: SKData<String>      // reactive data exposed to the ViewModel
    suspend fun doClickAction()       // an action the ViewModel can trigger
}
```

Two kinds of members:

- **`val … : SKData<T>`** — observable data the Model exposes. The ViewModel
  observes it with [`onData` / `onChange`](../components/skcomponent.md#5-reacting-to-data--skdata).
  See the [SKData guide](../skdata.md) for the data types.
- **`suspend fun …`** — actions/queries. Unlike a *ViewContract* function, a
  ModelContract function **may suspend and may return a value** (it runs in the
  ViewModel's coroutine scope). Use return values or `SKData` rather than
  callbacks.

> A component "has a Model" purely by **naming convention**: if a `<Name>MC`
> interface exists for `<Name>VC`, the generator wires a Model in. No annotation
> needed.

## Declaring the states a Model needs

If the Model needs application [state](../start/addstate.md), declare each state as
a property of the `MC`:

```kotlin
interface AccountScreenMC {
    val connectedState: ConnectedStateDef   // this Model requires the "connected" state
    val profile: SKData<Profile>
    suspend fun logout()
}
```

The generator then requires that state to be passed when the screen is created
(see [SKState](../start/addstate.md)).

## What the generator produces

Running `skGenerate` wires the contract into both sides:

- **ViewModel side** — the generated `*Gen` exposes a ready-to-use `model`:
  ```kotlin
  // in MyScreenGen (generated)
  val model: MyScreenMC = modelInjector.myScreen(coroutineContext /*, states… */)
  ```
  So in your ViewModel you just call `model.subtitle`, `model.doClickAction()`.
- **Model side** — a skeleton implementation `<Name>Model` is generated the first
  time (see [Model](model.md)), plus a `modelInjector` factory.

## Example — both sides

ViewModel (module `viewmodel`):

```kotlin
class MyScreen : MyScreenGen() {

    override val button = SKButton(strings.text) {
        launchWithLoaderAndErrors {
            model.doClickAction()
        }
    }

    override val loader = SKLoader()

    override val view: MyScreenVC = viewInjector.myScreen(
        visibilityListener = this,
        subtitleInitial = "",
        button = button.view,
        loader = loader.view,
    )

    init {
        model.subtitle.onData {
            view.subtitle = it
        }
    }
}
```

Model (module `model`):

```kotlin
class MyScreenModel(
    override val coroutineContext: CoroutineContext,
) : MyScreenMC, CoroutineScope {

    override val subtitle = SKManualData("")

    override suspend fun doClickAction() {
        subtitle.value = rootState.myBM.computeNewSubtitle()
    }
}
```

## See also

- [Model](model.md) — writing the implementation
- [BusinessModel](businessmodel.md) — where the real logic lives
- [SKData & the Model layer](../skdata.md) — the data types
- [SKState](../start/addstate.md) — scoping Models/data to app states
- [ViewContract](viewcontract.md) — the View-side counterpart