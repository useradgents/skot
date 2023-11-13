# ViewModel Module


Each viewModel can interact with its view and its model, if it exists, through the interactions described respectively in the [ViewContract](viewcontract.md) and the [ModelContract](modelcontract.md).
Its role is to give the view what it needs to display correctly, by dispatching data to the different components it uses. The data comes from its constructor or is retrieved from the associated Model.

Basic ViewModel sample code 
```kotlin
class MyComponent(title : String): MyComponentGen() {
    val button = SkButton()
    val myOtherComponent = MyOtherComponent()

    override val view: MyComponentVC = viewInjector.myComponent(
        this,
        button = button.view,
        myOtherComponent = myOtherComponent.view,
        title = title,
        subtitleInitial = model.getSubtitle()
    )
}
```
   