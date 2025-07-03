package io.github.toyota32k.utils.gesture

import android.view.View
import java.util.EnumSet

/**
 * parentView, contentView が動的に変化しない単純な ManipulationTarget
 * ページングも不要な場合は、UtAbstractManipulationTargetまたは、UtMinimumManipulationTarget を利用する。
 */
@Suppress("unused")
open class UtSimpleManipulationTarget(
    override val parentView: View,
    override val contentView: View,
    override val overScrollX: Float = 0f,
    override val overScrollY: Float = 0f,
    override val pageOrientation: EnumSet<Orientation> = EnumSet.noneOf(Orientation::class.java)
) : IUtManipulationTarget {
    interface IUtManipulationTargetCallbacks {
        fun changePage(fn: (orientation: Orientation, dir: Direction) -> Boolean)
        fun hasNextPage(fn: (orientation: Orientation, dir: Direction) -> Boolean)
    }

    private class Callbacks : IUtManipulationTargetCallbacks {
        var changePageProc:((orientation: Orientation, dir: Direction) -> Boolean)? = null
        var hasNextPageProc:((orientation: Orientation, dir: Direction) -> Boolean)? = null
        override fun changePage(fn: (orientation: Orientation, dir: Direction) -> Boolean) {
            changePageProc = fn
        }

        override fun hasNextPage(fn: (orientation: Orientation, dir: Direction) -> Boolean) {
            hasNextPageProc = fn
        }
    }
    private var mCallbacks:Callbacks? = null
    fun callbacks(fn:IUtManipulationTargetCallbacks.()->Unit):UtSimpleManipulationTarget {
        mCallbacks = Callbacks().apply {
            fn()
        }
        return this
    }

    override fun changePage(orientation: Orientation, dir: Direction): Boolean {
        return mCallbacks?.changePageProc?.invoke(orientation, dir) ?: false
    }

    override fun hasNextPage(orientation: Orientation, dir: Direction): Boolean {
        return mCallbacks?.hasNextPageProc?.invoke(orientation,dir) ?: false
    }
}

