#SKAnnotations

## class annotations

### SKLayoutIsSimpleView
This annotation can be used for a viewContract. It specifies that the view contract is not bound to a view XML, but is directly a view. This is the case for example of the SKButton. It is possible to use it in a SKComponent, but in this case the viewProxy of the component must be moved next to the corresponding ComponentView, and the file must be modified to take into account the use of an Android view instead of the viewBinding.
## property annotations

### @SKPassToParentView

Used for sub component in SkComponentVC to specify that the view of this sub component should be passed to the ScreenView.

- viewContract 
    ```
    interface MyScreenVC : SKScreenVC {
        @SKPassToParentView
        val list: SKListVC
    }
    ```
- view 
    ```
    class MyScreenView(
        override val proxy: MyScreenViewProxy,
        activity: SKActivity,
        fragment: Fragment?,
        binding: MyScreenBinding,
        list: SKListView
    )
    ```

