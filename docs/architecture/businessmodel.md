# BusinessModel

BusinessModels (`SKBM`) carry the **majority of the application's logic**. They
call web services and own most of the [`SKData`](../skdata.md) used in the app.

As everywhere in SKot there is an interface and its implementation, to keep an
abstraction — this ensures `SKData` is created/mutated only inside the
BusinessModel, limiting misuse.

A BusinessModel is best seen as a **set of use cases around one domain** — e.g. an
`AccountBM` handles everything about the user's account (profile, password,
avatar).

## Linking a BM to a state

A BusinessModel is attached to a [state](../start/addstate.md) via the
[`@SKBms`](../annotations.md) annotation on the state contract. The state is passed
to the BM, giving it access to all the state's values, the other BMs of that state,
and the BMs of parent states.

```kotlin
@SKBms(["AccountBM"])
interface ConnectedStateDef : SKStateDef {
    val userId: String
    var token: Token
}
```

Run `skGenerate`; a BM is reachable only while its state exists.

## The generated structure

For each BM declared in `@SKBms`, `skGenerate` generates an interface and a
skeleton implementation extending [`SKBM`](../skdata.md#6-declaring-data-in-a-businessmodel):

```kotlin
interface AccountBM

class AccountBMImpl(
    key: String?,
    // parent states + own state injected:
    private val connectedState: ConnectedStateModel,
) : SKBM(key), AccountBM {
    // declare data with the SKBM factories:
}
```

The `key` is propagated by `SKBM` to every data the BM creates, so the same BM
attached under different states caches under distinct keys.

## Declaring data

Use the `SKBM` factories (full list in the
[SKData guide](../skdata.md#6-declaring-data-in-a-businessmodel)):

```kotlin
class AccountBMImpl(key: String?, private val connectedState: ConnectedStateModel)
    : SKBM(key), AccountBM {

    val profile = DistantData("profile", Profile.serializer(), validity = 60_000) {
        api.getProfile(connectedState.token)        // fetch
    }

    suspend fun updateName(name: String) {
        api.updateName(name)
        profile.update()                            // refresh the cached data
    }
}
```

- Network calls live here.
- Caching, validity, and refresh are handled by the chosen `SKData` type — see the
  [SKData guide](../skdata.md).

## See also

- [SKData & the Model layer](../skdata.md) — data types and `SKBM` factories
- [Model](model.md) / [ModelContract](modelcontract.md)
- [SKState](../start/addstate.md) — states and `@SKBms`
- [Annotations reference](../annotations.md)