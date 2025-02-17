package tech.skot.core.components

import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import tech.skot.view.SKTransitionAndroidLegacy
import tech.skot.view.live.MutableSKLiveData

data class StateProxy(override val screens: List<SKScreenViewProxy<*>>, override val transition: SKTransitionAndroidLegacy?) : SKStackVC.State(
    screens,
    transition,
)

class SKStackViewProxy() : SKComponentViewProxy<FrameLayout>(), SKStackVC {
    private val lastScreenLD: MutableSKLiveData<Pair<SKScreenViewProxy<*>, SKTransitionAndroidLegacy?>?> = MutableSKLiveData(null)
    override var state: SKStackVC.State = StateProxy(emptyList(), null)
        set(newVal) {
            @Suppress("UNCHECKED_CAST")
            val newProxyList = newVal.screens as List<SKScreenViewProxy<*>>
            lastScreenLD.value?.let {
                if (newProxyList.lastOrNull() != it && newProxyList.contains(it.first)) {
                    it.first.saveState()
                }
            }
            lastScreenLD.postValue(newProxyList.lastOrNull()?.let { Pair(it, newVal.transition as? SKTransitionAndroidLegacy) })
            field = state
        }

    override fun saveState() {
        lastScreenLD.value?.first?.saveState()
    }

    override fun bindTo(
        activity: SKActivity,
        fragment: Fragment?,
        binding: FrameLayout,
    ) = SKStackView(this, activity, fragment, binding).apply {
        lastScreenLD.observe {
            onLastScreen(it)
        }
    }
}
