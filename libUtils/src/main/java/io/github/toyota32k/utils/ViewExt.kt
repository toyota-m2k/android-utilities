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
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.math.roundToInt

/**
 * コンテキストチェインを遡り、特定クラスのContextを探して返す。
 */
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

/**
 * Context が所属する Activity を取得する。
 */
fun Context.activity(): Activity? {
    return findSpecialContext()
}

/**
 * Context（通常はActivity）が所属する LifecycleOwner を取得する。
 */
fun Context.lifecycleOwner() : LifecycleOwner? {
    return findSpecialContext()
}

/**
 * Context（通常はActivity）が所属する ViewModelStoreOwner を取得する。
 */
fun Context.viewModelStorageOwner(): ViewModelStoreOwner? {
    return findSpecialContext()
}

/**
 * Viewが所属するActivityを取得する。
 */
fun View.activity(): Activity? {
    return context?.findSpecialContext()
}

/**
 * Viewが所属する ViewModelStoreOwner を取得する。
 */
fun View.viewModelStorageOwner(): ViewModelStoreOwner? {
    return context?.findSpecialContext()
}

/**
 * Viewが所属する LifecycleOwner を取得する。
 */
fun View.lifecycleOwner(): LifecycleOwner? {
    return context?.findSpecialContext()
}

/**
 * dpをpixelに変換する
 */
fun Context.dp2px(dp:Float) : Float {
    return resources.displayMetrics.density * dp
}

/**
 * dpをpixelに変換する
 */
fun Context.dp2px(dp:Int) : Int {
    return (resources.displayMetrics.density * dp).roundToInt()
}

/**
 * pixelをdpに変換する
 */
fun Context.px2dp(px:Float) : Float {
    return px / resources.displayMetrics.density
}

/**
 * pixelをdpに変換する
 */
fun Context.px2dp(px:Int) : Int {
    return px2dp(px.toFloat()).toInt()
}

/**
 * 例外をスローしない getString()
 */
fun Context.getStringOrNull(@StringRes id:Int) : String? {
    return try {
        getString(id)
    } catch (e: Throwable) {
        null
    }
}

fun Context.getStringOrDefault(@StringRes int: Int, default: String): String {
    return getStringOrNull(int) ?: default
}


/**
 * View の layoutParams に widthを設定する
 */
fun View.setLayoutWidth(width:Int) {
    val params = layoutParams
    if(null!=params) {
        params.width = width
        layoutParams = params
    }
}

/**
 * View の layoutParams.width を取得。
 * layoutParams が取得できなければ、width プロパティを返す
 */
fun View.getLayoutWidth() : Int {
    return if((layoutParams?.width ?: -1) >= 0) {
        layoutParams.width
    } else {
        width
    }
}

/**
 * View の layoutParams に heightを設定する
 */
fun View.setLayoutHeight(height:Int) {
    val params = layoutParams
    if(null!=params) {
        params.height = height
        layoutParams = params
    }
}

/**
 * View の layoutParams.height を取得。
 * layoutParams が取得できなければ、height プロパティを返す
 */
fun View.getLayoutHeight() : Int {
    return if((layoutParams?.height ?: -1) >= 0) {
        layoutParams.height
    } else {
        height
    }
}

/**
 * View の layoutParams にサイズ (width/height) を設定する
 */
fun View.setLayoutSize(width:Int, height:Int) {
    val params = layoutParams
    if(null!=params) {
        params.width = width
        params.height = height
        layoutParams = params
    }
}

/**
 * Viewのサイズを取得する。（取得する前に measure() を呼ぶのがミソ）
 */
fun View.measureAndGetSize() : Size {
    this.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    return Size(this.measuredWidth, this.measuredHeight)
}

/**
 * Viewにマージンを設定する
 */
fun View.setMargin(left:Int, top:Int, right:Int, bottom:Int) {
    val p = layoutParams as? ViewGroup.MarginLayoutParams
    if(null!=p) {
        p.setMargins(left, top, right, bottom)
        layoutParams = p
    }

}

/**
 * ParcelにBoolean値を書き込む
 * 。。。なんと、いつの間にか、ParcelにwriteBoolean()/readBoolean() メソッドが生えてた。API29かららしいｗ
 */
fun Parcel.writeBool(v:Boolean) {
    writeInt(if(v) 1 else 0)
}
/**
 * ParcelからBoolean値を読む
 */
fun Parcel.readBool() : Boolean {
    return readInt() != 0
}

/**
 * リストビューのコンテントの高さ（各アイテムの高さの合計）を計算する（可変アイテムサイズ用）
 */
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
/**
 * リストビューのコンテントの高さ（各アイテムの高さの合計）を計算する（固定アイテムサイズ用）
 */
fun ListView.calcFixedContentHeight():Int {
    val listAdapter = adapter ?: return 0
    if(count==0) return 0
    val listItem = listAdapter.getView(0, null, this)
    listItem.measure(0, 0)
    val itemHeight = listItem.measuredHeight
    return itemHeight * count + dividerHeight * (count-1)
}

/**
 * ViewGroupの特定クラス(T)の子ビューを列挙する
 */
inline fun <reified T: View> ViewGroup.listChildren():Sequence<View>
    = this.children.filter { it is T }
