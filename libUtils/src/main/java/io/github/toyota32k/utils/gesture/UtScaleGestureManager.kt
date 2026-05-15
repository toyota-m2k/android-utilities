package io.github.toyota32k.utils.gesture

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.utils.UtLib
import io.github.toyota32k.utils.gesture.UtGestureInterpreter.IListenerBuilder

/**
 * ピンチによるズーム操作を簡単に実現するために、
 * ジェスチャー（UtGestureInterpreter）、対象ビュー（IUtManipulationTarget）、アクション(UtManipulationAgent) をまとめて管理するクラス
 *
 * usage
 * Activity
 *   private lateinit var scaleGestureManager: UtScaleGestureManager
 *   override fun onCreate(savedInstanceState: Bundle?) {
 *      ...
 *      scaleGestureManager = UtScaleGestureManager(this.applicationContext, true).setup(this) {
 *          onTap {
 *              ...
 *          }
 *          onDoubleTap {
 *              ...
 *          }
 *          ...
 *      }
 *   }
 */
class UtScaleGestureManager(
    applicationContext: Context,
    enableDoubleTap:Boolean,        // !rapidTap for GestureInterpreter
    val manipulationTarget: IUtManipulationTarget,
    minScale:Float = 0f, maxScale:Float = 10f,
) {
    constructor(
        enableDoubleTap:Boolean,        // !rapidTap for GestureInterpreter
        manipulationTarget: IUtManipulationTarget,
        minScale:Float = 0f, maxScale:Float = 10f,
    ) : this(UtLib.applicationContext, enableDoubleTap, manipulationTarget, minScale, maxScale)

    val gestureInterpreter = UtGestureInterpreter(applicationContext, true, !enableDoubleTap)
    val agent = UtManipulationAgent(manipulationTarget, minScale, maxScale)

    /**
     * Activity#onCreate()から呼び出す
     */
    fun setup(owner:LifecycleOwner, view:View? = null, setupMe: IListenerBuilder.()->Unit) : UtScaleGestureManager {
        gestureInterpreter.setup(owner, view ?: manipulationTarget.parentView, setupMe)
        if (gestureInterpreter.scrollListener.count==0) {
            gestureInterpreter.scrollListener.add(owner, agent::onScroll)
        }
        if (gestureInterpreter.scaleListener.count==0) {
            gestureInterpreter.scaleListener.add(owner, agent::onScale)
        }
        return this
    }
}