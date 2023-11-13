# SKPagerWithTabs

As [SKPager](skpager.md), show a [ViewPager2](https://developer.android.com/jetpack/androidx/releases/viewpager2). With this component, you can show and customize tab indicator.

## Usage
In your ScreenVC, declare a SKPagerWithTabs. You also have to declare screens you want to display in `SKUses` annotation :

```kotlin
@SKUses([MyCardTabVC::class])
interface MyCardInfoScreenVC : SKScreenVC {
    val pager: SKPagerWithTabsVC
}
```
Launch the generation with the SKGenerate gradle task.
In your screen, you have to instantiate your component as below :

```kotlin
override val pager = SKPagerWithTabs()
```

In your xml layout file, whereas with [SKPager](skpager.md) you have to set an id on ViewPager2 widget, on SKPagerWithTabs, the id 
`pager` must be set on ViewGroup (ConstraintLayout, LinearLayout or whatever you want). It must contain a ViewPager2 and a TabLayout with tag `sk_view_pager2` and `sk_tab_layout`


```xml
<LinearLayout
    android:id="@+id/pager"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginTop="10dp"
    android:gravity="center_horizontal"
    android:orientation="vertical"
    app:layout_constraintTop_toBottomOf="@+id/closeButton">

    <com.google.android.material.tabs.TabLayout
        style="@style/TabStyle"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:tag="sk_tab_layout"
        app:tabMode="auto" />

    <View
        android:id="@+id/separator"
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/stroke" />

    <androidx.viewpager2.widget.ViewPager2
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:tag="sk_view_pager2" />
</LinearLayout>
```

The `initialPages` property can be an empty list. You can define the list of TabPages to be displayed at any time by setting the `pages` property.

## Properties

| name | type | description |
|--|--|--|
| initialPages | List<TabPage> | Initial TabPage to display. Default value is an empty list |
| onUserSwipeToPage | ((index: Int) -> Unit)? | Function called when user swipe to a page |
| onUserTabClick | ((index: Int) -> Unit)? | Function called when user tap on tab |
| initialSelectedPageIndex | Int | Set initial view pager page index. Default value is 0 |
| swipable | Boolean | Allow user to swipe on component to change page. Default value is false |
| initialTabsVisibility | SKPagerWithTabsVC.Visibility  | Initial tabs visibility. Default value is Visible |

| TabPage(val screen: SKScreen<*>) |
|--|
| Page(screen: SKScreen<*>, val label: String) |
| ConfigurableTabPage(screen: SKScreen<*>, val tabConfig: TabConfig) |

| TabConfig |
|--|
| CustomTab(val component: SKComponent<*>) |
| SpannableTitleTab(val title: SKSpannedString)|
| TitleTab(val title: String) |
| IconTitleTab(val title: SKSpannedString, val icon: Icon) |
| IconTab(val icon: Icon) |

| Visibility | |
|--|--|
| Visible | Tabs are visible |  
| Gone | Tabs are not visible  | 
| Automatic | Tabs are visible if there are more than one items into view pager |