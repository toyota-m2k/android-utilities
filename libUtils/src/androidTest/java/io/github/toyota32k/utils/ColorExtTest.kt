package io.github.toyota32k.utils

import android.graphics.Color
import io.github.toyota32k.utils.android.RGB
import io.github.toyota32k.utils.android.colorWithAlpha
import io.github.toyota32k.utils.android.opaqueColor
import io.github.toyota32k.utils.android.withAlpha
import org.junit.Assert
import org.junit.Test

class ColorExtTest {

    @Test
    fun testRGBConstructors() {
        // Colorでの値検証
        val color = Color.argb(200, 100, 150, 200)
        val rgb = RGB(color)

        Assert.assertEquals(100, rgb.r)
        Assert.assertEquals(150, rgb.g)
        Assert.assertEquals(200, rgb.b)
        Assert.assertEquals(200, rgb.a)

        // Longからのコンストラクタ
        val longColor = 0xC8649664L // alpha=200, r=100, g=150, b=100
        val rgbFromLong = RGB(longColor)

        Assert.assertEquals(100, rgbFromLong.r)
        Assert.assertEquals(150, rgbFromLong.g)
        Assert.assertEquals(100, rgbFromLong.b)
        Assert.assertEquals(200, rgbFromLong.a)

        // 直接値を指定
        val rgbDirect = RGB(50, 100, 150, 255)
        Assert.assertEquals(50, rgbDirect.r)
        Assert.assertEquals(100, rgbDirect.g)
        Assert.assertEquals(150, rgbDirect.b)
        Assert.assertEquals(255, rgbDirect.a)
    }

    @Test
    fun testRGBToColor() {
        val rgb = RGB(100, 150, 200, 255)
        val color = rgb.toColor()

        Assert.assertEquals(Color.argb(255, 100, 150, 200), color)
    }

    @Test
    fun testRGBBrend() {
        val rgb1 = RGB(100, 100, 100, 255)
        val rgb2 = RGB(200, 200, 200, 255)

        val result = rgb1.brend(rgb2)

        Assert.assertEquals(150, result.r)
        Assert.assertEquals(150, result.g)
        Assert.assertEquals(150, result.b)
        Assert.assertEquals(255, result.a)
    }

    @Test
    fun testRGBGrayScale() {
        val rgb = RGB(100, 150, 200, 255)
        val gray = rgb.grayScale()

        // 期待値: 0.2126 * 100 + 0.7152 * 150 + 0.0722 * 200 = 143
        // 小数点以下の丸めがあるので±1程度の誤差は許容
        Assert.assertTrue(gray.r in 142..144)
        Assert.assertTrue(gray.g in 142..144)
        Assert.assertTrue(gray.b in 142..144)
        Assert.assertEquals(255, gray.a)
    }

    @Test
    fun testStaticFactoryMethods() {
        val color = Color.argb(200, 100, 150, 200)

        // opaque
        val opaque = RGB.opaque(color)
        Assert.assertEquals(100, opaque.r)
        Assert.assertEquals(150, opaque.g)
        Assert.assertEquals(200, opaque.b)
        Assert.assertEquals(255, opaque.a) // 不透明

        // darken
        val darkened = RGB.darken(color)
        Assert.assertTrue(Color.red(darkened) < 100)
        Assert.assertTrue(Color.green(darkened) < 150)
        Assert.assertTrue(Color.blue(darkened) < 200)

        // lighten
        val lightened = RGB.lighten(color)
        Assert.assertTrue(Color.red(lightened) > 100)
        Assert.assertTrue(Color.green(lightened) > 150)
        Assert.assertTrue(Color.blue(lightened) > 200)

        // brend
        val color1 = Color.argb(255, 100, 100, 100)
        val color2 = Color.argb(255, 200, 200, 200)
        val blended = RGB.brend(color1, color2)
        Assert.assertEquals(Color.argb(255, 150, 150, 150), blended)

        // grayScale
        val grayColor = RGB.grayScale(color)
        val grayValue = Color.red(grayColor)
        Assert.assertEquals(grayValue, Color.green(grayColor))
        Assert.assertEquals(grayValue, Color.blue(grayColor))
    }

    @Test
    fun testWithAlphaExtensions() {
        val color = Color.rgb(100, 150, 200) // alpha=255がデフォルト

        // Intバージョン（値で指定）
        val alpha128 = color.withAlpha(128)
        Assert.assertEquals(128, Color.alpha(alpha128))
        Assert.assertEquals(100, Color.red(alpha128))
        Assert.assertEquals(150, Color.green(alpha128))
        Assert.assertEquals(200, Color.blue(alpha128))

        // Intバージョン（float値で指定）
        val alpha50percent = color.withAlpha(0.5f)
        Assert.assertEquals(128, Color.alpha(alpha50percent)) // 0.5 * 255 = 127.5 ≒ 128 (roundToInt)

        // Longバージョン
        val longColor = 0x649BC8L // r=100, g=155, b=200
        val alphaLong = longColor.colorWithAlpha(64)
        Assert.assertEquals(64, Color.alpha(alphaLong))
        Assert.assertEquals(100, Color.red(alphaLong))
        Assert.assertEquals(155, Color.green(alphaLong))
        Assert.assertEquals(200, Color.blue(alphaLong))

        // Longバージョン（float値で指定）
        val alphaLong25percent = longColor.colorWithAlpha(0.25f)
        Assert.assertEquals(64, Color.alpha(alphaLong25percent)) // 0.25 * 255 = 63.75 ≒ 64
    }

    @Test
    fun testOpaqueColor() {
        val color = Color.argb(128, 100, 150, 200)
        val opaque = color.opaqueColor()

        Assert.assertEquals(255, Color.alpha(opaque))
        Assert.assertEquals(100, Color.red(opaque))
        Assert.assertEquals(150, Color.green(opaque))
        Assert.assertEquals(200, Color.blue(opaque))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWithAlphaOutOfRange() {
        val color = Color.rgb(100, 150, 200)
        color.withAlpha(300) // 範囲外の値でエラーが発生することを確認
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWithAlphaFloatOutOfRange() {
        val color = Color.rgb(100, 150, 200)
        color.withAlpha(1.5f) // 範囲外の値でエラーが発生することを確認
    }
}