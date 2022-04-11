package tech.skot.core.components

import tech.skot.core.view.Icon
import tech.skot.core.view.SKSpannedString

@SKLayoutIsSimpleView
interface SKPagerWithTabsVC: SKComponentVC {
    val pager:SKPagerVC
    var tabConfigs:List<TabConfig>
    var showTabs : Boolean

    sealed class TabConfig
    class IconTitleTab(val title : SKSpannedString, val icon : Icon):TabConfig()
    class TitleTab(val title : SKSpannedString):TabConfig()
    class IconTab(val icon : Icon):TabConfig()
    class CustomTab(val tab : SKComponentVC) : TabConfig()

}