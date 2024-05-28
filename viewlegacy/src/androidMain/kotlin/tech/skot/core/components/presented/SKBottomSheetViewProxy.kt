package tech.skot.core.components.presented

import androidx.fragment.app.Fragment
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKComponentViewProxy
import tech.skot.view.live.MutableSKLiveData

class SKBottomSheetViewProxy() : SKComponentViewProxy<Unit>(), SKBottomSheetVC {
    private val stateLD = MutableSKLiveData<SKBottomSheetVC.Shown?>(null)

    override var state: SKBottomSheetVC.Shown? by stateLD

    override fun bindTo(
        activity: SKActivity,
        fragment: Fragment?,
        binding: Unit,
    ) = SKBottomSheetView(this, activity, fragment).apply {
        stateLD.observe {
            onState(it)
        }
    }
}
