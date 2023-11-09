# Model

In the context of Skot the model is linked to its [SKComponent](../components). This is most often a [SKScreen](../components/skscreen.md). The possible interactions are described in an interface that links the model and the ViewModel of the SKComponent.
The purpose of this model is to retrieve the data, then compile it to provide the necessary information to the SKComponent. It can also retrieve the information entered in the SKComponent.
Often, the model does not perform any processing and simply passes the data between the [ViewModel](viewmodel.md) and the [BusinessModel](businessmodel.md).

See [create my first component](../start/createcomponent.md)