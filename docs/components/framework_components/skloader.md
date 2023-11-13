# SKLoader

Show a layout when some operation take long time. Usually use as a [ProgressBar](https://developer.android.com/reference/android/widget/ProgressBar)

## Usage

In your ScreenVC, declare a SKLoaderVC :

```kotlin
interface MyAccountScreenVC : SKScreenVC, AccountActions {
    val loader: SKLoaderVC
}
```

Launch the generation with the SKGenerate gradle task.
In your screen, you have to instantiate your component as below :

```kotlin
override val loader: SKLoader = SKLoader()
```

In your xml layout file, add a layout with id `loader`. It can be a `ProgressBar`, `ConstraintLayout` or whatever you want. 
You can for example create an xml file called `my_loader.xml` 

```xml
<!--my_loader.xml-->
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:clickable="true"
    android:focusable="true"
    android:visibility="gone"
    tools:visibility="visible"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ProgressBar
        android:id="@+id/progres_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:theme="@style/AppTheme_MaterialAlertDialog" />
</FrameLayout>
```

And in your view, include this layout specifying the id `loader` : 

```xml
<include
    android:id="@+id/loader"
    layout="@layout/loader" />
```

SKLoader deals with an internal counter. Each time `skcomponent.launchWithOptions(withLoader = true){}`, `skcomponent.launchWithLoaderAndError` or `skdata.onData` function are called, counter is incremented on job started, and decremented on job ended. 
Loader is display when this internal counter > 0.
You can also manually show a skloader using `loader.workStarted` function. In this case, you have to call `loader.workEnded()` for updating counter value. 
You can get loader visibility using `loader.isLoading()` or `loader.isNotLoading()` function


