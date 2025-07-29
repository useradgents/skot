package tech.skot.core.components

import android.app.ActivityManager
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ScrollView
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import tech.skot.core.view.Color
import tech.skot.core.view.DimenDP
import tech.skot.view.extensions.systemBars
import kotlin.math.max

abstract class SKScreenView<B : ViewBinding>(
    override val proxy: SKScreenViewProxy<B>,
    activity: SKActivity,
    fragment: Fragment?,
    binding: B,
) : SKComponentView<B>(proxy, activity, fragment, binding) {
    val view: View = binding.root

    private val globalFocusChangeListener: ViewTreeObserver.OnGlobalFocusChangeListener by lazy {
        ViewTreeObserver.OnGlobalFocusChangeListener { _, newFocus ->
            newFocus?.let { focusedView ->
                focusedView.postDelayed({
                    // On récupère les "insets" (informations sur la fenêtre) au moment du focus
                    val insets =
                        ViewCompat.getRootWindowInsets(binding.root) ?: return@postDelayed
                    val imeHeight =
                        insets.getInsets(WindowInsetsCompat.Type.ime()).bottom + insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        ).bottom

                    // Si le clavier n'est pas visible, on ne fait rien
                    if (imeHeight == 0) {
                        return@postDelayed
                    }

                    val viewRect = Rect()
                    focusedView.getGlobalVisibleRect(viewRect)

                    val visibleScreenHeight = binding.root.height - imeHeight

                    val scrollAmount = viewRect.bottom - visibleScreenHeight

                    val finalScroll = scrollAmount - DimenDP(8).dp
                    onKeyboardVisibilityChange?.onFocusChange(finalScroll)

                }, 200)
            }
        }
    }

    private var onBackPressed: (() -> Unit)? = null

    fun setOnBackPressed(onBackPressed: (() -> Unit)?) {
        this.onBackPressed = onBackPressed
    }

    protected val originalPaddingTop = view.paddingTop

    @CallSuper
    open fun onResume() {
        onKeyboardVisibilityChange.let {
            ViewCompat.setOnApplyWindowInsetsListener(view) { view, windowInsets ->
                val imeHeight = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom +   windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

                it?.onKeyboardVisibilityChange(imeHeight- (onKeyboardVisibilityChange?.view?.bottom?.let { binding.root.bottom - it }
                    ?: 0))
                windowInsets
            }
            (onKeyboardVisibilityChange as? KeyboardAdjustDefault?)?.let {
                binding.root.viewTreeObserver.addOnGlobalFocusChangeListener(
                    globalFocusChangeListener
                )
            }
        }
        if (fragment !is DialogFragment) {
            if (secured) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            if (fragment?.arguments?.getBoolean(ScreensManager.SK_ARGUMENT_CAN_SET_FULL_SCREEN) != false) {
                activity.setFullScreen(
                    fullScreen,
                    proxy.statusBarColor,
                    lightStatusBar,
                    onWindowInset ?: (if (withWindowsInsetsPaddingTop) {
                        {
                            view.updatePadding(top = originalPaddingTop + it.systemBars().top)
                        }
                    } else {
                        null
                    }),
                )
            }
        }
        // onStatusBarColor(proxy.statusBarColor)
        proxy.onResume()
    }

    @CallSuper
    open fun onPause() {
        (onKeyboardVisibilityChange as? KeyboardAdjustDefault)?.let {
            binding.root.viewTreeObserver.removeOnGlobalFocusChangeListener(
                globalFocusChangeListener
            )
        }
        proxy.onPause()
    }

    open val fullScreen: Boolean = false
    open val lightStatusBar: Boolean? = null
    open val secured: Boolean = false


    fun onStatusBarColor(color: Color?) {
//        if (fragment !is DialogFragment) {
//            activity.window.statusBarColor =
//                color?.toColor(context) ?: activity.statusBarColor
//        }
    }

    fun onExit() {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val appTasks = am?.getAppTasks()
        if (appTasks?.isNotEmpty() == true) {
            val appTask = appTasks[0]
            appTask.finishAndRemoveTask()
        }
    }

    open val onKeyboardVisibilityChange: KeyboardAdjust? = null


    interface KeyboardAdjust {
        val view: View
        val onKeyboardVisibilityChange: ((Int) -> Unit)
        val onFocusChange: ((Int) -> Unit)
    }

    class KeyboardAdjustDefault(
        override val view: View,
    ) : KeyboardAdjust {
        override val onKeyboardVisibilityChange: ((Int) -> Unit) = { bottomPadding ->
            view.updatePadding(bottom = bottomPadding)
        }

        override val onFocusChange: ((Int) -> Unit) = { scroll ->
            when (view) {
                is RecyclerView -> {
                    view.smoothScrollBy(0, scroll)
                }

                is NestedScrollView -> {
                    view.smoothScrollBy(0, scroll)
                }

                is ScrollView -> {
                    view.smoothScrollBy(0, scroll)
                }
            }
        }
    }


    protected open val withWindowsInsetsPaddingTop: Boolean = false

    open val onWindowInset: ((windowInsets: WindowInsetsCompat) -> Unit)? = null

    init {
        ScreensManager.backPressed.observe(lifecycleOwner = lifecycleOwner) {
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                onBackPressed?.invoke()
            }
        }
    }
}
