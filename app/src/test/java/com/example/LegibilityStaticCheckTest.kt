package com.example

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LegibilityStaticCheckTest {

    @Test
    fun testNoBannedColorTokensInUiCodeWithoutExemption() {
        val rootDir = File("src/main/java/com/example/ui")
        assertTrue("UI source directory must exist", rootDir.exists() && rootDir.isDirectory)

        val bannedRegexes = listOf(
            Regex("""\bColor\.White\b"""),
            Regex("""\bColor\.Black\b"""),
            Regex("""\bPureWhite\b"""),
            Regex("""\bJetBlack\b"""),
            Regex("""\bTextPrimary\b"""),
            Regex("""\bTextSecondary\b"""),
            Regex("""\bColor\(0x[0-9a-fA-F]+\)""")
        )

        val violations = mutableListOf<String>()

        rootDir.walk().filter { file ->
            file.isFile && file.extension == "kt" &&
                    !file.path.contains("theme")
        }.forEach { file ->
            val lines = file.readLines()
            lines.forEachIndexed { index, line ->
                val lineNum = index + 1
                if (!line.contains("// legibility-ok:")) {
                    bannedRegexes.forEach { regex ->
                        if (regex.containsMatchIn(line)) {
                            violations.add("${file.name}:$lineNum -> '$line'")
                        }
                    }
                }
            }
        }

        assertTrue(
            "Found ${violations.size} banned color token usages in UI code without exemption comment:\n" +
                    violations.joinToString("\n"),
            violations.isEmpty()
        )
    }
}
