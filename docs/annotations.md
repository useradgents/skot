# SKot annotations reference

SKot uses annotations on your **contracts** (`*VC` ViewContracts, `*StateDef` /
model contracts) to drive code generation (`./gradlew skGenerate`). They are
*declarative hints* read by the generator — they never contain runtime logic.

This page lists every annotation, what it does, and how to use it.

- View annotations live in `tech.skot.core.components` (module `viewcontract`).
- Model/state annotations live in `tech.skot.model` (module `modelcontract`).

> Reminder: annotations only ever reference **your own app components/screens**.
> Framework components (`SKLoaderVC`, `SKListVC`, `SKButtonVC`, …) are already
> known to the generator and must never be listed.

---

## Quick reference

| Annotation | Target | Module | Purpose |
|---|---|---|---|
| `@SKUses` | class | viewcontract | Declare app components used by this component so they get generated. |
| `@SKOpens` | class | viewcontract | Declare app screens this screen can navigate to (`push`). |
| `@SKNoVM` | class | viewcontract | Skip generation of the ViewModel tester/test for this component. |
| `@SKGenerics` | class | viewcontract | Mark the component's ViewModel as generic (type-parameterized). |
| `@SKActivityClass` | class | viewcontract | Set the hosting Activity class for a screen. |
| `@SKUIState` | nested class | viewcontract | Mark a nested class as the component's saved UI state. |
| `@SKLayoutNo` | class | viewcontract | The component has no XML layout. |
| `@SKLayoutIsRoot` | class | viewcontract | The component's binding is the layout root. |
| `@SKLayoutIsSimpleView` | class | viewcontract | The layout is a single simple view (`<include>`-aware). |
| `@SKLegacyViewIncluded` | class | viewcontract | The view is pulled in via an `<include>` in the parent layout. |
| `@SKPassToParentView` | property | viewcontract | Forward this property to the parent view. |
| `@SKBms` | class | modelcontract | Declare the BusinessModels (`SKBM`) a state is built from. |
| `@SKBySKData` | property | modelcontract | The state property is backed by an `SKData`. |
| `@SKWithDefault` | property | modelcontract | Provide a default initialization expression for a property. |
| `@SKCompositeStateParts` | property | modelcontract | Declare which sub-states compose a composite sub-state. |

---

## View & component annotations (`viewcontract`)

### `@SKUses([XxxVC::class])`

**Target:** the `*VC` interface. Declares the **app components** this component
uses so the generator builds them and wires their `onRemove()` into the teardown
cascade.

The generator already auto-detects any component you declare as a typed property
*in your app package*. Use `@SKUses` for app components that are **not** exposed
as a direct typed property (created in code, used only inside functions, etc.).

```kotlin
@SKUses([CartBadgeVC::class])
interface HomeScreenVC : SKScreenVC {
    val header: HeaderVC            // auto-detected, no need to list it
    fun showCartBadge(count: Int)   // CartBadge used in code → must be in @SKUses
}
```

> Never list framework components here. See [SKComponent](components/skcomponent.md)
> for how sub-components and the `onRemove()` cascade work.

### `@SKOpens([XxxScreenVC::class])`

**Target:** the `*VC` interface (a screen). Declares the **app screens** this
screen can navigate to via `push`, guaranteeing they are generated and reachable
even when not held as a property.

```kotlin
@SKOpens([ProductScreenVC::class, CheckoutScreenVC::class])
interface CatalogScreenVC : SKScreenVC
```

### `@SKNoVM`

**Target:** the `*VC` interface. Tells the generator **not** to generate the
ViewModel tester / test scaffolding for this component. Use it for components
that don't need generated unit-test plumbing.

```kotlin
@SKNoVM
interface DecorationVC : SKComponentVC
```

### `@SKGenerics`

**Target:** the `*VC` interface. Marks the component's ViewModel as **generic**
(it has a type parameter); the generated tester references it with a star
projection.

```kotlin
@SKGenerics
interface SelectableItemVC<T> : SKComponentVC
```

### `@SKActivityClass("com.example.MyActivity")`

**Target:** a screen `*VC`. Specifies the fully-qualified **Activity class** that
should host this screen, instead of the default host.

```kotlin
@SKActivityClass("com.example.app.CustomActivity")
interface PaymentScreenVC : SKScreenVC
```

### `@SKUIState`

**Target:** a **nested class** inside a `*VC`. Marks it as the component's
**saved UI state** holder — the generator uses it as the component's `state` for
save/restore across recreation.

```kotlin
interface SearchScreenVC : SKScreenVC {
    @SKUIState
    data class State(val query: String, val scroll: Int)
}
```

### Layout-binding annotations

These control how the generated Android view binds to its XML layout. They are
mutually exclusive descriptions of the component's layout shape.

| Annotation | Effect on the generated binding |
|---|---|
| `@SKLayoutNo` | No layout for this component (binding resolves to `Unit`). |
| `@SKLayoutIsRoot` | The component's view *is* the root (`binding.root`). |
| `@SKLayoutIsSimpleView` | A single simple view; resolved as an `<include>` root when the tag is an include. |
| `@SKLegacyViewIncluded` | The view is brought in through an `<include name>` in the parent layout (`binding.<name>.root`). |

```kotlin
@SKLayoutNo
interface InvisibleControllerVC : SKComponentVC

@SKLayoutIsRoot
interface FullBleedScreenVC : SKScreenVC
```

### `@SKPassToParentView`

**Target:** a **property** of a `*VC`. Marks the property so it is **forwarded to
the parent view** during generation, rather than handled locally.

```kotlin
interface RowVC : SKComponentVC {
    @SKPassToParentView
    val backgroundColor: Color?
}
```

---

## Model & state annotations (`modelcontract`)

These drive generation of the data/state layer (`SKData`, states, business
models). See the model layer docs for the broader picture.

### `@SKBms(["UserBM", "CartBM"])`

**Target:** a state contract class. Declares the **BusinessModels** (`SKBM`) the
state is composed from / depends on. Plain names resolve under
`<appPackage>.model.business`; use a fully-qualified name (with dots) for others.

```kotlin
@SKBms(["UserBM", "com.example.app.model.business.SessionBM"])
interface ProfileStateDef : SKStateDef
```

### `@SKBySKData`

**Target:** a **state property**. Marks that the property is **backed by an
`SKData`** (reactive/observable) rather than a plain value.

```kotlin
interface DashboardStateDef : SKStateDef {
    @SKBySKData
    val notifications: List<Notification>
}
```

### `@SKWithDefault("emptyList()")`

**Target:** a **state property**. Provides the **default initialization
expression** used for the property in generated code.

```kotlin
interface FiltersStateDef : SKStateDef {
    @SKWithDefault("emptyList()")
    val selectedTags: List<String>
}
```

### `@SKCompositeStateParts(["partA", "partB"])`

**Target:** a **composite sub-state property**. A composite sub-state is built
from other sub-states; this annotation declares **which sub-states compose it**.
It is **required** for composite sub-states — the generator fails without it. The
listed names must match existing sub-state property names.

```kotlin
interface ScreenStateDef : SKStateDef {
    val userPart: UserStateDef?
    val cartPart: CartStateDef?

    @SKCompositeStateParts(["userPart", "cartPart"])
    val summary: SummaryStateDef?
}
```

---

## See also

- [SKComponent in-depth guide](components/skcomponent.md) — `@SKUses` / `@SKOpens`
  in context, and the `onRemove()` cascade.
- [Create a component](start/createcomponent.md)
- [Create a screen](start/createscreen.md)