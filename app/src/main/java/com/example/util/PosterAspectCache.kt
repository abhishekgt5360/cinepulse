package com.example.util

import android.util.LruCache

enum class ImageAspectRatioCategory {
    PORTRAIT,
    SQUAREISH,
    WIDE
}

object PosterAspectCache {
    const val RATIO_PORTRAIT_MAX = 0.85f
    const val RATIO_SQUAREISH_MAX = 1.15f

    private val cache = LruCache<String, Float>(500)

    fun getRatio(url: String?): Float? {
        if (url.isNullOrBlank()) return null
        return cache.get(url)
    }

    fun putRatio(url: String?, ratio: Float) {
        if (!url.isNullOrBlank() && ratio > 0f) {
            cache.put(url, ratio)
        }
    }

    fun classify(ratio: Float?): ImageAspectRatioCategory {
        if (ratio == null || ratio <= 0f) return ImageAspectRatioCategory.PORTRAIT
        return when {
            ratio <= RATIO_PORTRAIT_MAX -> ImageAspectRatioCategory.PORTRAIT
            ratio <= RATIO_SQUAREISH_MAX -> ImageAspectRatioCategory.SQUAREISH
            else -> ImageAspectRatioCategory.WIDE
        }
    }

    fun clear() {
        cache.evictAll()
    }
}
