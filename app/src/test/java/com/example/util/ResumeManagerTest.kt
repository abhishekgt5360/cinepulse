package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeManagerTest {

    @Test
    fun testFreshItemStartsAtZero() {
        val startPos = ResumeManager.calculateStartPosition(positionMs = 0L, durationMs = 1_200_000L, finished = false)
        assertEquals(0L, startPos)
    }

    @Test
    fun testValidProgressResumes() {
        val startPos = ResumeManager.calculateStartPosition(positionMs = 300_000L, durationMs = 1_200_000L, finished = false)
        assertEquals(300_000L, startPos)
    }

    @Test
    fun testUnderTenSecondsStartsAtZero() {
        val startPos = ResumeManager.calculateStartPosition(positionMs = 8_000L, durationMs = 1_200_000L, finished = false)
        assertEquals(0L, startPos)
    }

    @Test
    fun testLastThirtySecondsOrOver95PercentStartsAtZero() {
        // Last 30 seconds (1,180,000 / 1,200,000)
        val posOffset = ResumeManager.calculateStartPosition(positionMs = 1_180_000L, durationMs = 1_200_000L, finished = false)
        assertEquals(0L, posOffset)

        // 96% finished (1,152,000 / 1,200,000)
        val posPercent = ResumeManager.calculateStartPosition(positionMs = 1_152_000L, durationMs = 1_200_000L, finished = false)
        assertEquals(0L, posPercent)
    }

    @Test
    fun testInvalidOrNegativeValuesIgnored() {
        // Negative position
        assertEquals(0L, ResumeManager.calculateStartPosition(positionMs = -500L, durationMs = 1_200_000L, finished = false))

        // Position greater than duration
        assertEquals(0L, ResumeManager.calculateStartPosition(positionMs = 1_500_000L, durationMs = 1_200_000L, finished = false))
    }

    @Test
    fun testEpisodesHaveSeparateKeys() {
        val keyEp1 = ResumeManager.calculateKey(subjectId = "show_456", season = 1, episode = 1, isSeries = true)
        val keyEp2 = ResumeManager.calculateKey(subjectId = "show_456", season = 1, episode = 2, isSeries = true)

        assertEquals("show_456_s1_e1", keyEp1)
        assertEquals("show_456_s1_e2", keyEp2)
        assertNotEquals(keyEp1, keyEp2)
    }

    @Test
    fun testBlankSubjectIdReturnsBlankKey() {
        val key = ResumeManager.calculateKey(subjectId = "", season = 1, episode = 1, isSeries = true)
        assertEquals("", key)
    }

    @Test
    fun testSimulatedSharedPlayerIsolation() {
        // Movie A played to 14:02 (842,000 ms)
        val sessionA = PlaybackSession(
            subjectId = "movie_A",
            season = 0,
            episode = 0,
            startPositionMs = 842_000L,
            isSeries = false
        )

        // Movie B is fresh (no history)
        val sessionB = PlaybackSession(
            subjectId = "movie_B",
            season = 0,
            episode = 0,
            startPositionMs = ResumeManager.calculateStartPosition(positionMs = 0L, durationMs = 1_200_000L, finished = false),
            isSeries = false
        )

        assertNotEquals(sessionA.key, sessionB.key)
        assertEquals("movie_A", sessionA.key)
        assertEquals("movie_B", sessionB.key)
        assertEquals(842_000L, sessionA.startPositionMs)
        assertEquals(0L, sessionB.startPositionMs)
    }

    @Test
    fun testShouldSavePositionValidation() {
        assertFalse(ResumeManager.shouldSavePosition(positionMs = 5_000L, durationMs = 1_200_000L))
        assertTrue(ResumeManager.shouldSavePosition(positionMs = 60_000L, durationMs = 1_200_000L))
        assertFalse(ResumeManager.shouldSavePosition(positionMs = 1_180_000L, durationMs = 1_200_000L))
    }
}
