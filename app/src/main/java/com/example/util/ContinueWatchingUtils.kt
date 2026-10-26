package com.example.util

object ContinueWatchingUtils {

    fun formatRemaining(positionMs: Long, durationMs: Long): String {
        if (durationMs <= 0 || positionMs < 0 || positionMs > durationMs) return ""
        val remainingMs = durationMs - positionMs
        if (remainingMs < 60_000L) {
            return "Less than a minute left"
        }
        val totalMins = remainingMs / 60_000L
        val hours = totalMins / 60
        val mins = totalMins % 60
        return if (hours > 0) {
            if (mins > 0) "${hours}h ${mins}m remaining" else "${hours}h remaining"
        } else {
            "${mins}m remaining"
        }
    }

    fun calculateProgressFraction(positionMs: Long, durationMs: Long): Float {
        if (durationMs <= 0L || positionMs < 0L) return 0f
        return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    fun formatTitle(title: String, season: Int, episode: Int): String {
        return if (season > 0 || episode > 0) {
            "$title S$season:E$episode"
        } else {
            title
        }
    }
}
