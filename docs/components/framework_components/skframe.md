# SKFrame

Show a SKScreen inside another SKScreen, as a Fragment

## Usage

In your ScreenVC, declare a SKFrameVC. You also have to declare in `SKOpens` annotations other `SKScreen` you want to include.

```kotlin
@SKOpens([BuyingVouchersScreenVC::class])
interface MyCardsScreenVC : SKScreenVC {
    val frame: SKFrameVC
}
```

Launch the generation with the SKGenerate gradle task.
In your screen, you have to instantiate your component as below : 

```kotlin
override val frame = SKFrame(screens = setOf(buyingVouchersScreen), screenInitial = buyingVouchersScreen)
```
 
In your xml layout file, add a FrameLayout with id `frame`

```xml
<FrameLayout
    android:id="@+id/frame"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:elevation="12dp"
    />
```

You may want to use the SKFrame as a bottom sheet including a `SKScreen` : 

```xml
<FrameLayout
    android:id="@+id/frame"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:elevation="12dp"
    app:behavior_hideable="false"
    app:behavior_peekHeight="72dp"
    app:layout_behavior="com.google.android.material.bottomsheet.BottomSheetBehavior"
/>
```

## Properties
| name | type | description |  
|--|--|--|
| screens |  Set<SKScreen<*>> | Set of SKScreen you want to display |
| screenInitial | SKScreen<*>? |  The initial screen to display. If not null, must be a part of `screens`|
