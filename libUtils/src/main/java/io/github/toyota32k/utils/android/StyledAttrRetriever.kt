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
import kotlin.math.roundToInt

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
        return sa.getDrawable(attrId) ?: ColorDrawable(getColor(attrId, themeAttrId, fallbackThemeAttrRes, def))
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
        return sa.getDrawable(attrId) ?: ColorDrawable(getColor(attrId, themeAttrId, 0, def))
    }

    /**
     * 普通の getDrawable()
     */
    fun getDrawable(@StyleableRes attrId:Int): Drawable? {
        return sa.getDrawable(attrId)
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
        return sa.getDrawable(attrId) ?: ColorDrawable(getColorWithAlphaOnFallback(attrId, themeAttrId, fallbackThemeAttrRes, def, alpha))
    }
    fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Float): Drawable {
        return sa.getDrawable(attrId) ?: ColorDrawable(getColorWithAlphaOnFallback(attrId, themeAttrId, fallbackThemeAttrRes, def, alpha))
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
        return sa.getDrawable(attrId) ?: ColorDrawable(getColorWithAlphaOnFallback(attrId, themeAttrId, 0, def, alpha))
    }
    fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Float): Drawable {
        return sa.getDrawable(attrId) ?: ColorDrawable(getColorWithAlphaOnFallback(attrId, themeAttrId, 0, def, alpha))
    }

    data class DP(val v:Int):IDimension {
        override fun div(v: Int): IDimension {
            return DP(this.v / v)
        }

        override fun div(v: Float): IDimension {
            return DP((this.v.toFloat() / v).roundToInt())
        }
        override fun times(v: Int): IDimension {
            return DP(this.v * v)
        }

        override fun times(v: Float): IDimension {
            return DP((this.v.toFloat() * v).roundToInt())
        }
        override fun dp(context: Context):Int {
            return v
        }
        override fun px(context: Context):Int {
            return context.dp2px(v)
        }
        fun PX(context: Context):PX {
            return PX(dp(context))
        }
        operator fun plus(v: DP): DP {
            return DP(this.v + v.v)
        }
        operator fun minus(v: DP): DP {
            return DP(this.v - v.v)
        }
    }
    data class PX(val v:Int):IDimension {
        override fun div(v: Int): IDimension {
            return PX(this.v / v)
        }
        override fun div(v: Float): IDimension {
            return PX((this.v.toFloat()/v).roundToInt())
        }
        override fun times(v: Int): IDimension {
            return PX(this.v * v)
        }
        override fun times(v: Float): IDimension {
            return PX((this.v.toFloat() * v).roundToInt())
        }
        override fun dp(context: Context):Int {
            return context.px2dp(v)
        }
        override fun px(context: Context):Int {
            return v
        }
        fun DP(context: Context):DP {
            return DP(dp(context))
        }
        operator fun plus(v: PX): PX {
            return PX(this.v + v.v)
        }
        operator fun minus(v: PX): PX {
            return PX(this.v - v.v)
        }

    }

    /**
     * サイズをpx値で取得
     *
     * @param attrId カスタム属性(attrs.xmlで、declare-styleable によって定義された attr id)
     * @param def  取得できない場合に使う値（IDimension型）
     */
    fun getDimensionPixelSize(@StyleableRes attrId: Int, def: IDimension): Int {
        return sa.getDimensionPixelSize(attrId, def.px(context))
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
    fun dp(context: Context) : Int
    fun px(context: Context) : Int
}

val Int.dp get() = StyledAttrRetriever.DP(this)
val Int.px get() = StyledAttrRetriever.PX(this)
fun dp(v:Int) = StyledAttrRetriever.DP(v)
fun px(v:Int) = StyledAttrRetriever.PX(v)

@ColorInt
fun Resources.Theme.getAttrColor(@AttrRes attrId:Int, @ColorInt def:Int=0):Int {
    val typedValue = TypedValue()
    return if(this.resolveAttribute(attrId, typedValue, true)) {
        return typedValue.data
    } else def
}

fun Resources.Theme.getAttrColorAsDrawable(@AttrRes attrId:Int, @ColorInt def:Int=0): Drawable {
    return ColorDrawable(getAttrColor(attrId, def))
}


@ColorInt
fun Context.getAttrColor(@AttrRes attrId:Int, @ColorInt def:Int=0):Int {
    return this.theme.getAttrColor(attrId, def)
}

fun Context.getAttrColorAsDrawable(@AttrRes attrId:Int, @ColorInt def:Int=0): Drawable {
    return this.theme.getAttrColorAsDrawable(attrId, def)
}
