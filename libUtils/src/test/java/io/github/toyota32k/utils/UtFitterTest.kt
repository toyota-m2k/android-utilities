package io.github.toyota32k.utils

import io.github.toyota32k.utils.android.FitMode
import io.github.toyota32k.utils.android.UtFitter
import org.junit.Assert.*
import org.junit.Test

class UtFitterTest {

    @Test
    fun testFitModeFit() {
        val fitter = UtFitter(FitMode.Fit, 200f, 100f)
        fitter.fit(400f, 400f)
        assertEquals(200f, fitter.resultWidth, 0.01f)
        assertEquals(100f, fitter.resultHeight, 0.01f)
        assertEquals(0f, fitter.ratio, 0.01f)
    }

    @Test
    fun testFitModeWidth() {
        val fitter = UtFitter(FitMode.Width, 200f, 100f)
        fitter.fit(100f, 200f)
        assertEquals(200f, fitter.resultWidth, 0.01f)
        assertEquals(400f, fitter.resultHeight, 0.01f)
        assertEquals(2f, fitter.ratio, 0.01f)
        fitter.fit(400f, 200f)
        assertEquals(200f, fitter.resultWidth, 0.01f)
        assertEquals(100f, fitter.resultHeight, 0.01f)
        assertEquals(0.5f, fitter.ratio, 0.01f)
    }

    @Test
    fun testFitModeHeight() {
        val fitter = UtFitter(FitMode.Height, 200f, 100f)
        fitter.fit(100f, 50f)
        assertEquals(200f, fitter.resultWidth, 0.01f)
        assertEquals(100f, fitter.resultHeight, 0.01f)
        assertEquals(2f, fitter.ratio, 0.01f)
        fitter.fit(400f, 200f)
        assertEquals(200f, fitter.resultWidth, 0.01f)
        assertEquals(100f, fitter.resultHeight, 0.01f)
        assertEquals(0.5f, fitter.ratio, 0.01f)
    }

    @Test
    fun testFitModeInside() {
        val fitter = UtFitter(FitMode.Inside, 200f, 100f)
        fitter.fit(300f, 200f)
        assertEquals(150f, fitter.resultWidth, 0.01f)
        assertEquals(100f, fitter.resultHeight, 0.01f)
        assertEquals(0.5f, fitter.ratio, 0.01f)
        assertTrue(fitter.deflated)
        assertFalse(fitter.inflated)

        fitter.fit(400f, 50f)
        assertEquals(200f, fitter.resultWidth, 0.01f)
        assertEquals(25f, fitter.resultHeight, 0.01f)
        assertEquals(0.5f, fitter.ratio, 0.01f)
        assertTrue(fitter.deflated)
        assertFalse(fitter.inflated)

        fitter.fit(100f, 20f)
        assertEquals(200f, fitter.resultWidth, 0.01f)
        assertEquals(40f, fitter.resultHeight, 0.01f)
        assertEquals(2f, fitter.ratio, 0.01f)
        assertFalse(fitter.deflated)
        assertTrue(fitter.inflated)

        fitter.fit(25f, 50f)
        assertEquals(50f, fitter.resultWidth, 0.01f)
        assertEquals(100f, fitter.resultHeight, 0.01f)
        assertEquals(2f, fitter.ratio, 0.01f)
        assertFalse(fitter.deflated)
        assertTrue(fitter.inflated)

    }
}
