# SKScreen

Abstract component representing a Screen

## Usage

For creating new screen, you have to create an interface extending `SKScreenVC` in `viewcontract` module.
In this interface, every components from view that need to communicate with viewmodel have to be declared :

```kotlin
interface FaqPublicScreenVC : SKScreenVC {
    val actionBar: ActionBarVC
    val loader: SKLoaderVC
    val list: SKListVC
    val contactButton: SKButtonVC
}
```

The `ScreenVC` suffix is important for Skot framework.

Launch the generation with the SKGenerate gradle task.

Some files will be created, the mains are :

| module | file |
|--|--|--|
| view | FaqPublicScreenView.kt |  |
| view - generated | FaqPublicScreenViewProxy.kt |
| view - androidTest | TestViewFaqPublicScreen.kt |
| view - res/layout/ | faq_public_screen.xml |
| viewmodel | FaqPublicScreen.kt |
| viewmodel - jvmTest | FaqPublicScreenTest.kt |


For pushing a new screen, you have to call `push` function :

```kotlin
fun changeScreen() {
    push(FaqContactScreen())
}
```