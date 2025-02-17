package tech.skot.core.di

import tech.skot.core.components.SKBoxVC
import tech.skot.core.components.SKBoxViewMock
import tech.skot.core.components.SKComponentVC
import tech.skot.core.components.SKFrameVC
import tech.skot.core.components.SKFrameViewMock
import tech.skot.core.components.SKListVC
import tech.skot.core.components.SKListViewMock
import tech.skot.core.components.SKLoaderVC
import tech.skot.core.components.SKLoaderViewMock
import tech.skot.core.components.SKPagerVC
import tech.skot.core.components.SKPagerViewMock
import tech.skot.core.components.SKPagerWithTabsVC
import tech.skot.core.components.SKPagerWithTabsViewMock
import tech.skot.core.components.SKRootStackViewMock
import tech.skot.core.components.SKScreenVC
import tech.skot.core.components.SKStackVC
import tech.skot.core.components.SKStackViewMock
import tech.skot.core.components.SKWebViewVC
import tech.skot.core.components.SKWebViewViewMock
import tech.skot.core.components.inputs.SKButtonVC
import tech.skot.core.components.inputs.SKButtonViewMock
import tech.skot.core.components.inputs.SKComboVC
import tech.skot.core.components.inputs.SKComboViewMock
import tech.skot.core.components.inputs.SKImageButtonVC
import tech.skot.core.components.inputs.SKImageButtonViewMock
import tech.skot.core.components.inputs.SKInputVC
import tech.skot.core.components.inputs.SKInputViewMock
import tech.skot.core.components.inputs.SKInputWithSuggestionsVC
import tech.skot.core.components.inputs.SKInputWithSuggestionsViewMock
import tech.skot.core.components.inputs.SKSimpleInputVC
import tech.skot.core.components.inputs.SKSimpleInputViewMock
import tech.skot.core.components.presented.SKAlertVC
import tech.skot.core.components.presented.SKAlertViewMock
import tech.skot.core.components.presented.SKBottomSheetVC
import tech.skot.core.components.presented.SKBottomSheetViewMock
import tech.skot.core.components.presented.SKDialogVC
import tech.skot.core.components.presented.SKDialogViewMock
import tech.skot.core.components.presented.SKSnackBarVC
import tech.skot.core.components.presented.SKSnackBarViewMock
import tech.skot.core.components.presented.SKWindowPopupVC
import tech.skot.core.components.presented.SKWindowPopupViewMock
import tech.skot.core.view.Icon

class CoreViewInjectorMock : CoreViewInjector {
    override fun rootStack(): SKStackVC {
        return SKRootStackViewMock
    }

    override fun stack(): SKStackVC {
        return SKStackViewMock()
    }

    override fun alert(): SKAlertVC {
        return SKAlertViewMock()
    }

    override fun snackBar(): SKSnackBarVC {
        return SKSnackBarViewMock()
    }

    override fun bottomSheet(): SKBottomSheetVC {
        return SKBottomSheetViewMock()
    }

    override fun dialog(): SKDialogVC {
        return SKDialogViewMock()
    }

    override fun windowPopup(): SKWindowPopupVC {
        return SKWindowPopupViewMock()
    }

    override fun pager(
        screens: List<SKScreenVC>,
        onUserSwipeToPage: ((index: Int) -> Unit)?,
        initialSelectedPageIndex: Int,
        swipable: Boolean,
    ): SKPagerVC {
        return SKPagerViewMock(screens, onUserSwipeToPage, initialSelectedPageIndex, swipable)
    }

    override fun pagerWithTabs(
        pager: SKPagerVC,
        onUserTabClick: ((index: Int) -> Unit)?,
        tabConfigs: List<SKPagerWithTabsVC.TabConfig>,
        visibility: SKPagerWithTabsVC.Visibility,
    ): SKPagerWithTabsVC {
        return SKPagerWithTabsViewMock(pager, onUserTabClick, tabConfigs, visibility)
    }

    override fun skList(
        layoutMode: SKListVC.LayoutMode,
        reverse: Boolean,
        animate: Boolean,
        animateItem: Boolean,
        infiniteScroll: Boolean
    ): SKListVC {
        return SKListViewMock(layoutMode, reverse, animate, animateItem)
    }

    override fun skBox(
        itemsInitial: List<SKComponentVC>,
        hiddenInitial: Boolean?,
    ): SKBoxVC {
        return SKBoxViewMock(itemsInitial, hiddenInitial)
    }

    override fun webView(
        config: SKWebViewVC.Config,
        launchInitial: SKWebViewVC.Launch?,
    ): SKWebViewVC {
        return SKWebViewViewMock(config, launchInitial)
    }

    override fun frame(
        screens: Set<SKScreenVC>,
        screenInitial: SKScreenVC?,
    ): SKFrameVC {
        return SKFrameViewMock(screens, screenInitial)
    }

    override fun loader(): SKLoaderVC {
        return SKLoaderViewMock()
    }

    override fun input(
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
    ): SKInputVC {
        return SKInputViewMock(
            onInputText,
            type,
            maxSize,
            onFocusChange,
            onDone,
            hintInitial,
            textInitial,
            errorInitial,
            hiddenInitial,
            enabledInitial,
            showPasswordInitial,
        )
    }

    override fun inputSimple(
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
    ): SKSimpleInputVC {
        return SKSimpleInputViewMock(
            onInputText,
            type,
            maxSize,
            onFocusChange,
            onDone,
            hintInitial,
            textInitial,
            errorInitial,
            hiddenInitial,
            enabledInitial,
            showPasswordInitial,
        )
    }

    override fun combo(
        hint: String?,
        errorInitial: String?,
        onSelected: ((choice: Any?) -> Unit)?,
        choicesInitial: List<SKComboVC.Choice>,
        selectedInitial: SKComboVC.Choice?,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        dropDownDisplayedInitial: Boolean,
        oldSchoolModeHint: Boolean,
    ): SKComboVC {
        return SKComboViewMock(
            hint,
            errorInitial,
            onSelected,
            choicesInitial,
            selectedInitial,
            enabledInitial,
            hiddenInitial,
            dropDownDisplayedInitial,
            oldSchoolModeHint,
        )
    }

    override fun inputWithSuggestions(
        hint: String?,
        errorInitial: String?,
        onSelected: ((choice: Any?) -> Unit)?,
        choicesInitial: List<SKComboVC.Choice>,
        selectedInitial: SKComboVC.Choice?,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        dropDownDisplayedInitial: Boolean,
        onInputText: (input: String?) -> Unit,
        onFocusChange: ((hasFocus:Boolean) -> Unit)?,
        oldSchoolModeHint: Boolean,
    ): SKInputWithSuggestionsVC {
        return SKInputWithSuggestionsViewMock(
            hint,
            errorInitial,
            onSelected,
            choicesInitial,
            selectedInitial,
            enabledInitial,
            hiddenInitial,
            dropDownDisplayedInitial,
            onInputText,
            onFocusChange,
            oldSchoolModeHint,
        )
    }

    override fun button(
        onTapInitial: (() -> Unit)?,
        labelInitial: String?,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        debounce: Long?,
    ): SKButtonVC {
        return SKButtonViewMock(onTapInitial, labelInitial, enabledInitial, hiddenInitial, debounce)
    }

    override fun imageButton(
        onTapInitial: (() -> Unit)?,
        iconInitial: Icon,
        enabledInitial: Boolean?,
        hiddenInitial: Boolean?,
        debounce: Long?,
    ): SKImageButtonVC {
        return SKImageButtonViewMock(onTapInitial, iconInitial, enabledInitial, hiddenInitial, debounce)
    }
}
