package com.example.ui.player

import androidx.media3.common.Player
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerKeepScreenOnTest {

    fun shouldKeepScreenOn(playWhenReady: Boolean, playbackState: Int, hasError: Boolean): Boolean {
        if (hasError) return false
        if (!playWhenReady) return false
        return playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING
    }

    @Test
    fun testKeepScreenOnRules() {
        // Playing and ready -> true
        assertTrue(shouldKeepScreenOn(playWhenReady = true, playbackState = Player.STATE_READY, hasError = false))

        // Playing and buffering -> true
        assertTrue(shouldKeepScreenOn(playWhenReady = true, playbackState = Player.STATE_BUFFERING, hasError = false))

        // Paused -> false
        assertFalse(shouldKeepScreenOn(playWhenReady = false, playbackState = Player.STATE_READY, hasError = false))

        // Ended -> false
        assertFalse(shouldKeepScreenOn(playWhenReady = true, playbackState = Player.STATE_ENDED, hasError = false))

        // Idle -> false
        assertFalse(shouldKeepScreenOn(playWhenReady = true, playbackState = Player.STATE_IDLE, hasError = false))

        // Error -> false
        assertFalse(shouldKeepScreenOn(playWhenReady = true, playbackState = Player.STATE_READY, hasError = true))
    }
}
