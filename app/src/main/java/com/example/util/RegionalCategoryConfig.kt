package com.example.util

data class RegionPreset(
    val label: String,
    val country: String?
)

object RegionalCategoryConfig {
    const val ROW_TITLE = "Bollywood"

    val REGION_PRESETS = listOf(
        RegionPreset(label = "All", country = null),
        RegionPreset(label = "Bollywood", country = "India")
    )
}
