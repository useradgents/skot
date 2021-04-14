package tech.skot.core.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import tech.skot.view.live.MutableSKLiveData

abstract class ScreenViewProxy<B : ViewBinding> : ComponentViewProxy<B>(), ScreenVC {

    val key = ScreensManager.addScreen(this)

    private val onBackPressedLD = MutableSKLiveData<(() -> Unit)?>(null)
    override var onBackPressed by onBackPressedLD

    abstract override fun bindTo(activity: SKActivity, fragment: Fragment?, binding: B, collectingObservers:Boolean): ScreenView<B>

    open fun getActivityClass(): Class<*> = SKActivity::class.java

    fun bindTo(activity: SKActivity, fragment: Fragment?, layoutInflater: LayoutInflater): ScreenView<B> {
        val binding = inflate(layoutInflater, null, false)
        return bindTo(activity, fragment, binding).apply {
            onBackPressedLD.observe {
                setOnBackPressed(it)
            }
        }
//        return binding.root
    }


    abstract override fun inflate(layoutInflater: LayoutInflater, parent: ViewGroup?, attachToParent:Boolean): B


    fun createFragment(): SKFragment =
            SKFragment().apply {
                arguments = Bundle().apply {
                    putLong(ScreensManager.SK_ARGUMENT_VIEW_KEY, key)
                }
            }

    fun createDialogFragment(): SKBottomSheetDialogFragment =
            SKBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ScreensManager.SK_ARGUMENT_VIEW_KEY, key)
                }
            }

}