# SKPager

Show a [ViewPager2](https://developer.android.com/jetpack/androidx/releases/viewpager2) as described in Android documentation 

## Usage
In your ScreenVC, declare a SKPagerVC. You also have to declare screens you want to display in `SKUses` annotation :

```kotlin
@SKUses([PaymentSlideScreenVC::class])
interface PaymentOnBoardingScreenVC : SKScreenVC {
    val pager: SKPagerVC
}
```

Launch the generation with the SKGenerate gradle task.
In your screen, you have to instanciate your component as below :

```kotlin
override val pager = SKPager(
        initialScreens = listOf(PaymentSlideScreen(/**/), PaymentSlideScreen(/**/)),
        initialSelectedPageIndex = 0,
        swipable = true
    )
```

In your xml layout file, you have to declare a `ViewPager2` widget with id `pager` : 

```xml
<androidx.viewpager2.widget.ViewPager2
    android:id="@+id/pager"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintBottom_toTopOf="@+id/linear_button"
    app:layout_constraintTop_toBottomOf="@+id/actionBar" />
```

`initialScreens` cou property can be an empty list. You can define the list of TabPages to be displayed at any time by setting the `screens` property.

```kotlin
pager.screens = init.onBoardings.map { page ->
                    SplashPage(page)
                }
```

## Properties

| name | type | description |
|--|--|--|
| initialScreens | List<SKScreen<*>> | Initial screens to display |
| onUserSwipeToPage | ((index: Int) -> Unit)? | Function called when user swipe to a page |
| initialSelectedPageIndex | Int | Set initial view pager page index. Default value is 0 |
| swipable | Boolean | Allow user to swipe on component to change page. Default value is true |