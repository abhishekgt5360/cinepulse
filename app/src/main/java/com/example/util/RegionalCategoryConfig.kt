package com.example.util

data class CatalogPreset(
    val id: String,
    val label: String,
    val country: String? = null,
    val countries: List<String> = emptyList(),
    val genre: String? = null,
    val language: String? = null,
    val requiresAdultOptIn: Boolean = false
)

object RegionalCategoryConfig {
    const val ROW_TITLE = "Bollywood"
    const val ENGLISH_LABEL = "English"
    const val ADULT_LABEL = "Adult"

    val PRESETS = listOf(
        CatalogPreset(id = "all", label = "All"),
        CatalogPreset(id = "bollywood", label = ROW_TITLE, country = "India"),
        CatalogPreset(
            id = "english",
            label = ENGLISH_LABEL,
            countries = listOf("United States", "United Kingdom", "Canada", "Australia")
        ),
        CatalogPreset(
            id = "adult",
            label = ADULT_LABEL,
            genre = "Adult",
            requiresAdultOptIn = true
        )
    )
}
