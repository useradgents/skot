package tech.skot.core.components

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import tech.skot.core.view.toCharSequence
import tech.skot.view.extensions.setVisible

class SKPagerWithTabsView(
    override val proxy: SKPagerWithTabsViewProxy,
    activity: SKActivity,
    fragment: Fragment?,
    view: View,
    private val viewPager2: ViewPager2,
    private val tabLayout: TabLayout
) : SKComponentView<View>(proxy, activity, fragment, view) {

    fun onLabels(tabConfigs: List<SKPagerWithTabsVC.TabConfig>) {
        TabLayoutMediator(
            tabLayout, viewPager2
        ) { tab, index ->
            when (val tabConfig = tabConfigs.get(index)) {
                is SKPagerWithTabsVC.IconTab -> {
                    tab.setIcon(tabConfig.icon.res)
                    tab.tabLabelVisibility = TabLayout.TAB_LABEL_VISIBILITY_UNLABELED
                }
                is SKPagerWithTabsVC.IconTitleTab -> {
                    tab.text = tabConfig.title.toCharSequence(context)
                    tab.setIcon(tabConfig.icon.res)

                }
                is SKPagerWithTabsVC.TitleTab -> {
                    tab.text = tabConfig.title.toCharSequence(context)
                }
                is SKPagerWithTabsVC.CustomTab -> {
                    (tabConfig.tab as SKComponentViewProxy<*>).layoutId?.let {
                        LayoutInflater.from(context).inflate(it, tab.view, false)
                    }?.let {
                        (tabConfig.tab as SKComponentViewProxy<*>).bindToItemView(
                            activity,
                            fragment,
                            it
                        )
                        tab.setCustomView(it)
                    }
                }
            }


        }.attach()
    }

    override fun onRecycle() {
        super.onRecycle()

    }

    fun onShowTabs(showTabs: Boolean) {
        tabLayout.setVisible(showTabs)
    }
}