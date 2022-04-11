package tech.skot.core.components

class SKPagerWithTabsViewMock(
    pager: SKPagerVC,
    tabConfigs: List<SKPagerWithTabsVC.TabConfig>,
    showTabs : Boolean
): SKComponentViewMock(), SKPagerWithTabsVC {
    override val pager: SKPagerVC = pager
    override var tabConfigs: List<SKPagerWithTabsVC.TabConfig> = tabConfigs
    override var showTabs: Boolean = showTabs
}