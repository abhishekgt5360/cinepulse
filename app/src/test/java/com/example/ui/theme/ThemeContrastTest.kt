package com.example.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {

    @Test
    fun testAllFixedThemeSchemesMeetContrastRequirements() {
        val schemes = mapOf(
            "CRIMSON" to CrimsonScheme,
            "OLED" to OledScheme,
            "HIGH_CONTRAST" to HighContrastScheme,
            "PAPER" to PaperScheme
        )

        val failures = mutableListOf<String>()
        val report = StringBuilder()

        report.append("\n================ THEME CONTRAST RATIOS REPORT ================\n")

        schemes.forEach { (name, scheme) ->
            val isHighContrast = (name == "HIGH_CONTRAST")
            val textMinRatio = if (isHighContrast) 7.0 else 4.5
            val iconMinRatio = 3.0

            report.append("\nTheme: $name (Text min: ${textMinRatio}:1, Icon min: ${iconMinRatio}:1)\n")
            report.append("-".repeat(60)).append("\n")

            val textPairs = listOf(
                "onSurface / surface" to (scheme.onSurface to scheme.surface),
                "onSurfaceVariant / surfaceContainer" to (scheme.onSurfaceVariant to scheme.surfaceContainer),
                "onPrimary / primary" to (scheme.onPrimary to scheme.primary),
                "onPrimaryContainer / primaryContainer" to (scheme.onPrimaryContainer to scheme.primaryContainer),
                "onSecondaryContainer / secondaryContainer" to (scheme.onSecondaryContainer to scheme.secondaryContainer),
                "onTertiaryContainer / tertiaryContainer" to (scheme.onTertiaryContainer to scheme.tertiaryContainer),
                "onError / error" to (scheme.onError to scheme.error),
                "onBackground / background" to (scheme.onBackground to scheme.background),
                "onSurfaceVariant / surfaceContainerHigh" to (scheme.onSurfaceVariant to scheme.surfaceContainerHigh)
            )

            val graphicPairs = listOf(
                "outline / background" to (scheme.outline to scheme.background),
                "primary / background" to (scheme.primary to scheme.background)
            )

            textPairs.forEach { (pairName, pair) ->
                val ratio = calculateContrastRatio(pair.first, pair.second)
                val passed = ratio >= textMinRatio
                report.append(String.format("  %-40s : %5.2f:1  [%s]\n", pairName, ratio, if (passed) "PASS" else "FAIL"))
                if (!passed) {
                    failures.add("$name -> $pairName ratio is ${String.format("%.2f", ratio)}:1 (required >= $textMinRatio:1)")
                }
            }

            graphicPairs.forEach { (pairName, pair) ->
                val ratio = calculateContrastRatio(pair.first, pair.second)
                val passed = ratio >= iconMinRatio
                report.append(String.format("  %-40s : %5.2f:1  [%s]\n", pairName, ratio, if (passed) "PASS" else "FAIL"))
                if (!passed) {
                    failures.add("$name -> $pairName ratio is ${String.format("%.2f", ratio)}:1 (required >= $iconMinRatio:1)")
                }
            }
        }

        report.append("===============================================================\n")

        println(report.toString())

        assertTrue(
            "Theme contrast test failed with the following violations:\n" + failures.joinToString("\n"),
            failures.isEmpty()
        )
    }
}
