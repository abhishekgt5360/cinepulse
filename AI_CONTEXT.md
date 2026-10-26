# Zippy (formerly CinePulse) - Comprehensive AI Context & Instructions

**To the AI Assistant:** 
Read this document thoroughly before proposing any architectural changes, UI updates, or debugging fixes for this project. It contains the strict domain rules, technical boundaries ("ifs and buts"), and architectural constraints of this application.

## 1. Project Identity & Nomenclature
* **Public App Name:** Zippy (previously CinePulse). UI texts, launcher labels, and output APK names (`Zippy-<version>.apk`) reflect "Zippy".
* **Application ID:** `com.toolsetstudio.zippy`
* **Kotlin Namespace / Source Package:** `com.example`. 
  * *CRITICAL RULE:* Do **not** attempt to refactor the source package from `com.example` to `com.toolsetstudio.zippy`. It is intentionally kept as `com.example` to avoid breaking Room schemas, Moshi codegen, ProGuard rules, and complex internal references. A mismatch between Application ID and namespace is expected and supported.
* **Internal Identifiers:** Several internal constants, classes, and preferences still use the "CinePulse" naming convention (e.g., `CinePulseTheme`, `CinePulseApplication`, `cinepulse.db`, `cinepulse_prefs`). Do not rename these unless explicitly requested.

## 2. Tech Stack & Architecture
* **Framework:** Native Android (Kotlin), Jetpack Compose, Material 3.
* **Architecture Pattern:** MVVM (Model-View-ViewModel).
  * **UI State:** Exposed as `StateFlow` from `MainViewModel` (e.g., `HomeUiState`, `CatalogUiState`, `DetailPlayerUiState`).
* **Networking:** Retrofit + Moshi (`MovieApiService`).
* **Image Loading:** Coil (`AsyncImage`).
* **Video Playback:** AndroidX Media3 (ExoPlayer).
* **Local Persistence:** 
  * Room Database (`AppDatabase` -> `cinepulse.db`).
  * SharedPreferences (`cinepulse_prefs`, `CaptionPreferences`).

## 3. UI & Design System (Strict Rules)
* **Theming Engine:** The app supports 5 distinct themes (`CRIMSON`, `OLED`, `DYNAMIC`, `HIGH_CONTRAST`, `PAPER`) managed via `Theme.kt`.
* **No Hardcoded Colors:** You are strictly forbidden from using hardcoded colors like `Color.White`, `Color.Black`, or `Color(0xFF...)` in UI component code. 
  * **Enforcement:** A unit test (`LegibilityStaticCheckTest.kt`) scans UI code to prevent this. 
  * **Exception:** If a hardcoded color is absolutely required for a specific design overlay (e.g., a dark scrim over a poster or a solid white play button), you MUST append the exact comment `// legibility-ok: <reason>` on the same line to bypass the static check.
* **Theme Tokens via CompositionLocals:** Components should remain completely agnostic to `ThemeMode`. Provide varying styles (like border thicknesses) via `CompositionLocalProvider` in `Theme.kt` (e.g., `LocalHighContrast`, `LocalCardBorder`) and consume them in the UI.
* **Immersive Layouts:** The app uses full-bleed edge-to-edge layouts. The `HeroBanner` extends under the transparent status bar, using vignette gradients (`Brush.verticalGradient`) to keep system icons and floating top-bar actions legible.
* **Aspect Ratio Engine:** Poster images rely on `PosterAspectCache.kt` to dynamically classify image ratios (`PORTRAIT`, `SQUAREISH`, `WIDE`) without layout jitter. Always leverage this for uncropped 2:3 card framing.

## 4. Playback & Data Tracking
* **Watch Progress Isolation:** Playback progress is tracked in the `episode_progress` Room table with a composite primary key of `(subjectId, season, episode)`. This ensures different episodes of the same series don't overwrite each other's progress. 
* **Resume Logic:** Handled strictly by `ResumeManager.kt`. 
  * Thresholds: < 10s is ignored (starts at 0). > 95% or < 30s remaining is marked as "finished" (starts at 0). 
  * Pure logic formatting (e.g., "48m remaining", progress fractions) is isolated in `ContinueWatchingUtils.kt`.

## 5. Security & Networking Guardrails
* **`SecureVault.kt`:** Endpoints, developer URLs, and API keys are obfuscated using a dynamic XOR + Base64 rotating salt mechanism. **Never** modify this file without explicit instruction.
* **API Headers:** The app mimics specific client headers to authenticate with its backend. 
  * `User-Agent` is strictly set to `CinePulse-Android/1.0`.
  * Streaming `Referer` and `Origin` headers are set to `https://moviebox.ph/`.
  * *CRITICAL RULE:* Do **not** alter backend URLs, User-Agents, or Referer headers even if they contain the old name "CinePulse" or look like external domains. Changing these will instantly break video playback and API access.

## 6. General Development "Ifs and Buts"
* **No Mock Data:** Never replace functional API calls or database queries with hardcoded mock data for the sake of UI styling.
* **No Unnecessary Dependencies:** Do not add heavy libraries (e.g., `material-icons-extended`) if standard core icons or custom lightweight vectors suffice. 
* **Memory Constraints:** The app targets devices with as low as 2GB RAM. Do not use expensive `RenderEffect` blurs, palette color extraction per-image, or excessive bitmap allocations. Use `ContentScale.Fit` with low-res ambient background scaling where applicable.
* **Validation:** Before declaring a task complete, you must ensure that `./gradlew assembleRelease` and `./gradlew testDebugUnitTest` succeed without errors.

## 7. App Output
* **Release APK:** Configured in `build.gradle.kts` via `androidComponents` to output exactly `Zippy-<version>.apk`.
* **Min SDK:** 24
* **Target SDK:** 36