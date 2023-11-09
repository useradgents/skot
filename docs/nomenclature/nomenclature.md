# Nomenclature

Most of the elements provided by the Skot Framework are prefixed with SK. This allows for a more intuitive use of code completion.

## Lexicon:
- SKComponent: a feature with the View/ViewContract/ViewModel structure that can be reused in the app. It may be linked to a model through a ModelContract.
- SKScreen: a screen with the View/ViewContract/ViewModel structure. It may be linked to a model through a ModelContract.
- SKState: describes the state of the app. BusinessModels are linked to a state, and a screen or component may require a state.
- SKRootStack: the main stack of the app. Pushing a screen into this stack will display it in a new activity.