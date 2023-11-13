
- In the `screens` folder of the `viewcontract` module, create an interface called `MySKComponentVC` that inherits from `SKComponentVC`.

  ```
  interface MyComponentVC : SKComponentVC {
    
  }
  ```
- Add all the necessary elements inside this view :
    - SKComponentVC
      ```
      @SKUses([MyOtherComponentVC::class])
      interface MyComponentVC : SKComponentVC {
        val otherComponent : MyOtherComponentVC
      }
      ```
    - value
      ```
      interface MyComponentVC : SKComponentVC {
        val value : String?
      }
      ```
    - variable
      ```
      interface MyComponentVC : SKComponentVC {
        val variable : String?
      }
      ```  
    - function
      ```
      interface MyComponentVC : SKComponentVC {
        fun myFunction(onFinish : () -> Unit)
      }
      ```  

  ```
  @SKUses([MyOtherComponentVC::class])
  interface MyComponentVC : SKComponentVC {
  fun myFunction()
  val otherComponent : MyOtherComponentVC
  val value : String?
  var variable : String
  }
  ```


- Add the KClass of this interface to the annotation of the viewContract of the view that will use it, so that this new component is taken into account in the generation.
  ```
  @SKUses([MyComponentVC::class])
  interface MyScreenVC : SKScreenVC {
    val myComponent : MyComponentVC
  }
  ```  

- To generate the viewModel the view and the view xml, run the Gradle task `skGenerate`. The ViewModel will have the same name as the ViewContract but without the suffix `VC`. The view will have the same name as the ViewContract but replacing the suffix `VC` with `View`. The XML will have the same name as the ViewModel but in snake case.
    - viewModel : MyComponent
      ```
      class MyComponent : MyComponentGen() {
          override val otherComponent = MyOtherComponent()
      
          override val view: MyComponent = viewInjector.myComponent(
              visibilityListener = this,
              value = "MyValue",
              variableInitial = "MyVariable",
              otherComponent = otherComponent.view
          )
      }
       ```
    - view : MyComponentView
      ```
      class MyComponentView(
         override val proxy: MyComponentViewProxy,
         activity: SKActivity,
         fragment: Fragment?,
         binding: AccountSimpleItemBinding,
      ) : SKComponentView<MyComponentBinding>(proxy, activity, fragment, binding),
      MyAccountRAI  {
          override fun onValue(value: String?): Unit {
              // use your value
          }
          override fun onVariable(variable: String): Unit {
              // use your value
          }
      }
      ```
    - xml : my_component.xml