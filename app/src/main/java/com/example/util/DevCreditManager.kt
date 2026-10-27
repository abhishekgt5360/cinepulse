package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object DevCreditManager {
    private const val PREFS_NAME = "cinepulse_dev_credit"
    private const val KEY_DISMISSED_FOREVER = "dev_credit_dismissed_forever"
    val DEV_NAME: String get() = SecureVault.devName
    val DEV_URL: String get() = SecureVault.devUrl

    fun shouldShowDialog(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_DISMISSED_FOREVER, false)
    }

    fun dismissForever(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DISMISSED_FOREVER, true).apply()
    }

    fun openWebsite(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DEV_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
