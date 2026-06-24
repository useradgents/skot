# SKInputWithSuggestions

A **free-text input with a suggestions dropdown**. Unlike [SKCombo](skcombo.md) —
where the user picks from a fixed list — `SKInputWithSuggestions` lets the user
type freely and you react to the text (typically to query/filter suggestions),
updating the proposed `choices` dynamically.

Both share the same base (`SKBaseCombo`), so the entity-mapping options
(`label`, `inputText`, `textColor`, `striked`, `onSelected`) are identical.

## Usage

In your `ScreenVC`, declare a `SKInputWithSuggestionsVC`:

```kotlin
interface SearchScreenVC : SKScreenVC {
    val city: SKInputWithSuggestionsVC
}
```

Launch the generation with the `skGenerate` gradle task.

In your screen, instantiate the component. `onInputText` is **required** — it is
called on every keystroke so you can fetch/filter suggestions:

```kotlin
override val city = SKInputWithSuggestions<City>(
    hint = strings.search_city_hint,
    label = { it.name },
    onSelected = { selectCity(it) },
    onInputText = { typed ->
        launchNoCrash {
            city.choices = cityModel.suggest(typed.orEmpty())  // update suggestions
        }
    },
)
```

In your xml, include the suggestions input layout with id `city`.

## Updating suggestions at runtime

Assign `choices` (inherited from `SKBaseCombo`) whenever new suggestions are
available — the dropdown updates automatically:

```kotlin
city.choices = results          // list of D
city.value = preselectedCity    // programmatically set the selection
```

## Using a complex entity

Like `SKCombo`, the generic `D` can be any type; the lambdas map it to display:

```kotlin
SKInputWithSuggestions<AirportEntity>(
    label = { "${it.city} (${it.iata})" },
    inputText = { it.iata },         // what fills the field once selected
    onSelected = { /* … */ },
    onInputText = { /* query … */ },
)
```

## Overridable hooks

| hook | when |
|--|--|
| `onFocus()` | The field gains focus. |
| `onFocusLost()` | The field loses focus. |

## Properties & parameters

| name | type | description |
|--|--|--|
| `hint` | `String?` | Placeholder shown before input. |
| `errorInitial` | `String?` | Initial error text. |
| `initialChoices` | `List<D>` | Initial suggestions. Default empty. |
| `enabled` | `Boolean` | Enabled state. Default `true`. |
| `onInputText` | `(String?) -> Unit` | **Required.** Called on every text change. |
| `onSelected` | `((D) -> Unit)?` | Called when a suggestion is chosen. |
| `label` | `((D) -> String)?` | Text shown in the dropdown (defaults to `toString`). |
| `inputText` | `((D) -> String)?` | Text put in the field once selected. |
| `textColor` | `((D) -> Color)?` | Per-item text color. |
| `striked` | `((D) -> Boolean)?` | Strike-through an item. |
| `oldSchoolModeHint` | `Boolean` | Legacy hint rendering. Default `false`. |
| `choices` | `List<D>` | Runtime suggestions (settable). |
| `value` | `D?` | Currently selected value (settable). |

## See also

- [SKCombo](skcombo.md) — pick from a fixed list
- [SKInput](skinput.md) — plain text input with validation
- [SKComponent in-depth guide](../skcomponent.md)