package com.example.util

object ResumeManager {

    const val MIN_PLAY_THRESHOLD_MS = 10_000L // 10 seconds
    const val END_OFFSET_THRESHOLD_MS = 30_000L // 30 seconds
    const val END_PERCENT_THRESHOLD = 0.95f // 95%

    fun calculateKey(subjectId: String, season: Int, episode: Int, isSeries: Boolean): String {
        if (subjectId.isBlank()) return ""
        val se = if (isSeries) season.coerceAtLeast(1) else 0
        val ep = if (isSeries) episode.coerceAtLeast(1) else 0
        return if (isSeries) "${subjectId}_s${se}_e${ep}" else subjectId
    }

    fun calculateStartPosition(positionMs: Long, durationMs: Long, finished: Boolean): Long {
        if (finished) return 0L
        if (positionMs < MIN_PLAY_THRESHOLD_MS) return 0L
        if (durationMs > 0L) {
            if (positionMs > durationMs) return 0L
            val remainingMs = durationMs - positionMs
            if (remainingMs <= END_OFFSET_THRESHOLD_MS || positionMs >= (durationMs * END_PERCENT_THRESHOLD)) {
                return 0L
            }
        }
        return positionMs.coerceAtLeast(0L)
    }

    fun shouldSavePosition(positionMs: Long, durationMs: Long): Boolean {
        if (positionMs < MIN_PLAY_THRESHOLD_MS) return false
        if (durationMs > 0L) {
            if (positionMs > durationMs) return false
            val remainingMs = durationMs - positionMs
            if (remainingMs <= END_OFFSET_THRESHOLD_MS || positionMs >= (durationMs * END_PERCENT_THRESHOLD)) {
                return false
            }
        }
        return true
    }

    fun isFinished(positionMs: Long, durationMs: Long): Boolean {
        if (durationMs <= 0L) return false
        val remainingMs = durationMs - positionMs
        return remainingMs <= END_OFFSET_THRESHOLD_MS || positionMs >= (durationMs * END_PERCENT_THRESHOLD)
    }

    fun formatTimeLabel(positionMs: Long): String {
        val totalSec = (positionMs / 1000).coerceAtLeast(0)
        val mins = totalSec / 60
        val secs = totalSec % 60
        val hrs = mins / 60
        val remMins = mins % 60
        return if (hrs > 0) {
            String.format(java.util.Locale.US, "%d:%02d:%02d", hrs, remMins, secs)
        } else {
            String.format(java.util.Locale.US, "%d:%02d", remMins, secs)
        }
    }
}
