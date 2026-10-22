package tech.skot.core.components.presented

import androidx.fragment.app.Fragment
import tech.skot.core.components.*

class SKBottomSheetView(
    override val proxy: SKBottomSheetViewProxy,
    activity: SKActivity,
    fragment: Fragment?,
) : SKComponentView<Unit>(proxy, activity, fragment, Unit) {
    data class State(val state: SKBottomSheetVC.Shown, val bottomSheet: SKBottomSheetDialogFragment)

    private var current: State? = null

    fun onState(state: SKBottomSheetVC.Shown?) {
        if (state != current?.state) {
            current?.bottomSheet?.dismiss()
            // Après une recréation de l'Activity, le système a déjà restauré le volet affiché :
            // on le reprend au lieu d'en ouvrir un second.
            val restored = (fragmentManager.findFragmentByTag(TAG) as? SKBottomSheetDialogFragment)
                ?.takeIf { current == null }
            if (state != null) {
                val screen = state.screen as SKScreenViewProxy<*>
                val bottomSheet =
                    restored?.takeIf { it.arguments?.getLong(ScreensManager.SK_ARGUMENT_VIEW_KEY) == screen.key }
                        ?: screen.createBottomSheetFragment(
                            expanded = state.expanded,
                            skipCollapsed = state.skipCollapsed,
                            fullHeight = state.fullHeight,
                            resizeOnKeyboard = state.resizeOnKeyboard,
                        ).also {
                            restored?.dismissAllowingStateLoss()
                            it.show(this@SKBottomSheetView.fragmentManager, TAG)
                        }
                bottomSheet.setOnDismissListener {
                    if (proxy.state == state) {
                        proxy.state = null
                    }
                    state.onDismiss?.invoke()
                }
                current = State(state, bottomSheet)
            } else {
                restored?.dismissAllowingStateLoss()
                current = null
            }
        }
    }

    private companion object {
        const val TAG = "SkBottomSheet"
    }
}
