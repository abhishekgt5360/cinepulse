package com.example.ui

import android.app.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import com.example.data.local.AppDatabase
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.*
import com.example.data.repository.MovieRepository
import com.example.util.PlaybackSession
import com.example.util.RegionalCategoryConfig
import com.example.util.ResumeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.ui.theme.ThemeMode

private const val HTTP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
private const val REFERER_HEADER = "https://moviebox.ph/"

enum class AppTab {
    HOME,
    CATALOG,
    SEARCH,
    LIBRARY
}

enum class CatalogType(val label: String) {
    MOVIES("Movies"),
    TV_SERIES("TV Series"),
    ANIMATION("Animation")
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val bannerItems: List<MovieItem> = emptyList(),
    val sections: List<HomeSection> = emptyList(),
    val errorMessage: String? = null
)

data class CatalogUiState(
    val type: CatalogType = CatalogType.MOVIES,
    val selectedPresetId: String = RegionalCategoryConfig.PRESET_ALL,
    val selectedYear: String? = null,
    val selectedGenre: String = "All",
    val selectedSort: String = "foryou",
    val items: List<MovieItem> = emptyList(),
    val isLoading: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val errorMessage: String? = null
)

data class SearchUiState(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
    val results: List<MovieItem> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

data class DetailPlayerUiState(
    val slug: String? = null,
    val subjectId: String? = null,
    val title: String = "",
    val posterUrl: String? = null,
    val detail: DetailResponse? = null,
    val isSeries: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val selectedSeason: Int = 0,
    val selectedEpisode: Int = 0,
    val availableSeasons: List<SeasonItem> = emptyList(),
    val isPlaying: Boolean = false,
    val streamResponse: StreamResponse? = null,
    val captions: List<CaptionItem> = emptyList(),
    val isLoadingStream: Boolean = false,
    val streamError: String? = null,
    val session: PlaybackSession? = null,
    val isWatchlisted: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("cinepulse_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.CRIMSON.name) ?: ThemeMode.CRIMSON.name)
        } catch (e: Exception) {
            ThemeMode.CRIMSON
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    private val _isAdultEnabled = MutableStateFlow(prefs.getBoolean("adult_enabled", false))
    val isAdultEnabled: StateFlow<Boolean> = _isAdultEnabled.asStateFlow()

    fun setAdultEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("adult_enabled", enabled).apply()
        _isAdultEnabled.value = enabled

        if (!enabled && _catalogState.value.selectedPresetId == RegionalCategoryConfig.PRESET_ADULT) {
            setCatalogPreset(RegionalCategoryConfig.PRESET_ALL)
        }
        loadHomeFeed()
    }

    private fun isAdultItem(item: MovieItem): Boolean {
        val g = item.genre.orEmpty()
        return g.contains("Adult", ignoreCase = true) ||
               g.contains("Erotica", ignoreCase = true) ||
               g.contains("18+", ignoreCase = true) ||
               g.contains("Erotic", ignoreCase = true)
    }

    private val database = AppDatabase.getInstance(application)
    private val repository = MovieRepository(database = database, context = application)

    // Navigation state
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Home state primed with disk/memory cache for instant 0ms load
    private val _homeState = MutableStateFlow(
        repository.getCachedHome()?.let { cached ->
            val sections = cached.sections.orEmpty()
            val bannerSection = sections.firstOrNull { it.section.equals("Banner", ignoreCase = true) }
            val bannerItems = bannerSection?.items.orEmpty()
            val otherSections = sections.filter { !it.section.equals("Banner", ignoreCase = true) && it.items.isNotEmpty() }
            HomeUiState(
                isLoading = false,
                bannerItems = bannerItems,
                sections = otherSections,
                errorMessage = null
            )
        } ?: HomeUiState()
    )
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    // Catalog state
    private val _catalogState = MutableStateFlow(CatalogUiState())
    val catalogState: StateFlow<CatalogUiState> = _catalogState.asStateFlow()

    // Search state
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()
    private var searchJob: Job? = null
    private var catalogJob: Job? = null
    private var catalogJobIsReset = false

    // Detail & Player state
    private val _detailPlayerState = MutableStateFlow(DetailPlayerUiState())
    val detailPlayerState: StateFlow<DetailPlayerUiState> = _detailPlayerState.asStateFlow()

    // Room flows
    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Single persistent ExoPlayer instance surviving screen rotation
    private var playerInstance: ExoPlayer? = null

    init {
        loadHomeFeed()
        loadCatalog(reset = true)
    }

    @OptIn(UnstableApi::class)
    fun getPlayer(context: Context): ExoPlayer {
        if (playerInstance == null) {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(HTTP_USER_AGENT)
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to REFERER_HEADER,
                        "Origin" to "https://moviebox.ph",
                        "Accept" to "*/*",
                        "Accept-Encoding" to "identity" // CRITICAL: Prevents CDN proxy from gzip-compressing MP4 containers which causes parsing container malformed
                    )
                )
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(25000)
                .setReadTimeoutMs(35000)

            val dataSourceFactory = DefaultDataSource.Factory(context.applicationContext, httpDataSourceFactory)

            // Robust MP4 and Fragmented MP4 extractors with tolerant sample table parsing and edit list workarounds
            val extractorsFactory = DefaultExtractorsFactory().apply {
                setConstantBitrateSeekingEnabled(true)
                setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
                setFragmentedMp4ExtractorFlags(
                    FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS or
                    FragmentedMp4Extractor.FLAG_WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME
                )
            }

            val loadErrorHandlingPolicy = DefaultLoadErrorHandlingPolicy(6)

            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory)
                .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)

            // Resilient buffer control preventing buffer underruns and mid-stream chunk drops
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    /* minBufferMs = */ 30_000,
                    /* maxBufferMs = */ 60_000,
                    /* bufferForPlaybackMs = */ 2_500,
                    /* bufferForPlaybackAfterRebufferMs = */ 5_000
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()

            playerInstance = ExoPlayer.Builder(context.applicationContext)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .setSeekForwardIncrementMs(10000)
                .setSeekBackIncrementMs(10000)
                .build().apply {
                    playWhenReady = true
                }
        }
        return playerInstance!!
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            // Only show full loading indicator if we don't already have cached content
            if (_homeState.value.sections.isEmpty()) {
                val cachedHome = repository.getCachedHome()
                val cachedBolly = repository.getCachedBollywood()
                if (cachedHome != null || cachedBolly != null) {
                    val bannerSection = cachedHome?.sections.orEmpty().firstOrNull { it.section.equals("Banner", ignoreCase = true) }
                    val bannerItems = bannerSection?.items.orEmpty()
                    val otherSections = cachedHome?.sections.orEmpty()
                        .filter { !it.section.equals("Banner", ignoreCase = true) && !it.section.contains("Nollywood", ignoreCase = true) && it.items.isNotEmpty() }
                        .toMutableList()

                    if (cachedBolly != null && cachedBolly.items.isNotEmpty()) {
                        otherSections.add(0, HomeSection(section = RegionalCategoryConfig.ROW_TITLE, count = cachedBolly.items.size, items = cachedBolly.items))
                    }

                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            bannerItems = bannerItems,
                            sections = otherSections,
                            errorMessage = null
                        )
                    }
                } else {
                    _homeState.update { it.copy(isLoading = true, errorMessage = null) }
                }
            }

            val homeDeferred = async { repository.getHome() }
            val bollyDeferred = async { repository.getBollywoodMovies() }

            val homeResult = homeDeferred.await()
            val bollyResult = bollyDeferred.await()

            homeResult.onSuccess { response ->
                val sections = response.sections.orEmpty()
                val bannerSection = sections.firstOrNull { it.section.equals("Banner", ignoreCase = true) }
                val rawBannerItems = bannerSection?.items.orEmpty()
                val bannerItems = if (_isAdultEnabled.value) rawBannerItems else rawBannerItems.filter { !isAdultItem(it) }

                val otherSections = sections
                    .filter { !it.section.equals("Banner", ignoreCase = true) && !it.section.contains("Nollywood", ignoreCase = true) && it.items.isNotEmpty() }
                    .map { section ->
                        val filteredItems = if (_isAdultEnabled.value) section.items else section.items.filter { !isAdultItem(it) }
                        section.copy(items = filteredItems)
                    }
                    .filter { it.items.isNotEmpty() }
                    .toMutableList()

                val rawBollyItems = bollyResult.getOrNull()?.items.orEmpty()
                val bollyItems = if (_isAdultEnabled.value) rawBollyItems else rawBollyItems.filter { !isAdultItem(it) }
                if (bollyItems.isNotEmpty()) {
                    otherSections.add(0, HomeSection(section = RegionalCategoryConfig.ROW_TITLE, count = bollyItems.size, items = bollyItems))
                }

                _homeState.update {
                    it.copy(
                        isLoading = false,
                        bannerItems = bannerItems,
                        sections = otherSections,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                val cachedHome = repository.getCachedHome()
                val cachedBolly = repository.getCachedBollywood()
                if (cachedHome != null) {
                    val bannerSection = cachedHome.sections.orEmpty().firstOrNull { it.section.equals("Banner", ignoreCase = true) }
                    val bannerItems = bannerSection?.items.orEmpty()
                    val otherSections = cachedHome.sections.orEmpty()
                        .filter { !it.section.equals("Banner", ignoreCase = true) && !it.section.contains("Nollywood", ignoreCase = true) && it.items.isNotEmpty() }
                        .toMutableList()

                    if (cachedBolly != null && cachedBolly.items.isNotEmpty()) {
                        otherSections.add(0, HomeSection(section = RegionalCategoryConfig.ROW_TITLE, count = cachedBolly.items.size, items = cachedBolly.items))
                    }

                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            bannerItems = bannerItems,
                            sections = otherSections,
                            errorMessage = null
                        )
                    }
                } else {
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to load home feed. Please check your connection."
                        )
                    }
                }
            }
        }
    }

    fun setCatalogType(type: CatalogType) {
        if (_catalogState.value.type != type) {
            val isAnimation = type == CatalogType.ANIMATION
            _catalogState.update {
                it.copy(
                    type = type,
                    // Animation has no country/year controls, so never carry them in.
                    selectedPresetId = if (isAnimation) RegionalCategoryConfig.PRESET_ALL else it.selectedPresetId,
                    selectedYear = if (isAnimation) null else it.selectedYear,
                    page = 1
                )
            }
            loadCatalog(reset = true)
        }
    }

    fun setCatalogPreset(presetId: String) {
        val preset = RegionalCategoryConfig.PRESETS.firstOrNull { it.id == presetId } ?: return
        if (preset.requiresAdultOptIn && !_isAdultEnabled.value) return
        if (_catalogState.value.selectedPresetId != presetId) {
            _catalogState.update { it.copy(selectedPresetId = presetId, page = 1) }
            loadCatalog(reset = true)
        }
    }

    fun setCatalogYear(year: String?) {
        if (_catalogState.value.selectedYear != year) {
            _catalogState.update { it.copy(selectedYear = year, page = 1) }
            loadCatalog(reset = true)
        }
    }

    fun setCatalogGenre(genre: String) {
        if (_catalogState.value.selectedGenre != genre) {
            _catalogState.update { it.copy(selectedGenre = genre, page = 1) }
            loadCatalog(reset = true)
        }
    }

    fun setCatalogSort(sort: String) {
        if (_catalogState.value.selectedSort != sort) {
            _catalogState.update { it.copy(selectedSort = sort, page = 1) }
            loadCatalog(reset = true)
        }
    }

    fun resetCatalogFilters() {
        _catalogState.update {
            it.copy(
                selectedPresetId = RegionalCategoryConfig.PRESET_ALL,
                selectedYear = null,
                selectedGenre = "All",
                selectedSort = "foryou",
                page = 1
            )
        }
        loadCatalog(reset = true)
    }

    /**
     * Opens the catalog on [presetId] with a clean filter state. Used by Home "See all"
     * rows so a previous year/genre/sort selection never leaks into the destination list.
     */
    fun openCatalogPreset(presetId: String, type: CatalogType = CatalogType.MOVIES) {
        val preset = RegionalCategoryConfig.PRESETS.firstOrNull { it.id == presetId } ?: return
        if (preset.requiresAdultOptIn && !_isAdultEnabled.value) return

        val isAnimation = type == CatalogType.ANIMATION
        _catalogState.update {
            it.copy(
                type = type,
                selectedPresetId = if (isAnimation) RegionalCategoryConfig.PRESET_ALL else presetId,
                selectedYear = null,
                selectedGenre = "All",
                selectedSort = "foryou",
                page = 1
            )
        }
        loadCatalog(reset = true)
    }

    private fun catalogRequestSignature(state: CatalogUiState): String {
        val preset = RegionalCategoryConfig.presetById(state.selectedPresetId)
        val genre = preset.genre ?: state.selectedGenre.takeUnless { it == "All" }
        val year = if (state.type == CatalogType.ANIMATION) null else state.selectedYear
        return "${state.type.name}|${preset.id}|$genre|${state.selectedSort}|$year"
    }

    fun loadCatalog(reset: Boolean = false) {
        val snapshot = _catalogState.value
        if (!reset && (snapshot.isLoading || !snapshot.hasMore)) return
        // Never let auto-pagination cancel a reset that is still in flight.
        if (!reset && catalogJob?.isActive == true && catalogJobIsReset) return

        catalogJob?.cancel()

        val targetPage = if (reset) 1 else snapshot.page + 1
        val preset = RegionalCategoryConfig.presetById(snapshot.selectedPresetId)
        val isAnimation = snapshot.type == CatalogType.ANIMATION
        val yearParam = if (isAnimation) null else snapshot.selectedYear
        val genreParam = preset.genre ?: snapshot.selectedGenre.takeUnless { it == "All" }
        val sortParam = snapshot.selectedSort
        val requestSignature = catalogRequestSignature(snapshot)
        val cacheKey = MovieRepository.presetCacheKey(
            preset = preset,
            type = snapshot.type,
            page = targetPage,
            sort = sortParam,
            genre = genreParam,
            year = yearParam
        )

        // Immediately display cached catalog items if available on reset.
        if (reset) {
            val cached = repository.getCachedCatalog(cacheKey)
            if (cached != null && cached.items.isNotEmpty()) {
                _catalogState.update {
                    it.copy(
                        items = cached.items,
                        isLoading = false,
                        page = 1,
                        hasMore = cached.hasMore ?: true,
                        errorMessage = null
                    )
                }
            } else {
                // Clear the previous filter's results rather than showing stale titles.
                _catalogState.update {
                    it.copy(
                        items = emptyList(),
                        isLoading = true,
                        page = 1,
                        hasMore = true,
                        errorMessage = null
                    )
                }
            }
        } else {
            _catalogState.update { it.copy(isLoading = true, errorMessage = null) }
        }

        catalogJobIsReset = reset
        catalogJob = viewModelScope.launch {
            val result = repository.getPresetCatalog(
                preset = preset,
                type = snapshot.type,
                genre = genreParam,
                sort = sortParam,
                year = yearParam,
                page = targetPage
            )

            // A newer filter state owns the screen now; drop this response.
            if (catalogRequestSignature(_catalogState.value) != requestSignature) return@launch

            result.onSuccess { response ->
                val newItems = if (reset) {
                    response.items
                } else {
                    (_catalogState.value.items + response.items)
                        .distinctBy { "${it.safeSlug}_${it.subjectId}" }
                }
                _catalogState.update {
                    it.copy(
                        isLoading = false,
                        items = newItems,
                        page = targetPage,
                        hasMore = response.hasMore ?: response.items.isNotEmpty(),
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _catalogState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load catalog."
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchState.update { it.copy(query = newQuery) }
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _searchState.update { it.copy(suggestions = emptyList(), results = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            repository.getSuggestions(newQuery).onSuccess { res ->
                val titles = res.suggestions.mapNotNull { it.title }.filter { it.isNotBlank() }.distinct()
                _searchState.update { it.copy(suggestions = titles) }
            }
            performSearch(newQuery)
        }
    }

    fun performSearch(queryToSearch: String) {
        val q = queryToSearch.trim()
        if (q.isBlank()) return
        searchJob?.cancel()
        viewModelScope.launch {
            _searchState.update { it.copy(query = q, isSearching = true, errorMessage = null) }
            repository.search(query = q, page = 1)
                .onSuccess { res ->
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            results = res.items,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            errorMessage = error.localizedMessage ?: "Search failed."
                        )
                    }
                }
        }
    }

    fun openDetail(
        slug: String,
        subjectId: String,
        title: String = "",
        initialType: String? = null,
        posterUrl: String? = null
    ) {
        val guessedSeries = initialType.equals("tv", ignoreCase = true) || initialType.equals("tv-series", ignoreCase = true)
        _detailPlayerState.value = DetailPlayerUiState(
            slug = slug,
            subjectId = subjectId,
            title = title,
            posterUrl = posterUrl,
            isSeries = guessedSeries,
            selectedSeason = if (guessedSeries) 1 else 0,
            selectedEpisode = if (guessedSeries) 1 else 0,
            isLoadingDetail = true
        )

        // Observe watchlist
        viewModelScope.launch {
            repository.isWatchlisted(subjectId).collectLatest { isWatchlisted ->
                _detailPlayerState.update { it.copy(isWatchlisted = isWatchlisted) }
            }
        }

        // Fetch detail
        viewModelScope.launch {
            val savedHistory = repository.getHistoryItem(subjectId)
            val prevPos = savedHistory?.positionMs ?: 0L
            val prevSe = savedHistory?.season ?: 1
            val prevEp = savedHistory?.episode ?: 1

            repository.getDetail(slug)
                .onSuccess { detailRes ->
                    val subjectType = detailRes.data?.subject?.subjectType
                    val rawSeasons = detailRes.data?.resource?.seasons.orEmpty()
                    // CRITICAL: A title is a series ONLY if subjectType == 2 OR (subjectType != 1 && rawSeasons has se > 0 and maxEp > 0)
                    val isMovie = subjectType == 1 || (!guessedSeries && subjectType != 2 && rawSeasons.all { it.se == 0 })
                    val isSeries = !isMovie && (subjectType == 2 || guessedSeries || rawSeasons.any { it.se > 0 && it.maxEp > 0 })

                    val validSeasons = if (isSeries) rawSeasons.filter { it.se > 0 && it.maxEp > 0 } else emptyList()
                    val actualTitle = detailRes.data?.subject?.title ?: title

                    val actualSe = if (isSeries) {
                        if (validSeasons.any { it.se == prevSe }) prevSe else (validSeasons.firstOrNull()?.se ?: 1)
                    } else 0
                    val actualEp = if (isSeries) prevEp.coerceAtLeast(1) else 0

                    val fetchedCover = detailRes.data?.subject?.cover?.url
                    val effectivePoster = posterUrl?.takeIf { it.isNotBlank() } ?: fetchedCover
                    if (!effectivePoster.isNullOrBlank()) {
                        viewModelScope.launch {
                            repository.updateWatchlistPosterIfMissing(subjectId, effectivePoster)
                            repository.updateHistoryPosterIfMissing(subjectId, effectivePoster)
                        }
                    }

                    val startPos = repository.getEpisodeProgress(subjectId, actualSe, actualEp, isSeries)
                    val session = PlaybackSession(
                        subjectId = subjectId,
                        season = actualSe,
                        episode = actualEp,
                        startPositionMs = startPos,
                        isSeries = isSeries
                    )

                    _detailPlayerState.update {
                        it.copy(
                            detail = detailRes,
                            title = actualTitle,
                            posterUrl = effectivePoster,
                            isSeries = isSeries,
                            availableSeasons = validSeasons,
                            selectedSeason = actualSe,
                            selectedEpisode = actualEp,
                            isLoadingDetail = false,
                            session = session
                        )
                    }

                    // Pre-fetch captions so available subtitle languages (e.g. Hindi, English) are known immediately
                    viewModelScope.launch {
                        repository.getCaptions(subjectId, slug, season = actualSe, episode = actualEp, isSeries = isSeries)
                            .onSuccess { capRes ->
                                val caps = capRes.captions.orEmpty()
                                _detailPlayerState.update { it.copy(captions = caps) }
                            }
                    }
                }
                .onFailure { error ->
                    _detailPlayerState.update {
                        it.copy(
                            isLoadingDetail = false,
                            streamError = error.localizedMessage
                        )
                    }
                }
        }
    }

    fun playItem(
        slug: String,
        subjectId: String,
        season: Int = 0,
        episode: Int = 0,
        resumePositionMs: Long? = null
    ) {
        viewModelScope.launch {
            val current = _detailPlayerState.value
            val isSeries = current.isSeries

            // Flush old session position under its own key before starting new session!
            val oldSession = current.session
            val player = playerInstance
            if (oldSession != null && player != null && player.duration > 0 && player.currentPosition > 0) {
                repository.saveWatchProgress(
                    subjectId = oldSession.subjectId,
                    slug = current.slug ?: slug,
                    title = current.title,
                    posterUrl = current.posterUrl,
                    season = oldSession.season,
                    episode = oldSession.episode,
                    positionMs = player.currentPosition,
                    durationMs = player.duration,
                    type = if (oldSession.isSeries) "tv" else "movie"
                )
            }

            // Stop player & clear media items to isolate sessions
            playerInstance?.stop()
            playerInstance?.clearMediaItems()

            val actualSe = if (isSeries) season.coerceAtLeast(1) else 0
            val actualEp = if (isSeries) episode.coerceAtLeast(1) else 0

            val initialPos = resumePositionMs ?: repository.getEpisodeProgress(subjectId, actualSe, actualEp, isSeries)
            val newSession = PlaybackSession(
                subjectId = subjectId,
                season = actualSe,
                episode = actualEp,
                startPositionMs = initialPos,
                isSeries = isSeries
            )

            _detailPlayerState.update {
                it.copy(
                    slug = slug,
                    subjectId = subjectId,
                    selectedSeason = actualSe,
                    selectedEpisode = actualEp,
                    isPlaying = true,
                    isLoadingStream = true,
                    streamError = null,
                    session = newSession
                )
            }

            // Fetch stream and captions in parallel
            val streamResult = repository.getStream(subjectId, slug, actualSe, actualEp, isSeries = isSeries)
            val captionsResult = repository.getCaptions(subjectId, slug, actualSe, actualEp, isSeries = isSeries)

            streamResult.onSuccess { streamRes ->
                val caps = captionsResult.getOrNull()?.captions.orEmpty()
                if (streamRes.sources.isNotEmpty()) {
                    _detailPlayerState.update {
                        it.copy(
                            streamResponse = streamRes,
                            captions = caps,
                            isLoadingStream = false,
                            streamError = null
                        )
                    }
                } else {
                    _detailPlayerState.update {
                        it.copy(
                            streamResponse = streamRes,
                            captions = caps,
                            isLoadingStream = false,
                            streamError = streamRes.note ?: "No stream sources available for this title."
                        )
                    }
                }
            }.onFailure { err ->
                _detailPlayerState.update {
                    it.copy(
                        isLoadingStream = false,
                        streamError = err.localizedMessage ?: "Failed to fetch stream link."
                    )
                }
            }
        }
    }

    suspend fun getStreamForDownload(subjectId: String, slug: String, season: Int, episode: Int): List<StreamSource> {
        val isSeries = _detailPlayerState.value.isSeries
        val s = if (isSeries) season.coerceAtLeast(1) else 0
        val e = if (isSeries) episode.coerceAtLeast(1) else 0
        return repository.getStream(subjectId, slug, s, e, isSeries = isSeries).getOrNull()?.sources.orEmpty()
    }

    fun selectEpisode(season: Int, episode: Int) {
        val s = _detailPlayerState.value
        val slug = s.slug ?: return
        val subjectId = s.subjectId ?: return
        playItem(slug, subjectId, season, episode, resumePositionMs = 0L)
    }

    fun resetEpisodeProgress(subjectId: String, season: Int, episode: Int, isSeries: Boolean) {
        viewModelScope.launch {
            repository.resetEpisodeProgress(subjectId, season, episode, isSeries)
        }
    }

    fun saveProgress(posMs: Long, durMs: Long) {
        val s = _detailPlayerState.value
        val subId = s.session?.subjectId ?: s.subjectId ?: return
        val slug = s.slug ?: return
        val title = s.title.ifBlank { s.detail?.data?.subject?.title ?: "Movie" }
        val poster = s.posterUrl?.takeIf { it.isNotBlank() } ?: s.detail?.data?.subject?.cover?.url
        val type = if (s.isSeries) "tv" else "movie"
        val season = s.session?.season ?: s.selectedSeason
        val episode = s.session?.episode ?: s.selectedEpisode

        viewModelScope.launch {
            repository.saveWatchProgress(
                subjectId = subId,
                slug = slug,
                title = title,
                posterUrl = poster,
                season = season,
                episode = episode,
                positionMs = posMs,
                durationMs = durMs,
                type = type
            )
        }
    }

    fun toggleWatchlist() {
        val s = _detailPlayerState.value
        val subId = s.subjectId ?: return
        val slug = s.slug ?: return
        val subject = s.detail?.data?.subject
        val title = s.title.ifBlank { subject?.title ?: "Movie" }
        val poster = s.posterUrl?.takeIf { it.isNotBlank() } ?: subject?.cover?.url
        val rating = subject?.imdbRatingValue
        val year = subject?.releaseDate?.take(4)
        val genre = subject?.genre
        val type = if (s.isSeries) "tv" else "movie"

        viewModelScope.launch {
            repository.toggleWatchlist(
                subjectId = subId,
                slug = slug,
                title = title,
                posterUrl = poster,
                rating = rating,
                year = year,
                genre = genre,
                type = type,
                currentlyWatchlisted = s.isWatchlisted
            )
        }
    }

    fun deleteHistory(subjectId: String) {
        viewModelScope.launch {
            repository.deleteHistory(subjectId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun closePlayer() {
        playerInstance?.pause()
        playerInstance?.stop()
        _detailPlayerState.update { it.copy(isPlaying = false) }
    }

    fun closeDetail() {
        playerInstance?.pause()
        playerInstance?.stop()
        _detailPlayerState.value = DetailPlayerUiState()
    }

    override fun onCleared() {
        super.onCleared()
        playerInstance?.release()
        playerInstance = null
    }
}
