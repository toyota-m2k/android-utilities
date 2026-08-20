@file:Suppress("unused")

package io.github.toyota32k.utils.android

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.core.content.res.getColorOrThrow
import io.github.toyota32k.utils.UtLib
import kotlin.math.roundToInt
import androidx.core.graphics.drawable.toDrawable
import io.github.toyota32k.utils.UtLib.applicationContext

/**
 * StyledAttributeのラッパークラス
 * - recycle() をAutoCloseableによって自動化
 * - 色、サイズの取得の面倒な処理を隠蔽＋ロジックの共通化
 *
 * 使い方：
 * StyledAttrRetriever(context, attrs, R.styleable.ControlPanel, defStyleAttr, 0).use { sar ->
 *  sar.getColor(...)
 * }
 */
class StyledAttrRetriever(private val context: Context, @Suppress("MemberVisibilityCanBePrivate") val sa: TypedArray) : AutoCloseable {
    constructor(context: Context, attrs: AttributeSet?, @StyleableRes attrRes: IntArray, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes:Int)
            : this(context, context.theme.obtainStyledAttributes(attrs, attrRes, defStyleAttr, defStyleRes))

    private val typedValue = TypedValue()

    /**
     * カスタム属性(attrId) --> テーマ色(Material3推奨) --> 第２希望（Material2 など） --> デフォルト色 の順に利用可能な色を取得する
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param fallbackThemeAttrRes 第２希望（Material2 など）... Material3 の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     */
    @ColorInt
    fun getColor(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int): Int {
        return try {
            sa.getColorOrThrow(attrId)
        } catch (e: Throwable) {
            if(context.theme.resolveAttribute(themeAttrId, typedValue, true)) {
                typedValue.data
            } else if(fallbackThemeAttrRes!=0 && context.theme.resolveAttribute(fallbackThemeAttrRes, typedValue, true)) {
                typedValue.data
            } else {
                def
            }
        }
    }
    /**
     * カスタム属性(attrId) --> テーマ色(Material3推奨) --> デフォルト色 の順に利用可能な色を取得する
     * （fallbackThemeAttrRes を使わないバージョン）
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     */
    @ColorInt
    fun getColor(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int): Int {
        return getColor(attrId, themeAttrId, 0, def)
    }

    /**
     * カスタム属性(attrId) --> テーマ色(Material3推奨) --> 第２希望（Material2 など） --> デフォルト色 の順に利用可能な色を取得する
     * カスタム属性が見つかったときは、それをそのまま返すが、それ以外（テーマ色など）を返す場合は、指定されたアルファ値を付与する。
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param fallbackThemeAttrRes 第２希望（Material2 など）... Material3 の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     * @param alpha カスタム属性以外の場合に設定するアルファ値(0..0xFF)
     */
    @ColorInt
    fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Int): Int {
        return try {
            sa.getColorOrThrow(attrId)
        } catch (e: Throwable) {
            (if(context.theme.resolveAttribute(themeAttrId, typedValue, true)) {
                typedValue.data
            } else if(fallbackThemeAttrRes!=0 && context.theme.resolveAttribute(fallbackThemeAttrRes, typedValue, true)) {
                typedValue.data
            } else {
                def
            }).withAlpha(alpha)
        }
    }
    @ColorInt
    fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Float): Int {
        return getColorWithAlphaOnFallback(attrId, themeAttrId, fallbackThemeAttrRes, def, (alpha*255).roundToInt())
    }
    /**
     * カスタム属性(attrId) --> テーマ色(Material3推奨) --> 第２希望（Material2 など） --> デフォルト色 の順に利用可能な色を取得する
     * カスタム属性が見つかったときは、それをそのまま返すが、それ以外（テーマ色など）を返す場合は、指定されたアルファ値を付与する。
     * （fallbackThemeAttrRes を使わないバージョン）
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     * @param alpha カスタム属性以外の場合に設定するアルファ値(0..0xFF)
     */
    @ColorInt
    fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Int): Int {
        return getColorWithAlphaOnFallback(attrId, themeAttrId, 0, def, alpha)
    }
    @ColorInt
    fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Float): Int {
        return getColorWithAlphaOnFallback(attrId, themeAttrId, 0, def, alpha)
    }

    private fun getResourceType(@StyleableRes attrId: Int): String? {
        return sa.peekValue(attrId)?.let { tv->
            context.resources.getResourceTypeName(tv.resourceId) }
    }
    private fun getDrawableOrNull(@StyleableRes attrId: Int): Drawable? {
        return try {
            sa.getDrawable(attrId)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Drawableを取得する。（background属性など、Drawable / color のどちらでも受け取れる属性の取得に使う。
     * - カスタム属性が Drawableなら、それを返す。
     * - それ以外の場合は、getColor() で取得される色を ColorDrawableとして返す。
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param fallbackThemeAttrRes 第２希望（Material2 など）... Material3 の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     */
    fun getDrawable(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int): Drawable {
        return getDrawableOrNull(attrId) ?: getColor(attrId,themeAttrId,fallbackThemeAttrRes,def).toDrawable()
    }

    /**
     * Drawableを取得する。（background属性など、Drawable / color のどちらでも受け取れる属性の取得に使う。
     * - カスタム属性が Drawableなら、それを返す。
     * - それ以外の場合は、getColor() で取得される色を ColorDrawableとして返す。
     * （fallbackThemeAttrRes を使わないバージョン）
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     */
    fun getDrawable(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int): Drawable {
        return getDrawableOrNull(attrId) ?: getColor(attrId, themeAttrId, 0, def).toDrawable()
    }

    /**
     * 普通の getDrawable()
     */
    fun getDrawable(@StyleableRes attrId:Int): Drawable? {
        return getDrawableOrNull(attrId)
    }

    /**
     * Drawableを取得する。（background属性など、Drawable / color のどちらでも受け取れる属性の取得に使う。
     * - カスタム属性が Drawableなら、それを返す。
     * - カスタム属性が Colorなら、それを ColorDrawableにして返す。
     * - それ以外の場合は、getColor() で取得される色に、alpha値を付与して、ColorDrawableとして返す。
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param fallbackThemeAttrRes 第２希望（Material2 など）... Material3 の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     * @param alpha カスタム属性以外の場合に設定するアルファ値(0..0xFF)
     */
    fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Int): Drawable {
        return getDrawableOrNull(attrId) ?: getColorWithAlphaOnFallback(attrId,themeAttrId,fallbackThemeAttrRes,def,alpha).toDrawable()
    }
    fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Float): Drawable {
        return getDrawableOrNull(attrId) ?: getColorWithAlphaOnFallback(attrId,themeAttrId,fallbackThemeAttrRes,def,alpha).toDrawable()
    }

    /**
     * Drawableを取得する。（background属性など、Drawable / color のどちらでも受け取れる属性の取得に使う。
     * - カスタム属性が Drawableなら、それを返す。
     * - カスタム属性が Colorなら、それを ColorDrawableにして返す。
     * - それ以外の場合は、getColor() で取得される色に、alpha値を付与して、ColorDrawableとして返す。
     * （fallbackThemeAttrRes を使わないバージョン）
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param themeAttrId テーマ色(Material3推奨) ... attrId の色が定義されていない場合に使用
     * @param def  上記のどれも取得できない場合に使われる色（ちゃんと作りこんでいたら、これは使われないはず）
     * @param alpha カスタム属性以外の場合に設定するアルファ値(0..0xFF)
     */
    fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Int): Drawable {
        return getDrawableOrNull(attrId) ?: getColorWithAlphaOnFallback(attrId,themeAttrId,0,def,alpha).toDrawable()
    }
    fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Float): Drawable {
        return getDrawableOrNull(attrId) ?: getColorWithAlphaOnFallback(attrId,themeAttrId,0,def,alpha).toDrawable()
    }

    data class DP(val v:Float):IDimension {
        constructor(v:Int): this(v.toFloat())
        override fun div(v: Int): IDimension {
            return DP(this.v / v)
        }

        override fun div(v: Float): IDimension {
            return DP(this.v / v)
        }
        override fun times(v: Int): IDimension {
            return DP(this.v * v)
        }

        override fun times(v: Float): IDimension {
            return DP(this.v * v)
        }
        override fun dpf():Float {
            return v
        }
        override fun dp():Int {
            return v.roundToInt()
        }
        override fun pxf():Float {
            return UtLib.applicationContext.dp2px(v)
        }
        override fun px():Int {
            return UtLib.applicationContext.dp2px(v).roundToInt()
        }
        fun PX():PX {
            return PX(pxf())
        }
        override operator fun plus(v: IDimension): IDimension {
            return DP(this.v + v.dpf())
        }
        override operator fun minus(v: IDimension): IDimension {
            return DP(this.v - v.dpf())
        }
    }
    data class PX(val v:Float):IDimension {
        constructor(v:Int):this(v.toFloat())
        override fun div(v: Int): IDimension {
            return PX(this.v / v)
        }
        override fun div(v: Float): IDimension {
            return PX(this.v/v)
        }
        override fun times(v: Int): IDimension {
            return PX(this.v * v)
        }
        override fun times(v: Float): IDimension {
            return PX(this.v * v)
        }
        override fun dpf():Float {
            return UtLib.applicationContext.px2dp(v)
        }
        override fun dp():Int {
            return UtLib.applicationContext.px2dp(v).roundToInt()
        }
        override fun pxf():Float {
            return v
        }
        override fun px():Int {
            return v.roundToInt()
        }
        fun DP():DP {
            return DP(dpf())
        }
        override operator fun plus(v: IDimension): IDimension {
            return PX(this.v + v.pxf())
        }
        override operator fun minus(v: IDimension): IDimension {
            return PX(this.v - v.pxf())
        }
    }

    /**
     * サイズをpx値で取得
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param def  取得できない場合に使う値（IDimension型）
     */
    fun getDimensionPixelSize(@StyleableRes attrId: Int, def: IDimension): Int {
        return sa.getDimensionPixelSize(attrId, def.px())
    }

    override fun close() {
        sa.recycle()
    }
}

/**
 * pxとdpを区別しつつ同様に扱えるようにするための仕掛け
 * デザインするときは dp を使うが、内部では px を使わないといけなくて、しばしば、これを取り違えてレイアウトがメタクソになってしまう。
 * コードを見ただけで、この数字がdpかpxか、区別がつくようにしたい。
 * 1.px とか、2.dp のように書けると便利じゃね？
 */
interface IDimension {
    operator fun div(v:Int):IDimension
    operator fun div(v:Float): IDimension
    operator fun times(v:Int):IDimension
    operator fun times(v:Float): IDimension
    operator fun unaryMinus():IDimension = times(-1)
    fun dp() : Int
    fun dpf() : Float
    fun px() : Int
    fun pxf() : Float
    operator fun plus(v:IDimension):IDimension
    operator fun minus(v: IDimension): IDimension
}

val Int.dp get() = StyledAttrRetriever.DP(this)
val Int.px get() = StyledAttrRetriever.PX(this)
val Float.dp get() = StyledAttrRetriever.DP(this)
val Float.px get() = StyledAttrRetriever.PX(this)

//fun dp(v:Int) = StyledAttrRetriever.DP(v)
//fun px(v:Int) = StyledAttrRetriever.PX(v)

@ColorInt
fun Resources.Theme.getAttrColor(@AttrRes attrId:Int, @ColorInt def:Int=0):Int {
    val typedValue = TypedValue()
    return if(this.resolveAttribute(attrId, typedValue, true)) {
        typedValue.data
    } else def
}

fun Resources.Theme.getAttrColorAsDrawable(@AttrRes attrId:Int, @ColorInt def:Int=0): Drawable {
    return getAttrColor(attrId, def).toDrawable()
}


@ColorInt
fun Context.getAttrColor(@AttrRes attrId:Int, @ColorInt def:Int=0):Int {
    return this.theme.getAttrColor(attrId, def)
}

fun Context.getAttrColorAsDrawable(@AttrRes attrId:Int, @ColorInt def:Int=0): Drawable {
    return this.theme.getAttrColorAsDrawable(attrId, def)
}
