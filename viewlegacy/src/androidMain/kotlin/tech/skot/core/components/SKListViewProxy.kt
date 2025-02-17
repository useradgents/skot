package tech.skot.core.components

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import tech.skot.view.live.MutableSKLiveData
import tech.skot.view.live.SKMessage
import tech.skot.viewlegacy.R

open class SKListViewProxy(
    private val layoutMode: SKListVC.LayoutMode = SKListVC.LayoutMode.Linear(true),
    private val reverse: Boolean = false,
    private val animate: Boolean = true,
    private val animateItem: Boolean = false,
    private val infiniteScroll: Boolean = false,
) : SKComponentViewProxy<RecyclerView>(), SKListVC {
    private val itemsLD: MutableSKLiveData<List<Triple<SKComponentViewProxy<*>, Any, (() -> Unit)?>>> = MutableSKLiveData(emptyList())

    override var items: List<Triple<SKComponentVC, Any, (() -> Unit)?>>
        get() = itemsLD.value
        set(newVal) {
            @Suppress("UNCHECKED_CAST")
            itemsLD.postValue(newVal as List<Triple<SKComponentViewProxy<*>, Any, (() -> Unit)?>>)
        }

    data class ScrollRequest(val position: Int, val mode: SKListVC.ScrollMode)

    private val srollToPositionMessage = SKMessage<ScrollRequest>()

    override fun scrollToPosition(
        position: Int,
        mode: SKListVC.ScrollMode,
    ) {
        srollToPositionMessage.post(ScrollRequest(position, mode))
    }

    private val saveSignal: SKMessage<Unit> = SKMessage()
    private var _state: Parcelable? = null

    override fun saveState() {
        saveSignal.post(Unit)
    }

    override val layoutId = R.layout.sk_list

    override fun bindTo(
        activity: SKActivity,
        fragment: Fragment?,
        binding: RecyclerView,
    ) = SKListView(layoutMode, reverse, animate, animateItem, infiniteScroll, this, activity, fragment, binding).apply {
        itemsLD.observe {
            onItems(it)
        }
        saveSignal.observe {
            _state = saveState()
        }
        srollToPositionMessage.observe {
            scrollToPosition(it)
        }
        _state?.let {
            restoreState(it)
        }
    }
}
