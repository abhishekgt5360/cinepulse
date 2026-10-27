package com.example.util

import android.content.Context

enum class ResumeNoticePosition {
    TOP,
    BOTTOM
}

/**
 * User-configurable appearance and behavior for the "Resumed from ..." notice
 * shown at the start of video playback.
 */
data class ResumeNoticeConfig(
    val enabled: Boolean = true,
    val durationMs: Long = ResumeNoticePreferences.DEFAULT_DURATION_MS,
    val position: ResumeNoticePosition = ResumeNoticePosition.BOTTOM,
    val messageTemplate: String = ResumeNoticePreferences.DEFAULT_MESSAGE_TEMPLATE,
    val actionLabel: String = ResumeNoticePreferences.DEFAULT_ACTION_LABEL
) {
    fun formatMessage(timeLabel: String): String {
        val template = messageTemplate.ifBlank { ResumeNoticePreferences.DEFAULT_MESSAGE_TEMPLATE }
        return template.replace("{time}", timeLabel)
    }

    fun resolvedActionLabel(): String {
        return actionLabel.ifBlank { ResumeNoticePreferences.DEFAULT_ACTION_LABEL }
    }
}

object ResumeNoticePreferences {
    private const val PREFS_NAME = "cinepulse_resume_notice"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_DURATION_MS = "duration_ms"
    private const val KEY_POSITION = "position"
    private const val KEY_MESSAGE_TEMPLATE = "message_template"
    private const val KEY_ACTION_LABEL = "action_label"

    const val DEFAULT_DURATION_MS = 5_000L
    const val DURATION_UNTIL_DISMISSED = 0L
    const val DEFAULT_MESSAGE_TEMPLATE = "Resumed from {time}"
    const val DEFAULT_ACTION_LABEL = "Start over"

    val DURATION_OPTIONS = listOf(3_000L, 5_000L, 8_000L, 10_000L, DURATION_UNTIL_DISMISSED)

    fun durationLabel(durationMs: Long): String = when (durationMs) {
        DURATION_UNTIL_DISMISSED -> "Until dismissed"
        3_000L -> "3 seconds"
        5_000L -> "5 seconds"
        8_000L -> "8 seconds"
        10_000L -> "10 seconds"
        else -> "${durationMs / 1000} seconds"
    }

    fun positionLabel(position: ResumeNoticePosition): String = when (position) {
        ResumeNoticePosition.TOP -> "Top"
        ResumeNoticePosition.BOTTOM -> "Bottom"
    }

    private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): ResumeNoticeConfig {
        val prefs = getPrefs(context)
        val duration = prefs.getLong(KEY_DURATION_MS, DEFAULT_DURATION_MS)
        return ResumeNoticeConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            durationMs = if (duration in DURATION_OPTIONS) duration else DEFAULT_DURATION_MS,
            position = readEnum(prefs.getString(KEY_POSITION, null), ResumeNoticePosition.BOTTOM),
            messageTemplate = prefs.getString(KEY_MESSAGE_TEMPLATE, DEFAULT_MESSAGE_TEMPLATE)
                ?: DEFAULT_MESSAGE_TEMPLATE,
            actionLabel = prefs.getString(KEY_ACTION_LABEL, DEFAULT_ACTION_LABEL)
                ?: DEFAULT_ACTION_LABEL
        )
    }

    fun save(context: Context, config: ResumeNoticeConfig) {
        getPrefs(context).edit()
            .putBoolean(KEY_ENABLED, config.enabled)
            .putLong(KEY_DURATION_MS, config.durationMs.coerceAtLeast(0L))
            .putString(KEY_POSITION, config.position.name)
            .putString(KEY_MESSAGE_TEMPLATE, config.messageTemplate)
            .putString(KEY_ACTION_LABEL, config.actionLabel)
            .apply()
    }

    fun reset(context: Context): ResumeNoticeConfig {
        val defaults = ResumeNoticeConfig()
        save(context, defaults)
        return defaults
    }

    private inline fun <reified T : Enum<T>> readEnum(value: String?, fallback: T): T {
        if (value.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
    }
}
