package tech.skot.core.components

import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import tech.skot.view.live.MutableSKLiveData

class SKPagerViewProxy(
    initialScreens: List<SKScreenViewProxy<*>>,
    override val onUserSwipeToPage: ((index: Int) -> Unit)?,
    initialSelectedPageIndex: Int,
    override val swipable: Boolean,
) : SKComponentViewProxy<ViewPager2>(), SKPagerVC {
    private val selectedPageIndexLD = MutableSKLiveData<Int>(initialSelectedPageIndex)
    override var selectedPageIndex by selectedPageIndexLD

    private val screensLD = MutableSKLiveData(initialScreens)
    override var screens: List<SKScreenVC>
        get() = screensLD.value
        set(value) {
            @Suppress("UNCHECKED_CAST")
            screensLD.postValue(value as List<SKScreenViewProxy<*>>)
        }

    override fun saveState() {
        for (screenViewProxy in screensLD.value) {
            screenViewProxy.saveState()
        }
    }

    override fun bindTo(
        activity: SKActivity,
        fragment: Fragment?,
        binding: ViewPager2,
    ) = SKPagerView(this, activity, fragment, binding).apply {
        onOnUserSwipeToPage(onUserSwipeToPage)
        onSwipable(swipable)
        selectedPageIndexLD.observe {
            onSelectedPageIndex(it)
        }
        screensLD.observe {
            onScreens(it)
        }
    }
}
