# ModelContract

This module contains the interfaces that allow communication between a viewModel and its model.

All components can have an interface with their name suffixed by MC, which contains the set of possible interactions between the viewModel and the Model.

- Component sample code :
    ```kotlin
    interface MyComponentMC {
        val subtitle = SKData<String>
        suspend fun doClickAction() 
    }
    ```

- Screen sample code :
    ```kotlin
    interface MyScreenMC {
        val subtitle = SKData<String>
        suspend fun doClickAction() 
    }
    ```

In these examples, the Model will be able to pass a subtitle via a SKData to the ViewModel, and the ViewModel will be able to call the doClickAction method when the button is clicked.

Here is an example of use

  ```kotlin
    class MyScreen : MyScreenGen() {
        
        override val button = SKButton(strings.text) {
            launchWithLoaderAndError {
                model.doClickAction()
            }
        }
  
        override val loader = SKLoader()
        
        override val view: MyScreenVC = viewInjector.myScreen(
            visibilityListener = this,
            subtitleInitial = "",
            button = button.view,
            loader = loader.view
        )
        
        init {
            model.subtitle.onData {
                view.subtitle = it
            }
        }
    }
  ```

  ```kotlin
    class MyScreenModel( override val coroutineContext: CoroutineContext) : MyScreenMC, CoroutineScope {
        override val subtitle = SKManualData()
        
        override suspend fun doClickAction(){
            val subtitle = rootState.myModel.computeNewSubtitle()
            
            subtitle.value = subtitle
        }
    }
  ```