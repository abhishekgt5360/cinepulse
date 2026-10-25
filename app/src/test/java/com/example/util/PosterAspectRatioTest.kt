package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PosterAspectRatioTest {

    @Test
    fun testAspectClassifier() {
        // Portrait (ratio <= 0.85)
        assertEquals(ImageAspectRatioCategory.PORTRAIT, PosterAspectCache.classify(0.666f)) // Standard 2:3
        assertEquals(ImageAspectRatioCategory.PORTRAIT, PosterAspectCache.classify(0.70f))
        assertEquals(ImageAspectRatioCategory.PORTRAIT, PosterAspectCache.classify(0.85f))

        // Squareish (0.85 < ratio <= 1.15)
        assertEquals(ImageAspectRatioCategory.SQUAREISH, PosterAspectCache.classify(0.90f))
        assertEquals(ImageAspectRatioCategory.SQUAREISH, PosterAspectCache.classify(1.00f)) // 1:1 Square
        assertEquals(ImageAspectRatioCategory.SQUAREISH, PosterAspectCache.classify(1.15f))

        // Wide (ratio > 1.15)
        assertEquals(ImageAspectRatioCategory.WIDE, PosterAspectCache.classify(1.20f))
        assertEquals(ImageAspectRatioCategory.WIDE, PosterAspectCache.classify(1.777f)) // 16:9 Landscape
        assertEquals(ImageAspectRatioCategory.WIDE, PosterAspectCache.classify(2.35f)) // Cinematic ultrawide

        // Null / invalid defaults to PORTRAIT
        assertEquals(ImageAspectRatioCategory.PORTRAIT, PosterAspectCache.classify(null))
        assertEquals(ImageAspectRatioCategory.PORTRAIT, PosterAspectCache.classify(0f))
        assertEquals(ImageAspectRatioCategory.PORTRAIT, PosterAspectCache.classify(-1f))
    }
}
