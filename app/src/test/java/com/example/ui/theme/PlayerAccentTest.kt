package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerAccentTest {

    @Test
    fun testPlayerAccentHasAtLeastThreeToOneContrastAgainstBlackInAllThemes() {
        val schemes = mapOf(
            "CRIMSON" to CrimsonScheme,
            "OLED" to OledScheme,
            "HIGH_CONTRAST" to HighContrastScheme,
            "PAPER" to PaperScheme
        )

        val failures = mutableListOf<String>()

        schemes.forEach { (name, scheme) ->
            val accent = getPlayerAccentForScheme(scheme)
            val ratio = calculateContrastRatio(accent, Color.Black)
            println("Theme $name -> playerAccent: $accent | Contrast against Black: ${String.format("%.2f", ratio)}:1")

            if (ratio < 3.0) {
                failures.add("Theme $name: playerAccent contrast against black is ${String.format("%.2f", ratio)}:1 (< 3.0:1)")
            }

            val isHighContrast = (name == "HIGH_CONTRAST")
            val textMinRatio = if (isHighContrast) 7.0 else 4.5

            val opcRatio = calculateContrastRatio(scheme.onPrimaryContainer, scheme.primaryContainer)
            if (opcRatio < textMinRatio) {
                failures.add("Theme $name: onPrimaryContainer / primaryContainer contrast is ${String.format("%.2f", opcRatio)}:1 (< $textMinRatio:1)")
            }

            val osRatio = calculateContrastRatio(scheme.onSurface, scheme.surfaceContainerHigh)
            if (osRatio < textMinRatio) {
                failures.add("Theme $name: onSurface / surfaceContainerHigh contrast is ${String.format("%.2f", osRatio)}:1 (< $textMinRatio:1)")
            }
        }

        assertTrue(
            "PlayerAccentTest failed with violations:\n" + failures.joinToString("\n"),
            failures.isEmpty()
        )
    }
}
