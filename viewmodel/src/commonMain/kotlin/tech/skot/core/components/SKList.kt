package tech.skot.core.components

import tech.skot.core.di.coreViewInjector

/**
 * [SKComponent] used to show a list or a grid
 * @param layoutMode [SKListVC.LayoutMode] specify if the list should scroll vertically or horizontally, vertical by default
 * @param reverse [Boolean] direction of the list
 * @param animate [Boolean]
 * @param animateItem [Boolean]
 * @param infiniteScroll [Boolean]
 *
 */
open class SKList(
    layoutMode: SKListVC.LayoutMode = SKListVC.LayoutMode.Linear(true),
    reverse: Boolean = false,
    animate: Boolean = true,
    animateItem: Boolean = false,
    infiniteScroll: Boolean = false,
) : SKComponent<SKListVC>() {
    constructor(vertical: Boolean) : this(layoutMode = SKListVC.LayoutMode.Linear(vertical = vertical))

    override val view = coreViewInjector.skList(layoutMode, reverse, animate, animateItem, infiniteScroll)

    var items: List<SKComponent<*>> = emptyList()
        set(value) {
            view.items = value.map { Triple(it.view, it.computeItemId(), it.onSwipe) }
            field.forEach { if (!value.contains(it)) it.onRemove() }
            field = value
        }

    fun scrollToPosition(position: Int) {
        view.scrollToPosition(position, mode = SKListVC.ScrollMode.StartToStart)
    }

    fun scrollToStart(item: SKComponent<*>) {
        view.scrollToPosition(items.indexOf(item), mode = SKListVC.ScrollMode.StartToStart)
    }

    fun showAll(item: SKComponent<*>) {
        view.scrollToPosition(items.indexOf(item), mode = SKListVC.ScrollMode.Visible)
    }

    fun center(item: SKComponent<*>) {
        view.scrollToPosition(items.indexOf(item), mode = SKListVC.ScrollMode.Center)
    }

    override fun onRemove() {
        super.onRemove()
        items.forEach {
            it.onRemove()
        }
    }
}
