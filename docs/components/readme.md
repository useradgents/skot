# SKComponent

A SKComponent is a graphical android element boosted by Skot framework. It could be a entire screen or a widget such as a button or a list.
Each SKComponent is described by a SKComponentVC which will contain all properties and functions.
The goal is to have reusable views throughout the application in a simple and consistent way.


## Structure
The SkComponent respects a MVVM architecture and uses interfaces to connect the layers.
The ViewContract is the first file you write when creating a component. It describes all the possible interactions between the view and the viewModel.
The viewModel is generated the first time by the Framework from the declarations in the ViewContract.
The view is generated the first time by the Framework from the declarations in the ViewContract.
The link between the view and the viewModel is made through a ViewProxy. It allows, among other things, that changes in the viewModel are only sent to the view when it exists.
If necessary, you can add a model. It works the same way. You need a ModelContract to connect the model and the viewModel.



## Usage
The ViewContract is an interface that you must create in the viewcontract module. The name of the interface must be prefixed with VC and it must inherit from MyComponent.

Inside the ViewContract, you must specify all the components and all the functions, values, or variables that are required for your view.

:warning: Functions are called only once, unlike values and variables. This means that the call may be ignored if the view does not exist at the time the function is called.
In addition, a function cannot return a value. If a return value is required, you must use a callback.

:warning: Do not forget to define the components used in the SKUses annotation so that they are generated.

```
@SKUses([MyOtherComponentVC::class])
interface MyComponentVC : SKComponentVC {
  fun myFunction()
  val otherComponent : MyOtherComponentVC
  val value : String?
  var variable : String
}
```

## SKScreen
This is the component type to use for creating application pages.
see [SkScreen](skscreen.md)


:warning: We recommend that you suffix the names of screens with "Screen" to simplify the readability of the project. This is not, however, mandatory.


## SKComponent Included in the framework

To simplify development, most used Components are included in the framework

see [Framework components](framework_components/readme.md)

## SKComponent as Optional Framework Library


- SKMap
  This Skot library can be used to display a Google or Mapbox map in the application.

  see [SKMap](https://github.com/useradgents/sk-map#readme)

- SKBottomNav
  explanation -> link to library repo doc

- SKVideo
  This Skot library provides a helper for using Exoplayer for audio or video.
- 
  see [SKVideo](https://github.com/useradgents/sk-video#readme)

## Create My SKComponent




