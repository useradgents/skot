package tech.skot.core.components.presented

import androidx.fragment.app.Fragment
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKComponentView
import tech.skot.core.components.SKDialogFragment
import tech.skot.core.components.SKScreenViewProxy
import tech.skot.core.components.ScreensManager

class SKDialogView(
    override val proxy: SKDialogViewProxy,
    activity: SKActivity,
    fragment: Fragment?,
) : SKComponentView<Unit>(proxy, activity, fragment, Unit) {
    data class State(val state: SKDialogVC.Shown, val dialog: SKDialogFragment)

    private var current: State? = null

    fun onState(state: SKDialogVC.Shown?) {
        if (state != current?.state) {
            // Après une recréation de l'Activity, le système a déjà restauré le dialogue affiché :
            // on le reprend au lieu d'en ouvrir un second.
            val restored = (fragmentManager.findFragmentByTag(TAG) as? SKDialogFragment)
                ?.takeIf { current == null }
            if (state != null) {
                val screen = state.screen as SKScreenViewProxy<*>
                val dialog =
                    restored?.takeIf { it.arguments?.getLong(ScreensManager.SK_ARGUMENT_VIEW_KEY) == screen.key }
                        ?: screen.createDialogFragment(state.style).also {
                            restored?.dismissAllowingStateLoss()
                            it.isCancelable = state.cancelable
                            it.show(this@SKDialogView.fragmentManager, TAG)
                        }
                dialog.setOnDismissListener {
                    proxy.state = null
                    state.onDismiss?.invoke()
                }
                current = State(state, dialog)
            } else {
                current?.dialog?.dismiss()
                restored?.dismissAllowingStateLoss()
                current = null
            }
        }
    }

    private companion object {
        const val TAG = "SKDialog"
    }
}
