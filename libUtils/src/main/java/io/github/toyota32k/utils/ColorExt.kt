package io.github.toyota32k.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlin.math.roundToInt

data class RGB(val r:Int, val g:Int, val b:Int, val a:Int) {
    constructor(@ColorInt rgba: Int) : this(Color.red(rgba), Color.green(rgba), Color.blue(rgba), Color.alpha(rgba))
    constructor(rgba: Long) : this(rgba.toInt())

    @ColorInt
    fun toColor() : Int {
        return Color.argb(a, r, g, b)
    }

    fun brend(other: RGB) : RGB {
        return RGB(
            (r + other.r) / 2,
            (g + other.g) / 2,
            (b + other.b) / 2,
            255
        )
    }

    companion object {
        fun opaque(@ColorInt rgba: Int) : RGB {
            return RGB(Color.red(rgba), Color.green(rgba), Color.blue(rgba), 255)
        }
        fun opaque(rgba: Long) : RGB {
            return opaque(rgba.toInt())
        }

        val black = RGB(0, 0, 0, 255)
        val white = RGB(255, 255, 255, 255)

        @ColorInt
        fun darken(@ColorInt rgba:Int) : Int {
            return RGB(rgba).brend(black).toColor()
        }
        @ColorInt
        fun darken(rgba:Long) : Int {
            return RGB(rgba).brend(black).toColor()
        }
        @ColorInt
        fun lighten(@ColorInt rgba:Int) : Int {
            return RGB(rgba).brend(white).toColor()
        }
        @ColorInt
        fun lighten(rgba:Long) : Int {
            return RGB(rgba).brend(white).toColor()
        }
        @ColorInt
        fun brend(@ColorInt rgba1:Int, @ColorInt rgba2:Int) : Int {
            return RGB(rgba1).brend(RGB(rgba2)).toColor()
        }
        @ColorInt
        fun brend(rgba1:Long, rgba2:Long) : Int {
            return RGB(rgba1).brend(RGB(rgba2)).toColor()
        }
    }
}

/**
 * ColorIntにアルファ値を付与
 */
@ColorInt
fun @receiver:ColorInt Int.withAlpha(alpha:Int) : Int  {
    require(alpha in 0..255) { "alpha must be between 0 and 255" }
    return this and 0x00ffffff or (alpha shl 24)
}
@ColorInt
fun @receiver:ColorInt Int.withAlpha(alpha:Float) : Int  {
    require(alpha in 0f..1f) { "alpha must be between 0 and 255" }
    return this and 0x00ffffff or ((255 * alpha).roundToInt() shl 24)
}

@ColorInt
fun Long.colorWithAlpha(alpha:Int) : Int {
    require(alpha in 0..255) { "alpha must be between 0 and 255" }
    return toInt() and 0x00ffffff or (alpha shl 24)
}
@ColorInt
fun Long.colorWithAlpha(alpha:Float) : Int {
    require(alpha in 0f..1f) { "alpha must be between 0 and 255" }
    return toInt() and 0x00ffffff or ((alpha*255).roundToInt() shl 24)
}

@ColorInt
fun Int.opaqueColor() : Int {
    return this or 0xff000000.toInt()
}

