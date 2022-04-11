package tech.skot.core.components

import tech.skot.core.di.coreViewInjector
import tech.skot.core.view.Icon
import tech.skot.core.view.SKSpannedString
import tech.skot.core.view.skSpannedString

class SKPagerWithTabs(
    initialPages: List<TabPage> = emptyList(),
    onSwipeToPage: ((index: Int) -> Unit)? = null,
    initialSelectedPageIndex: Int = 0,
    swipable: Boolean = false,
    initialShowTabs: Boolean = true
) : SKComponent<SKPagerWithTabsVC>() {
    sealed class TabPage(val screen: SKScreen<*>)
    class Page(screen: SKScreen<*>, val label: String) : TabPage(screen)
    class ConfigurableTabPage(
        screen: SKScreen<*>,
        val tabConfig: TabConfig
    ) : TabPage(screen)

    sealed class TabConfig {
        class CustomTab(val component: SKComponent<*>) : TabConfig()
        class TitleTab(val title: SKSpannedString) : TabConfig(){
            constructor(title: String) : this(skSpannedString { append(title) })
        }
        class IconTitleTab(val title: SKSpannedString, val icon : Icon) : TabConfig()
        class IconTab(val icon : Icon) : TabConfig()
    }


    val pager = SKPager(
        initialScreens = initialPages.map { it.screen },
        onSwipeToPage = onSwipeToPage,
        initialSelectedPageIndex = initialSelectedPageIndex,
        swipable = swipable
    )

    var pages: List<TabPage> = initialPages
        set(value) {
            pager.screens = value.map { it.screen }
            view.tabConfigs = mapTabConfig(value)
            field = value
        }

    var showTabs: Boolean = initialShowTabs
        set(value) {
            view.showTabs = value
            field = value
        }

    private fun mapTabConfig(values: List<TabPage>): List<SKPagerWithTabsVC.TabConfig> {
        return values.map {
            when (it) {
                is Page -> {
                    SKPagerWithTabsVC.TitleTab(skSpannedString { append(it.label)})
                }
                is ConfigurableTabPage -> {
                    when(val tab = it.tabConfig){
                        is TabConfig.CustomTab -> {
                            SKPagerWithTabsVC.CustomTab(tab.component.view)
                        }
                        is TabConfig.TitleTab -> {
                            SKPagerWithTabsVC.TitleTab(tab.title)
                        }
                        is TabConfig.IconTitleTab -> {
                            SKPagerWithTabsVC.IconTitleTab(tab.title, tab.icon)
                        }
                        is TabConfig.IconTab ->{
                            SKPagerWithTabsVC.IconTab(tab.icon)
                        }
                    }
                }
            }
        }
    }

    override val view =
        coreViewInjector.pagerWithTabs(pager.view, mapTabConfig(initialPages), initialShowTabs)

    override fun onRemove() {
        pager.onRemove()
    }

}