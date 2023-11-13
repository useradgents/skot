# SKButton

Show a Button

## Usage

* In your ScreenVC add a SKButtonVC

```kotlin    
val myButtonId : SKButtonVC  
 ``` 

Launch the generation with the SKGenerate gradle task


* In your Screen, instantiate the SKButton

```kotlin  
override val myButtonId = SKButton(name) {  
   //onTap 
}  
```

* in your xml, add a Button or a MaterialButton with id `myButtonId`
## Properties

| name | type | description |  
|--|--|--|  
| onTap | ()->Unit | Function called when button is tapped |  
| label | String | Text to display on Button |  
| enabled | Boolean | Is Button enabled or not |  
| hidden | Boolean | Is button hidden or visible |  
| debounce | Long | Debounce in milliseconds for preventing multiple taps. Default value is 500 ms  |