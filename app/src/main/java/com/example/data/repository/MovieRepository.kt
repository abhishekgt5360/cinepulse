package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.EpisodeProgressEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.*
import com.example.data.remote.MovieApiService
import com.example.data.remote.NetworkClient
import com.example.util.CatalogPreset
import com.example.util.RegionalCategoryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MovieRepository(
    private val api: MovieApiService = NetworkClient.api,
    private val database: AppDatabase,
    private val context: android.content.Context? = null
) {
    private val dao = database.watchDao()
    private val homeJsonAdapter by lazy { NetworkClient.moshi.adapter(HomeResponse::class.java) }
    private val catalogJsonAdapter by lazy { NetworkClient.moshi.adapter(CatalogResponse::class.java) }
    private val cacheFile by lazy {
        context?.cacheDir?.resolve("cached_home_feed.json")
    }
    private val bollywoodCacheFile by lazy {
        context?.cacheDir?.resolve("cached_bollywood_feed.json")
    }

    private fun writeCacheAtomically(file: java.io.File?, content: String) {
        if (file == null) return
        try {
            val tmpFile = java.io.File(file.parentFile, "${file.name}.tmp")
            tmpFile.writeText(content)
            if (tmpFile.exists()) {
                if (file.exists()) {
                    file.delete()
                }
                tmpFile.renameTo(file)
            }
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile private var inMemoryHome: HomeResponse? = null
        private val catalogCache = java.util.concurrent.ConcurrentHashMap<String, CatalogResponse>()
    }

    fun getCachedHome(): HomeResponse? {
        inMemoryHome?.let { return it }
        try {
            val file = cacheFile
            if (file != null && file.exists()) {
                val json = file.readText()
                val parsed = homeJsonAdapter.fromJson(json)
                if (parsed != null) {
                    inMemoryHome = parsed
                    return parsed
                }
            }
        } catch (_: Exception) {}
        return null
    }

    suspend fun getHome(): Result<HomeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getHome()
            if (!response.sections.isNullOrEmpty()) {
                inMemoryHome = response
                try {
                    cacheFile?.writeText(homeJsonAdapter.toJson(response))
                } catch (_: Exception) {}
            }
            Result.success(response)
        } catch (e: Exception) {
            val cached = getCachedHome()
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    fun getCachedCatalog(key: String): CatalogResponse? {
        return catalogCache[key]
    }

    fun getCachedBollywood(): CatalogResponse? {
        try {
            val file = bollywoodCacheFile
            if (file != null && file.exists() && file.length() > 0) {
                val json = file.readText()
                val parsed = catalogJsonAdapter.fromJson(json)
                if (parsed != null && !parsed.items.isNullOrEmpty()) {
                    return parsed
                }
            }
        } catch (_: Exception) {}
        return null
    }

    suspend fun getRegionCatalog(
        type: com.example.ui.CatalogType = com.example.ui.CatalogType.MOVIES,
        country: String? = null,
        genre: String? = null,
        sort: String? = null,
        page: Int = 1
    ): Result<CatalogResponse> = withContext(Dispatchers.IO) {
        val cacheKey = "${type.name.lowercase()}_${page}_${sort}_${genre}_${country}"
        try {
            val response = when (type) {
                com.example.ui.CatalogType.MOVIES -> api.getMovies(page = page, sort = sort, genre = genre, country = country)
                com.example.ui.CatalogType.TV_SERIES -> api.getTvSeries(page = page, sort = sort, genre = genre, country = country)
                com.example.ui.CatalogType.ANIMATION -> api.getAnimation(page = page, sort = sort, genre = genre)
            }

            if (response.items.isNotEmpty()) {
                catalogCache[cacheKey] = response
                // Write cached_bollywood_feed.json ONLY for page 1, country != null, and unfiltered by genre/sort
                if (page == 1 && country != null && genre == null && sort == null && type == com.example.ui.CatalogType.MOVIES) {
                    writeCacheAtomically(bollywoodCacheFile, catalogJsonAdapter.toJson(response))
                }
            }
            Result.success(response)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            catalogCache[cacheKey]?.let { return@withContext Result.success(it) }
            if (page == 1 && country != null && genre == null && sort == null && type == com.example.ui.CatalogType.MOVIES) {
                val cached = getCachedBollywood()
                if (cached != null) {
                    return@withContext Result.success(cached)
                }
            }
            Result.failure(e)
        }
    }

    suspend fun getMovies(page: Int, sort: String?, genre: String?, country: String? = null): Result<CatalogResponse> =
        getRegionCatalog(type = com.example.ui.CatalogType.MOVIES, country = country, genre = genre, sort = sort, page = page)

    suspend fun getTvSeries(page: Int, sort: String?, genre: String?, country: String? = null): Result<CatalogResponse> =
        getRegionCatalog(type = com.example.ui.CatalogType.TV_SERIES, country = country, genre = genre, sort = sort, page = page)

    suspend fun getAnimation(page: Int, sort: String?, genre: String?): Result<CatalogResponse> =
        getRegionCatalog(type = com.example.ui.CatalogType.ANIMATION, genre = genre, sort = sort, page = page)

    suspend fun getBollywoodMovies(page: Int = 1): Result<CatalogResponse> =
        getRegionCatalog(type = com.example.ui.CatalogType.MOVIES, country = "India", page = page)

    suspend fun getPresetCatalog(
        preset: CatalogPreset,
        type: com.example.ui.CatalogType = com.example.ui.CatalogType.MOVIES,
        genre: String? = null,
        sort: String? = null,
        page: Int = 1
    ): Result<CatalogResponse> = withContext(Dispatchers.IO) {
        if (preset.countries.isNotEmpty()) {
            try {
                val countryResponses = coroutineScope {
                    preset.countries.map { countryName ->
                        async {
                            getRegionCatalog(
                                type = type,
                                country = countryName,
                                genre = preset.genre ?: genre,
                                sort = sort,
                                page = page
                            ).getOrNull()
                        }
                    }.awaitAll().filterNotNull()
                }

                if (countryResponses.isEmpty()) {
                    return@withContext Result.failure(java.io.IOException("Failed to load multi-country catalog"))
                }

                val lists = countryResponses.map { it.items }
                val maxListLen = lists.maxOfOrNull { it.size } ?: 0
                val interleaved = mutableListOf<MovieItem>()
                val seenIds = mutableSetOf<String>()

                for (i in 0 until maxListLen) {
                    for (list in lists) {
                        if (i < list.size) {
                            val item = list[i]
                            val id = item.subjectId.ifBlank { item.safeSlug }
                            if (id.isNotBlank() && seenIds.add(id)) {
                                interleaved.add(item)
                            }
                        }
                    }
                }

                val hasMore = countryResponses.any { it.hasMore == true }
                Result.success(CatalogResponse(page = page, hasMore = hasMore, items = interleaved))
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Result.failure(e)
            }
        } else {
            getRegionCatalog(
                type = type,
                country = preset.country,
                genre = preset.genre ?: genre,
                sort = sort,
                page = page
            )
        }
    }

    suspend fun search(query: String, page: Int = 1): Result<SearchResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.search(query = query, page = page)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSuggestions(query: String): Result<SuggestResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getSuggestions(query)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getDetail(slug: String): Result<DetailResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getDetail(slug)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getStream(
        subjectId: String,
        slug: String,
        season: Int,
        episode: Int,
        isSeries: Boolean = false
    ): Result<StreamResponse> = withContext(Dispatchers.IO) {
        try {
            // For movies, Zar MovieBox API explicitly expects se=0, ep=0!
            val initialSe = if (isSeries) season.coerceAtLeast(1) else 0
            val initialEp = if (isSeries) episode.coerceAtLeast(1) else 0

            var response = try {
                api.getStream(subjectId = subjectId, detailPath = slug, season = initialSe, episode = initialEp)
            } catch (e: Exception) {
                StreamResponse(subjectId = subjectId, hasResource = false, sources = emptyList())
            }

            if (!response.hasResource || response.sources.isEmpty()) {
                val fallbacks = if (isSeries) {
                    listOf(Pair(season, episode), Pair(0, 0), Pair(1, 1), Pair(0, 1), Pair(1, 0))
                } else {
                    listOf(Pair(0, 0), Pair(1, 1), Pair(0, 1), Pair(1, 0))
                }.filter { it.first != initialSe || it.second != initialEp }

                for ((s, e) in fallbacks) {
                    try {
                        val alt = api.getStream(subjectId = subjectId, detailPath = slug, season = s, episode = e)
                        if (alt.hasResource && alt.sources.isNotEmpty()) {
                            response = alt
                            break
                        }
                    } catch (_: Exception) {}
                }
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCaptions(
        subjectId: String,
        slug: String,
        season: Int,
        episode: Int,
        isSeries: Boolean = false
    ): Result<CaptionsResponse> = withContext(Dispatchers.IO) {
        try {
            val initialSe = if (isSeries) season.coerceAtLeast(1) else 0
            val initialEp = if (isSeries) episode.coerceAtLeast(1) else 0

            var response = try {
                api.getCaptions(subjectId = subjectId, detailPath = slug, season = initialSe, episode = initialEp)
            } catch (e: Exception) {
                CaptionsResponse(subjectId = subjectId, captions = emptyList())
            }

            if (response.captions.isEmpty()) {
                val fallbacks = if (isSeries) {
                    listOf(Pair(season, episode), Pair(0, 0), Pair(1, 1), Pair(0, 1))
                } else {
                    listOf(Pair(0, 0), Pair(1, 1), Pair(0, 1))
                }.filter { it.first != initialSe || it.second != initialEp }

                for ((s, e) in fallbacks) {
                    try {
                        val alt = api.getCaptions(subjectId = subjectId, detailPath = slug, season = s, episode = e)
                        if (alt.captions.isNotEmpty()) {
                            response = alt
                            break
                        }
                    } catch (_: Exception) {}
                }
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Local Watch History
    val watchHistory: Flow<List<WatchHistoryEntity>> = dao.getAllHistory()

    suspend fun getHistoryItem(subjectId: String): WatchHistoryEntity? = withContext(Dispatchers.IO) {
        try {
            dao.getHistory(subjectId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEpisodeProgress(
        subjectId: String,
        season: Int,
        episode: Int,
        isSeries: Boolean
    ): Long = withContext(Dispatchers.IO) {
        if (subjectId.isBlank()) return@withContext 0L
        val se = if (isSeries) season.coerceAtLeast(1) else 0
        val ep = if (isSeries) episode.coerceAtLeast(1) else 0
        try {
            val epProgress = dao.getEpisodeProgress(subjectId, se, ep)
            if (epProgress != null) {
                return@withContext com.example.util.ResumeManager.calculateStartPosition(
                    positionMs = epProgress.positionMs,
                    durationMs = epProgress.durationMs,
                    finished = epProgress.finished
                )
            }

            // Legacy Fallback: check watch_history
            val history = dao.getHistory(subjectId)
            if (history != null && history.season == se && history.episode == ep) {
                return@withContext com.example.util.ResumeManager.calculateStartPosition(
                    positionMs = history.positionMs,
                    durationMs = history.durationMs,
                    finished = false
                )
            }
        } catch (_: Exception) {}
        return@withContext 0L
    }

    suspend fun resetEpisodeProgress(
        subjectId: String,
        season: Int,
        episode: Int,
        isSeries: Boolean
    ) = withContext(Dispatchers.IO) {
        if (subjectId.isBlank()) return@withContext
        val se = if (isSeries) season.coerceAtLeast(1) else 0
        val ep = if (isSeries) episode.coerceAtLeast(1) else 0
        try {
            dao.upsertEpisodeProgress(
                EpisodeProgressEntity(
                    subjectId = subjectId,
                    season = se,
                    episode = ep,
                    positionMs = 0L,
                    durationMs = 0L,
                    updatedAt = System.currentTimeMillis(),
                    finished = true
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun saveWatchProgress(
        subjectId: String,
        slug: String,
        title: String,
        posterUrl: String?,
        season: Int,
        episode: Int,
        positionMs: Long,
        durationMs: Long,
        type: String?
    ) = withContext(Dispatchers.IO) {
        if (subjectId.isBlank()) return@withContext
        val isSeries = type.equals("tv", ignoreCase = true) || type.equals("tv-series", ignoreCase = true)
        val se = if (isSeries) season.coerceAtLeast(1) else 0
        val ep = if (isSeries) episode.coerceAtLeast(1) else 0

        val finished = com.example.util.ResumeManager.isFinished(positionMs, durationMs)
        val shouldSave = com.example.util.ResumeManager.shouldSavePosition(positionMs, durationMs) || finished

        try {
            if (shouldSave) {
                val savePos = if (finished) 0L else positionMs
                dao.upsertEpisodeProgress(
                    EpisodeProgressEntity(
                        subjectId = subjectId,
                        season = se,
                        episode = ep,
                        positionMs = savePos,
                        durationMs = durationMs,
                        updatedAt = System.currentTimeMillis(),
                        finished = finished
                    )
                )

                dao.upsertHistory(
                    WatchHistoryEntity(
                        subjectId = subjectId,
                        slug = slug,
                        title = title,
                        posterUrl = posterUrl,
                        season = se,
                        episode = ep,
                        positionMs = savePos,
                        durationMs = durationMs,
                        lastWatchedTimestamp = System.currentTimeMillis(),
                        type = type
                    )
                )
            }
        } catch (_: Exception) {}
    }

    suspend fun deleteHistory(subjectId: String) = withContext(Dispatchers.IO) {
        try {
            dao.deleteHistory(subjectId)
            dao.deleteEpisodeProgressForSubject(subjectId)
        } catch (_: Exception) {}
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        try {
            dao.clearHistory()
            dao.clearEpisodeProgress()
        } catch (_: Exception) {}
    }

    suspend fun updateWatchlistPosterIfMissing(subjectId: String, posterUrl: String) = withContext(Dispatchers.IO) {
        if (posterUrl.isBlank()) return@withContext
        try {
            val existing = dao.getWatchlistItem(subjectId)
            if (existing != null && existing.posterUrl.isNullOrBlank()) {
                dao.addToWatchlist(existing.copy(posterUrl = posterUrl))
            }
        } catch (_: Exception) {}
    }

    suspend fun updateHistoryPosterIfMissing(subjectId: String, posterUrl: String) = withContext(Dispatchers.IO) {
        if (posterUrl.isBlank()) return@withContext
        try {
            val existing = dao.getHistory(subjectId)
            if (existing != null && existing.posterUrl.isNullOrBlank()) {
                dao.upsertHistory(existing.copy(posterUrl = posterUrl))
            }
        } catch (_: Exception) {}
    }

    // Local Watchlist
    val watchlist: Flow<List<WatchlistEntity>> = dao.getAllWatchlist()

    fun isWatchlisted(subjectId: String): Flow<Boolean> = dao.isWatchlisted(subjectId)

    suspend fun toggleWatchlist(
        subjectId: String,
        slug: String,
        title: String,
        posterUrl: String?,
        rating: String?,
        year: String?,
        genre: String?,
        type: String?,
        currentlyWatchlisted: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            if (currentlyWatchlisted) {
                dao.removeFromWatchlist(subjectId)
            } else {
                dao.addToWatchlist(
                    WatchlistEntity(
                        subjectId = subjectId,
                        slug = slug,
                        title = title,
                        posterUrl = posterUrl,
                        rating = rating,
                        year = year,
                        genre = genre,
                        type = type,
                        addedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        } catch (_: Exception) {}
    }
}
