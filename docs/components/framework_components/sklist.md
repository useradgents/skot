# SKList

Show a List or a grid

## Usage

* In your ScreenVC add a SKListVC

```kotlin  
val myList : SKListVC  
```  

Launch the generation with the SKGenerate gradle task

* In your Screen, instantiate the SKList

```kotlin  
override val myList = SKList(name)  
```  

* Instantiate a SKComponent for each items you ant to show in your SKList, and add this list in your SKlist :
```kotlin  
  init{  
	 val listItems = listOf(
			 MyComponentHeader("testHeader1"),
			 MyComponent("test1"),
			 MyComponent("test2"),
			 MyComponentHeader("testHeader2"),
			 MyComponent("test3"),
			 MyComponent("test4"),
			 MyComponent("test5")
	)
	myList.items = listItems 
}  
```  
* In your xml, add a RecyclerView with your id (myList in this sample)

For improving list performances, you should override `computeItemId` function in every list item SkComponent. 
You can also override `onSwipe` function which will be called on item swiped.


## Properties

| name | type |description |  
|--|--|--|  
| layoutMode | SKListVC.LayoutMode | It could be Manual, Linear or Grid mode |
| reverse | Boolean | When set to true, layouts from end to start  |
| animate | Boolean | Animate list on change |
| animateItem | Boolean | Animate item when it changes or not |


| SKListVC.LayoutMode |  |
|--|--|
| Manual | Use an other LayoutManager, such as `FlexboxLayoutManager` for example. It has to be full managed by app |
| Linear | Standard list mode. Set `vertical` property to false for horizontal list |
| Grid | Grid mode. Set `nbColumn` property to define number of grid columns, and `vertical` property to false for an horizontal gridv| 

