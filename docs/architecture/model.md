# Model

The `model` module is the **data layer**. It implements the
[ModelContracts](modelcontract.md) (`*MC`), holds the
[BusinessModels](businessmodel.md), and contains all network/persistence code. A
Model's job is to fetch data, compile it, and expose it to the ViewModel as
[`SKData`](../skdata.md) — and to receive what the user entered.

Often a Model does little itself: it just wires data between the
[ViewModel](viewmodel.md) and the [BusinessModels](businessmodel.md).

## The generated skeleton

When a `<Name>MC` exists and no implementation is present, `skGenerate` creates a
skeleton `<Name>Model`:

```kotlin
class MyScreenModel(
    override val coroutineContext: CoroutineContext,
    // one constructor param per state the MC declares:
    override val connectedState: ConnectedStateModel,
) : MyScreenMC, CoroutineScope {

    override val subtitle: SKData<String>      // implement the MC members
    override suspend fun doClickAction() { /* … */ }
}
```

Key points:

- It implements the `*MC` **and** `CoroutineScope`, receiving a `coroutineContext`
  (so it can launch coroutines tied to the component's lifecycle).
- Each [state](../start/addstate.md) declared in the `MC` is injected as a
  constructor parameter (as the state's *model* type), giving access to that
  state's values, its BusinessModels, and parent states.
- You fill in the member implementations — usually by delegating to a
  BusinessModel.

You never instantiate a Model directly: the generated **`modelInjector`** does it,
and the ViewModel's `*Gen` calls `modelInjector.<name>(coroutineContext, states…)`.

## Exposing data

Expose reactive values as [`SKData`](../skdata.md). Typical patterns:

```kotlin
class ProfileScreenModel(
    override val coroutineContext: CoroutineContext,
    override val connectedState: ConnectedStateModel,
) : ProfileScreenMC, CoroutineScope {

    // pass-through from a BusinessModel:
    override val profile = connectedState.accountBM.profile

    // derived data with map (see SKData guide):
    override val displayName = profile.map { "${it.firstName} ${it.lastName}" }

    // an action delegating to the BM:
    override suspend fun refresh() { connectedState.accountBM.profile.update() }
}
```

- Prefer returning a BusinessModel's `SKData` (or a [`map`/`combine`](../skdata.md#5-combinators)
  of them) so caching and refresh logic stay in one place.
- Use `SKManualData` for simple local state the Model owns.

## Where things live

| Concern | Where |
|---|---|
| ViewModel↔Model contract | `modelcontract` (`*MC`) — [doc](modelcontract.md) |
| Model implementation | `model` (`*Model`) — this module |
| Business logic, network calls, `SKData` declarations | [BusinessModels](businessmodel.md) (`SKBM`) |
| App states & their hierarchy | [SKState](../start/addstate.md) |
| Persistence / cache | `SKPersistor` (transparent, [see SKData](../skdata.md#7-the-cache-skpersistor)) |

## Error propagation

Models throw ordinary exceptions (network, parsing, etc.); they surface in the
ViewModel through the launch helpers / `onData`. See
[Error handling](../error-handling.md).

## See also

- [ModelContract](modelcontract.md)
- [BusinessModel](businessmodel.md)
- [SKData & the Model layer](../skdata.md)
- [SKState](../start/addstate.md)
- [Error handling](../error-handling.md)
