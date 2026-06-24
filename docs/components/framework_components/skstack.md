# SKStack

`SKStack` is the **navigation stack**: an ordered list of
[`SKScreen`](../skscreen.md)s where the last one is visible and the others are
kept behind it. It is the backbone of in-app navigation — pushing, popping and
replacing screens, with optional transitions.

Most navigation is done from the screens themselves
([`push`/`finish`/`replaceWith`…](../skscreen.md#navigation-api)); those methods
delegate to the screen's parent `SKStack`. You use `SKStack` directly when you
embed a stack as a sub-component (e.g. a tab that has its own navigation).

## SKRootStack

`SKRootStack` is the application-wide root stack (an `object`, i.e. a singleton).
It is the top-level navigation host; a screen with no explicit parent stack pushes
onto it.

```kotlin
SKRootStack.content = HomeScreen()      // set the root screen
SKRootStack.push(DetailsScreen(id))     // navigate forward
SKRootStack.screenOnTop                 // the currently visible screen
SKRootStack.resetToRoot()               // pop everything above the first screen
```

## Usage as a sub-component

Declare a `SKStackVC` in your `ScreenVC`:

```kotlin
interface TabScreenVC : SKScreenVC {
    val stack: SKStackVC
}
```

Instantiate it and set its first screen:

```kotlin
override val stack: SKStack = SKStack().apply {
    content = TabHomeScreen()
}
```

## Setting the content

| Way | Effect |
|--|--|
| `content = screen` | Reset the stack to a single screen. |
| `state = SKStack.State(listOf(a, b))` | Set the whole stack at once (with an optional transition). |

```kotlin
stack.state = SKStack.State(
    screens = listOf(rootScreen, detailScreen),
    transition = mySlideTransition,
)
```

## Navigation functions

| name | signature | description |
|--|--|--|
| `push` | `push(screen, transition?)` | Add `screen` on top of the stack. |
| `pop` | `pop(transition?, ifRoot?)` | Remove the top screen; runs `ifRoot` if only one screen remains. |
| `replace` | `replace(oldScreen, newScreen, transition?)` | Swap a screen already in the stack. |
| `remove` | `remove(screen, transition?)` | Remove a specific screen if present. |

```kotlin
stack.push(DetailsScreen(id))
stack.pop(ifRoot = { finish() })   // leave the host when popping the root
```

> Screens removed from the stack get `onRemove()` called automatically (their
> coroutines and observers are released) — this is the cascade that prevents
> leaks. See [SKComponent — lifecycle](../skcomponent.md#2-lifecycle--memory-management).
> Conversely, screens added to the stack get their `parent` set to this stack.

## Transitions

You can pass a `SKTransition` per call, or set defaults on the stack so every
push/pop animates consistently:

| Property | Type | Description |
|--|--|--|
| `defaultPushTransition` | `SKTransition?` | Used when a screen is pushed and no per-call transition is given. |
| `defaultPopTransition` | `SKTransition?` | Used when a screen is popped and no per-call transition is given. |

The stack detects whether a `state` change is a push (one screen appended) or a
pop (one screen dropped) and applies the matching default transition.

## Properties

| name | type | description |
|--|--|--|
| `state` | `SKStack.State` | The full stack state (`screens` + optional `transition`). |
| `content` | `SKScreen<*>` | Get the top screen; set to reset the stack to a single screen. |
| `defaultPushTransition` | `SKTransition?` | Default push animation. |
| `defaultPopTransition` | `SKTransition?` | Default pop animation. |

### SKRootStack-only

| name | type | description |
|--|--|--|
| `screenOnTop` | `SKScreen<*>?` | The currently visible screen, or `null` if empty. |
| `resetToRoot()` | function | Pop every screen above the first one. |

## See also

- [SKScreen](../skscreen.md) — navigation from the screen side
- [SKFrame](skframe.md) — show one screen from a set without a stack
- [SKComponent in-depth guide](../skcomponent.md)