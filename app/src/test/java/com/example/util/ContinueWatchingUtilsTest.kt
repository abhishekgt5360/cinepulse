package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinueWatchingUtilsTest {

    @Test
    fun testFormatRemaining() {
        assertEquals("Less than a minute left", ContinueWatchingUtils.formatRemaining(119_000L, 120_000L))
        assertEquals("48m remaining", ContinueWatchingUtils.formatRemaining(12_000_000L, 14_880_000L))
        assertEquals("1h 12m remaining", ContinueWatchingUtils.formatRemaining(3_600_000L, 7_920_000L))
        assertEquals("2h remaining", ContinueWatchingUtils.formatRemaining(0L, 7_200_000L))
        assertEquals("", ContinueWatchingUtils.formatRemaining(0L, 0L))
        assertEquals("", ContinueWatchingUtils.formatRemaining(10_000L, 0L))
        assertEquals("", ContinueWatchingUtils.formatRemaining(50_000L, 10_000L)) // Position > duration
    }

    @Test
    fun testCalculateProgressFraction() {
        assertEquals(0.5f, ContinueWatchingUtils.calculateProgressFraction(50_000L, 100_000L), 0.001f)
        assertEquals(1.0f, ContinueWatchingUtils.calculateProgressFraction(150_000L, 100_000L), 0.001f)
        assertEquals(0.0f, ContinueWatchingUtils.calculateProgressFraction(-10_000L, 100_000L), 0.001f)
        assertEquals(0.0f, ContinueWatchingUtils.calculateProgressFraction(50_000L, 0L), 0.001f)
    }

    @Test
    fun testFormatTitle() {
        assertEquals("Lupin S1:E1", ContinueWatchingUtils.formatTitle("Lupin", 1, 1))
        assertEquals("Batman", ContinueWatchingUtils.formatTitle("Batman", 0, 0))
    }
}
