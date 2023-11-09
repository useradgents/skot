# SKState

SkStates are used to describe the states of the application, for example, connected or in a payment tunnel. BusinessModels can be linked to a state, and therefore will only be accessible if the state exists.

There is therefore a rootState, which contains a hierarchy of states according to the needs of the application. When a state is reached, it is then sufficient to create the state and assign it to its parent state to activate this state. It is then possible to launch a screen that needs this state by passing it as a constructor parameter, and by mentioning its use in the model.

1. Define the state in the states/states.kt file of the modelcontract module
    ```
    interface ConnectedStateDef : SKStateDef {
        val userId: String
        var token: Token
    }
    ```
2. Link the state to its parent by declaring a val of the state's name in the parent
   ```
   interface RootStateDef : SKStateDef {
        val conf: ConfStateDef?
        val connected: ConnectedStateDef?
   }
   ```
   
3. Add the name of the BusinessModel interface(s) associated with this State, then run the generation with the command skGenerate
    ```
    @SKBms(["UserBM"])
    interface ConnectedStateDef : SKStateDef {
        val userId: String
        var token: Token
    }
    ```  
   :info: It is possible to add a new BusinessModel at any time by adding it to the @SKBms annotation and then running `skGenerate` again.

4. To specify that a component needs a state, simply declare a val of the type of that state in the ModelContract and then run the generation with the command `skGenerates`.
    ```
    interface AccountScreenMC {
       val connectedState: ConnectedStateDef
    }
    ``` 
5. The state will then need to be passed from the ViewModel.
    ```
    class AccountScreen() {
       connectedState: ConnectedConfStateContract
    } : AccountScreenGen(connectedState)
    ```
6. Here is an example of how to push this Screen.
    ```
    fun launchAccountScreen() {
       rootState.connectedState?.let {
           push(AccountScreen(it))
       }?:run { SkLog.d("User not connected") }
    }
    ```

