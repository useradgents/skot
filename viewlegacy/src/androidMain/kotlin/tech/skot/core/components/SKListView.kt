package tech.skot.core.components

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import tech.skot.core.SKLog

class SKListView(
    layoutMode: SKListVC.LayoutMode,
    reverse: Boolean,
    animate: Boolean,
    animateItem: Boolean,
    val infiniteScroll: Boolean,
    proxy: SKListViewProxy,
    activity: SKActivity,
    fragment: Fragment?,
    private val recyclerView: RecyclerView,
) : SKComponentView<RecyclerView>(proxy, activity, fragment, recyclerView) {

    interface SKListViewInterface {
        fun getRealItemCount(): Int
    }

    inner class ViewHolder(idLayout: Int, parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(idLayout, parent, false)
    ) {
        var bindedOnce: Boolean = false
        var componentView: SKComponentView<*>? = null
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>(), SKListViewInterface {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(viewType, parent)

        override fun getItemViewType(position: Int): Int {
            val truePosition = getPosition(position)
            return items[truePosition].first.layoutId
                ?: throw IllegalStateException("${items[truePosition].first::class.simpleName} can't be in a recyclerview")
        }

        override fun getItemCount() = if (infiniteScroll) Int.MAX_VALUE else items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val truePosition = getPosition(position)
            items[truePosition].let { proxy ->
                val componentViewImpl =
                    proxy.first.bindToItemView(activity, fragment, holder.itemView)
                holder.componentView = componentViewImpl
                if (!holder.bindedOnce) {
                    holder.bindedOnce = true
                    componentViewImpl.onFirstBind()
                }
                mapProxyIndexComponentViewImpl[proxy.first] = componentViewImpl
            }
        }

        private fun getPosition(position: Int): Int {
            return if (infiniteScroll) {
                position % items.size
            } else {
                position
            }
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            holder.componentView?.onRecycle()
            holder.componentView = null

        }

        override fun getRealItemCount(): Int {
            return items.size
        }
    }

    private val mapProxyIndexComponentViewImpl =
        mutableMapOf<SKComponentViewProxy<*>, SKComponentView<*>>()

    private val adapter = Adapter()

    override fun onRecycle() {
        super.onRecycle()
        mapProxyIndexComponentViewImpl.values.forEach {
            it.onRecycle()
        }
    }

    var items: List<Triple<SKComponentViewProxy<*>, Any, (() -> Unit)?>> = emptyList()
        set(newVal) {
            field.forEach { proxy ->
                if (!newVal.any { it.first == proxy.first }) {
                    mapProxyIndexComponentViewImpl.remove(proxy.first)
                }
            }
            val diffCallBack = DiffCallBack(field, newVal)
            field = newVal
            DiffUtil.calculateDiff(diffCallBack, true).dispatchUpdatesTo(adapter)
        }


    init {
        when (layoutMode) {
            is SKListVC.LayoutMode.Linear -> {
                recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    if (layoutMode.vertical) LinearLayoutManager.VERTICAL else LinearLayoutManager.HORIZONTAL,
                    reverse
                )
            }

            is SKListVC.LayoutMode.Grid -> {
                recyclerView.layoutManager = GridLayoutManager(
                    context,
                    layoutMode.nbColumns,
                    if (layoutMode.vertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL,
                    reverse
                )
            }

            SKListVC.LayoutMode.Manual -> {

            }
        }

        when {
            !animate -> {
                recyclerView.itemAnimator = null
            }

            !animateItem -> {
                (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            }
        }

        recyclerView.adapter = adapter
    }


    fun onItems(items: List<Triple<SKComponentViewProxy<*>, Any, (() -> Unit)?>>) {
        this.items = items
    }

    fun saveState(): Parcelable? {
        return recyclerView.layoutManager?.onSaveInstanceState()
    }

    fun restoreState(state: Parcelable) {
        recyclerView.layoutManager?.onRestoreInstanceState(state)
    }


    private class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int,
        ): Int = (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
    }

    fun scrollToPosition(scrollRequest: SKListViewProxy.ScrollRequest) {
        when (scrollRequest.mode) {
            SKListVC.ScrollMode.StartToStart -> {
                binding.post {
                    (binding.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                        scrollRequest.position,
                        0
                    )
                }
            }

            SKListVC.ScrollMode.Visible -> {
                if (adapter.itemCount > scrollRequest.position) {
                    binding.smoothScrollToPosition(scrollRequest.position)
                }
            }

            SKListVC.ScrollMode.Center -> {
                (binding.layoutManager as? LinearLayoutManager)?.apply {
                    val centerSmotthScroller = CenterSmoothScroller(recyclerView.context)
                    centerSmotthScroller.targetPosition = scrollRequest.position
                    startSmoothScroll(centerSmotthScroller)
                }
            }
        }

    }


    class DiffCallBack(
        private val oldList: List<Triple<SKComponentViewProxy<*>, Any, (() -> Unit)?>>,
        private val newList: List<Triple<SKComponentViewProxy<*>, Any, (() -> Unit)?>>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.first.layoutId == newItem.first.layoutId && oldItem.second == newItem.second
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].first == newList[newItemPosition].first
        }

    }
}

abstract class SKListItemTouchHelperCallBack(private val listView: SKListView) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listView.items[viewHolder.absoluteAdapterPosition].third?.invoke()
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        val position = viewHolder.bindingAdapterPosition
        val item = if (position >= 0) listView.items.getOrNull(position) else null
        if (item == null) {
            SKLog.e(IllegalStateException("getSwipeDirs item null"), "getSwipeDirs item null")
        }
        return if (item?.third != null) {
            super.getSwipeDirs(recyclerView, viewHolder)
        } else {
            0
        }
    }

}