package com.example.util

data class CatalogPreset(
    val id: String,
    val label: String,
    val description: String = "",
    val country: String? = null,
    val countries: List<String> = emptyList(),
    val genre: String? = null,
    val language: String? = null,
    val requiresAdultOptIn: Boolean = false
)

object RegionalCategoryConfig {
    /** Title of the Home shelf. Kept for the Home feed; Browse uses [PRESET_INDIA]. */
    const val ROW_TITLE = "Bollywood"

    const val PRESET_ALL = "all"
    const val PRESET_INDIA = "india"
    const val PRESET_US_UK_CA_AU = "us_uk_ca_au"
    const val PRESET_ADULT = "adult"

    val PRESETS = listOf(
        CatalogPreset(
            id = PRESET_ALL,
            label = "All",
            description = "Everything in this catalog."
        ),
        CatalogPreset(
            id = PRESET_INDIA,
            label = "India",
            description = "Indian titles, including Hindi, Tamil, Telugu, Malayalam and more.",
            country = "India"
        ),
        CatalogPreset(
            id = PRESET_US_UK_CA_AU,
            label = "US, UK, CA, AU",
            description = "Titles from these countries. This is not a language filter.",
            countries = listOf("United States", "United Kingdom", "Canada", "Australia")
        ),
        CatalogPreset(
            id = PRESET_ADULT,
            label = "Adult",
            description = "Titles tagged Adult. Enable Adult content in Settings first.",
            genre = "Adult",
            requiresAdultOptIn = true
        )
    )

    /** Country/region presets. Adult is intentionally not part of this group. */
    val REGION_PRESETS: List<CatalogPreset> = PRESETS.filter { !it.requiresAdultOptIn }

    val ADULT_PRESET: CatalogPreset = PRESETS.first { it.id == PRESET_ADULT }

    fun presetById(id: String?): CatalogPreset =
        PRESETS.firstOrNull { it.id == id } ?: PRESETS.first()

    fun regionPresets(): List<CatalogPreset> = REGION_PRESETS

    /** Exact years offered by the Browse filter sheet, newest first. */
    val YEAR_OPTIONS: List<String> = run {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        (currentYear downTo 1950).map(Int::toString)
    }
}
