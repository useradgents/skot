# Skot framewok project structure

A project contains six module : 

- androidApp : contains the application class.

- viewcontract : contains the contract of the views. Start point to create Screens or components
  
  [read more](viewcontract.md)

- viewmodel : contains the ViewModel of the views. 

  [read more](viewmodel.md)

- view : contains the view and its XML.

  [read more](view.md)

- modelcontract : contains the contract of the model.

  [read more](modelcontract.md)

- model : contains the model of the views, the businessModels, and all the code for network request
  
  [read more](model.md)

![](MVVM_skot.jpg "Schema architecture")

## Cross-cutting guides

These topics span several modules:

- [SKComponent in-depth guide](../components/skcomponent.md) — the ViewModel base class
- [SKData & the Model layer](../skdata.md) — the reactive data primitives
- [Dependency Injection](../dependency-injection.md) — modules, `get` / `getByName`
- [Error handling](../error-handling.md)
- [SKState](../start/addstate.md) — app states
- [Annotations reference](../annotations.md) — every generation annotation
- [Framework components](../components/framework_components/readme.md)