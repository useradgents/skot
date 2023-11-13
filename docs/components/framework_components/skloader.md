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
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clickable="true"
    android:focusable="true"
    android:outlineProvider="none"
    android:visibility="gone"
    app:cardBackgroundColor="@color/transparent"
    app:cardElevation="50dp"
    tools:visibility="visible">

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ProgressBar
            android:id="@+id/progres_bar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:theme="@style/AppTheme_MaterialAlertDialog" />

    </FrameLayout>
</androidx.cardview.widget.CardView>
```

And in your view, include this layout specifying the id `loader` : 

```xml
<include
    android:id="@+id/loader"
    layout="@layout/loader" />
```

There is no more to do for using SKLoader. Skot framework automatically show it when you call `launchWithOptions(withLoader = true){}` function. 


