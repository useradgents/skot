package tech.skot.core.di

import tech.skot.core.components.SKBoxVC
import tech.skot.core.components.SKComponentVC
import tech.skot.core.components.SKFrameVC
import tech.skot.core.components.SKListVC
import tech.skot.core.components.SKLoaderVC
import tech.skot.core.components.SKPagerVC
import tech.skot.core.components.SKPagerWithTabsVC
import tech.skot.core.components.SKScreenVC
import tech.skot.core.components.SKStackVC
import tech.skot.core.components.SKWebViewVC
import tech.skot.core.components.inputs.SKButtonVC
import tech.skot.core.components.inputs.SKComboVC
import tech.skot.core.components.inputs.SKImageButtonVC
import tech.skot.core.components.inputs.SKInputVC
import tech.skot.core.components.inputs.SKInputWithSuggestionsVC
import tech.skot.core.components.inputs.SKSimpleInputVC
import tech.skot.core.components.presented.SKAlertVC
import tech.skot.core.components.presented.SKBottomSheetVC
import tech.skot.core.components.presented.SKDialogVC
import tech.skot.core.components.presented.SKSnackBarVC
import tech.skot.core.components.presented.SKWindowPopupVC
import tech.skot.core.view.Icon

interface CoreViewInjector {
    fun rootStack(): SKStackVC
    fun stack(): SKStackVC
    fun alert(): SKAlertVC
    fun snackBar(): SKSnackBarVC
    fun bottomSheet(): SKBottomSheetVC
    fun dialog(): SKDialogVC
    fun windowPopup(): SKWindowPopupVC
    fun pager(
        screens: List<SKScreenVC>,
        onUserSwipeToPage: ((index: Int) -> Unit)?,
        initialSelectedPageIndex: Int,
        swipable: Boolean,
    ): SKPagerVC

    fun pagerWithTabs(
        pager: SKPagerVC,
        onUserTabClick: ((index: Int) -> Unit)?,
        tabConfigs: List<SKPagerWithTabsVC.TabConfig>,
        tabsVisibility: SKPagerWithTabsVC.Visibility,
    ): SKPagerWithTabsVC

    fun skList(
        layoutMode: SKListVC.LayoutMode,
        reverse: Boolean,
        animate: Boolean,
        animateItem: Boolean,
        infiniteScroll: Boolean,
    ): SKListVC

    fun skBox(itemsInitial: List<SKComponentVC>, hiddenInitial: Boolean?): SKBoxVC
    fun webView(config: SKWebViewVC.Config, launchInitial: SKWebViewVC.Launch?): SKWebViewVC
    fun frame(screens: Set<SKScreenVC>, screenInitial: SKScreenVC?): SKFrameVC
    fun loader(): SKLoaderVC

    fun input(
        onInputText: (newText: String?) -> Unit,
        type: SKInputVC.Type?,
        maxSize: Int?,
        onFocusChange: ((hasFocus: Boolean) -> Unit)?,
        onDone: ((text: String?) -> Unit)?,
        hintInitial: String?,
        textInitial: String?,
        errorInitial: String?,
        hiddenInitial: Boolean?,
        enabledInitial: Boolean?,
        showPasswordInitial: Boolean?,
    ): SKInputVC

    fun inputSimple(
        onInputText: (newText: String?) -> Unit,
        type: SKInputVC.Type?,
        maxSize: Int?,
        onFocusChange: ((hasFocus: Boolean) -> Unit)?,
        onDone: ((text: String?) -> Unit)?,
        hintInitial: String?,
        textInitial: String?,
        errorInitial: String?,
        hiddenInitial: Boolean?,
        enabledInitial: Boolean?,
        showPasswordInitial: Boolean?,
    ): SKSimpleInputVC

    fun combo(
        hint: String?,
        errorInitial: String?,
        onSelected: ((choice: Any?) -> Unit)?,
        choicesInitial: List<SKComboVC.Choice>,
        selectedInitial: SKComboVC.Choice?,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        dropDownDisplayedInitial: Boolean,
        oldSchoolModeHint: Boolean,
    ): SKComboVC

    fun inputWithSuggestions(
        hint: String?,
        errorInitial: String?,
        onSelected: ((choice: Any?) -> Unit)?,
        choicesInitial: List<SKComboVC.Choice>,
        selectedInitial: SKComboVC.Choice?,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        dropDownDisplayedInitial: Boolean,
        onInputText: (input: String?) -> Unit,
        oldSchoolModeHint: Boolean,
    ): SKInputWithSuggestionsVC


    fun button(
        onTapInitial: (() -> Unit)?,
        labelInitial: String?,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        debounce: Long?,
    ): SKButtonVC

    fun imageButton(
        onTapInitial: (() -> Unit)?,
        iconInitial: Icon,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        debounce: Long?,
    ): SKImageButtonVC
}
