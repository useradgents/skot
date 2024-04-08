package tech.skot.core.components

import androidx.fragment.app.Fragment
import tech.skot.view.SKTransitionAndroidLegacy
import tech.skot.view.live.MutableSKLiveData

object SKRootStackViewProxy : SKComponentViewProxy<Unit>(), SKStackVC {
    val stateLD: MutableSKLiveData<StateProxy> =
        MutableSKLiveData(StateProxy(emptyList(), null))

    override var state: SKStackVC.State = StateProxy(screens = emptyList(), transition = null)
        set(newVal) {
            @Suppress("UNCHECKED_CAST")
            val newProxyList = newVal.screens as List<SKScreenViewProxy<*>>
            field = newVal
            stateLD.postValue(
                StateProxy(
                    screens = newProxyList,
                    transition = newVal.transition as SKTransitionAndroidLegacy?,
                ),
            )
        }

    @Throws(java.lang.IllegalAccessException::class)
    override fun bindTo(
        activity: SKActivity,
        fragment: Fragment?,
        binding: Unit,
    ): SKComponentView<Unit> {
        throw IllegalAccessException("On ne bind pas la RootStack")
    }
}
