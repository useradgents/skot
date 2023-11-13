# ViewContract Module

This module contains the interfaces that allow communication between a view and its viewModel.

All components have an interface with their name suffixed by VC, which contains the set of possible interactions between the view and the viewModel. These contracts inherit either from SkComponentVC or SKScreenVC and also contain all the subcomponents used.

- Component sample code :
    ```kotlin
    interface MyComponentVC: SKComponentVC {
        val title : String
        var subTitle : String
        val button : SkBUttonVC
        val myOtherComponent : MyOtherComponentVC
    }
    ```

- Screen sample code :
    ```kotlin
    interface MyScreenVC: SKSCreenVC {
        val title : String
        var subTitle : String
        val button : SkBUttonVC
        val myOtherComponent : MyOtherComponentVC
    }
    ```

In these examples, the viewModel will be able to pass a title and subtitle to the view, and will allow the use of the SkButton and MyOtherComponent components.

The difference between SKScreen and SKComponent lies in the presence of navigation methods for SkScreen. Apart from that, SKScreen inherits from SKComponent, which allows for exactly the same functionality in both cases.

[see create Component](../start/createcomponent.md) 

[see create Screen](../start/createscreen.md) 