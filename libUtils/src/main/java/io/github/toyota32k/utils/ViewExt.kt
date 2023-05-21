@file:Suppress("unused")

package io.github.toyota32k.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Parcel
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.math.roundToInt

inline fun <reified T> Context.findSpecialContext() : T? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is T) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    return null
}

fun Context.activity(): Activity? {
    return findSpecialContext()
}
//fun Context.fragment(): Fragment? {
//    return findSpecialContext()
//}

fun Context.lifecycleOwner() : LifecycleOwner? {
    return findSpecialContext()
}

fun Context.viewModelStorageOwner(): ViewModelStoreOwner? {
    return findSpecialContext()
}

fun View.activity(): Activity? {
    return context?.findSpecialContext()
}
//fun View.fragment(): Fragment? {
//    return context?.findSpecialContext()
//}
fun View.viewModelStorageOwner(): ViewModelStoreOwner? {
    return context?.findSpecialContext()
}
fun View.lifecycleOwner(): LifecycleOwner? {
    return context?.findSpecialContext()
}

fun Context.dp2px(dp:Float) : Float {
    return resources.displayMetrics.density * dp
}

fun Context.dp2px(dp:Int) : Int {
    return (resources.displayMetrics.density * dp).roundToInt()
}

fun Context.px2dp(px:Float) : Float {
    return px / resources.displayMetrics.density
}

fun Context.px2dp(px:Int) : Int {
    return px2dp(px.toFloat()).toInt()
}


fun View.setLayoutWidth(width:Int) {
    val params = layoutParams
    if(null!=params) {
        params.width = width
        layoutParams = params
    }
}

fun View.getLayoutWidth() : Int {
    return if(layoutParams?.width ?: -1 >=0) {
        layoutParams.width
    } else {
        width
    }
}

fun View.setLayoutHeight(height:Int) {
    val params = layoutParams
    if(null!=params) {
        params.height = height
        layoutParams = params
    }
}

fun View.getLayoutHeight() : Int {
    return if(layoutParams?.height ?: -1 >=0) {
        layoutParams.height
    } else {
        height
    }
}

fun View.setLayoutSize(width:Int, height:Int) {
    val params = layoutParams
    if(null!=params) {
        params.width = width
        params.height = height
        layoutParams = params
    }
}

fun View.measureAndGetSize() : Size {
    this.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    return Size(this.measuredWidth, this.measuredHeight)
}

fun View.setMargin(left:Int, top:Int, right:Int, bottom:Int) {
    val p = layoutParams as? ViewGroup.MarginLayoutParams
    if(null!=p) {
        p.setMargins(left, top, right, bottom)
        layoutParams = p
    }

}

fun Parcel.writeBool(v:Boolean) {
    writeInt(if(v) 1 else 0)
}
fun Parcel.readBool() : Boolean {
    return readInt() != 0
}

fun ListView.calcContentHeight():Int {
    val listAdapter = adapter ?: return 0
    val count = listAdapter.count
    if(count==0) return 0

    var totalHeight = 0
    for (i in 0 until count) {
        val listItem = listAdapter.getView(i, null, this)
        listItem.measure(0, 0)
        totalHeight += listItem.measuredHeight
    }
    return totalHeight + dividerHeight * (count-1)
}
fun ListView.calcFixedContentHeight():Int {
    val listAdapter = adapter ?: return 0
    if(count==0) return 0
    val listItem = listAdapter.getView(0, null, this)
    listItem.measure(0, 0)
    val itemHeight = listItem.measuredHeight
    return itemHeight * count + dividerHeight * (count-1)
}

inline fun <reified T: View> ViewGroup.listChildren():Sequence<View>
    = this.children.filter { it is T }
