package com.example.util

data class PlaybackSession(
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val subjectId: String,
    val season: Int = 0,
    val episode: Int = 0,
    val startPositionMs: Long = 0L,
    val isSeries: Boolean = false
) {
    val key: String
        get() = if (isSeries) "${subjectId}_s${season}_e${episode}" else subjectId

    val isValid: Boolean
        get() = subjectId.isNotBlank()
}
