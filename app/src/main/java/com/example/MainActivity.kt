package com.example

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.components.DevCreditDialog
import com.example.ui.screens.catalog.CatalogScreen
import com.example.ui.screens.detail.DetailScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.theme.*
import com.example.util.DevCreditManager

class MainActivity : ComponentActivity() {
    var isInPipMode by mutableStateOf(false)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel<MainViewModel>()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            CinePulseTheme(mode = themeMode) {
                MainApp(viewModel = viewModel, isInPipMode = isInPipMode)
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
        if (!isInPictureInPictureMode) {
            // When leaving PiP, if the user closed/dismissed the popup window, immediately halt playback
            if (isFinishing || !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
                viewModel.closePlayer()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop playback when activity is stopped and not actively displaying PiP
        if (!isInPipMode || isFinishing) {
            val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
            viewModel.closePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.closePlayer()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        if (viewModel.detailPlayerState.value.isPlaying) {
            enterPipMode()
        }
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel(),
    isInPipMode: Boolean = false
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailPlayerState.collectAsStateWithLifecycle()

    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()

    val isDetailOpen = detailState.slug != null
    val isFullscreenPlaying = isLandscape && detailState.isPlaying

    val exoPlayer = remember(context) { viewModel.getPlayer(context) }

    // Developer credit dialog on app start with 7-day snooze capability (hidden in PiP mode)
    var showDevCreditDialog by remember {
        mutableStateOf(!isInPipMode && DevCreditManager.shouldShowDialog(context))
    }
    var showAppearanceScreen by remember { mutableStateOf(false) }

    if (showDevCreditDialog && !isInPipMode) {
        DevCreditDialog(
            onDismiss = { showDevCreditDialog = false },
            onSnooze7Days = {
                DevCreditManager.snoozeFor7Days(context)
                showDevCreditDialog = false
            }
        )
    }

    // Handle back button: In landscape, exit fullscreen back to portrait first
    BackHandler(enabled = !isInPipMode && (isLandscape || isDetailOpen || showAppearanceScreen || currentTab != AppTab.HOME)) {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (showAppearanceScreen) {
            showAppearanceScreen = false
        } else if (isDetailOpen) {
            viewModel.closeDetail()
        } else {
            viewModel.selectTab(AppTab.HOME)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isInPipMode && !isDetailOpen && !isFullscreenPlaying) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    NavigationBarItem(
                        selected = !showAppearanceScreen && currentTab == AppTab.HOME,
                        onClick = {
                            showAppearanceScreen = false
                            viewModel.selectTab(AppTab.HOME)
                        },
                        icon = { Icon(if (!showAppearanceScreen && currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = !showAppearanceScreen && currentTab == AppTab.CATALOG,
                        onClick = {
                            showAppearanceScreen = false
                            viewModel.selectTab(AppTab.CATALOG)
                        },
                        icon = { Icon(if (!showAppearanceScreen && currentTab == AppTab.CATALOG) Icons.Filled.Explore else Icons.Outlined.Explore, contentDescription = "Browse") },
                        label = { Text("Browse") },
                        modifier = Modifier.testTag("nav_browse")
                    )
                    NavigationBarItem(
                        selected = !showAppearanceScreen && currentTab == AppTab.SEARCH,
                        onClick = {
                            showAppearanceScreen = false
                            viewModel.selectTab(AppTab.SEARCH)
                        },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        modifier = Modifier.testTag("nav_search")
                    )
                    NavigationBarItem(
                        selected = !showAppearanceScreen && currentTab == AppTab.LIBRARY,
                        onClick = {
                            showAppearanceScreen = false
                            viewModel.selectTab(AppTab.LIBRARY)
                        },
                        icon = { Icon(if (!showAppearanceScreen && currentTab == AppTab.LIBRARY) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Library") },
                        label = { Text("Library") },
                        modifier = Modifier.testTag("nav_library")
                    )
                    NavigationBarItem(
                        selected = showAppearanceScreen,
                        onClick = { showAppearanceScreen = true },
                        icon = { Icon(if (showAppearanceScreen) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        // Edge-to-edge content: screens manage their own top status bar clearance to avoid top black voids
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (showAppearanceScreen) {
                com.example.ui.screens.appearance.AppearanceScreen(
                    currentThemeMode = viewModel.themeMode.collectAsStateWithLifecycle().value,
                    onThemeSelected = { viewModel.setThemeMode(it) },
                    onBackClick = { showAppearanceScreen = false }
                )
            } else if (isDetailOpen) {
                DetailScreen(
                    uiState = detailState,
                    exoPlayer = exoPlayer,
                    onBackClick = {
                        if (isLandscape) {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                        viewModel.closeDetail()
                    },
                    onPlayClick = { se, ep ->
                        val s = detailState.slug
                        val id = detailState.subjectId
                        if (s != null && id != null) {
                            viewModel.playItem(s, id, se, ep)
                        }
                    },
                    onSelectEpisode = { se, ep ->
                        viewModel.selectEpisode(se, ep)
                    },
                    onToggleWatchlist = { viewModel.toggleWatchlist() },
                    onProgressUpdate = { pos, dur ->
                        viewModel.saveProgress(pos, dur)
                    },
                    onFetchStreamForDownload = { se, ep ->
                        val slug = detailState.slug
                        val id = detailState.subjectId
                        if (slug != null && id != null) {
                            viewModel.getStreamForDownload(id, slug, se, ep)
                        } else {
                            emptyList()
                        }
                    }
                )
            } else {
                when (currentTab) {
                    AppTab.HOME -> {
                        HomeScreen(
                            uiState = homeState,
                            watchHistory = watchHistory,
                            onMovieClick = { movie ->
                                viewModel.openDetail(movie.safeSlug, movie.subjectId, movie.safeName, movie.type)
                            },
                            onWatchHeroClick = { movie ->
                                val isSeries = movie.isTvSeries
                                viewModel.openDetail(movie.safeSlug, movie.subjectId, movie.safeName, movie.type)
                                viewModel.playItem(
                                    slug = movie.safeSlug,
                                    subjectId = movie.subjectId,
                                    season = if (isSeries) 1 else 0,
                                    episode = if (isSeries) 1 else 0
                                )
                            },
                            onResumeHistoryClick = { history ->
                                viewModel.openDetail(history.slug, history.subjectId, history.title, history.type)
                                viewModel.playItem(history.slug, history.subjectId, history.season, history.episode, history.positionMs)
                            },
                            onDeleteHistoryClick = { subId ->
                                viewModel.deleteHistory(subId)
                            },
                            onRetry = { viewModel.loadHomeFeed() },
                            onSearchClick = { viewModel.selectTab(AppTab.SEARCH) }
                        )
                    }

                    AppTab.CATALOG -> {
                        CatalogScreen(
                            uiState = catalogState,
                            onTypeSelected = { type -> viewModel.setCatalogType(type) },
                            onGenreSelected = { genre -> viewModel.setCatalogGenre(genre) },
                            onSortSelected = { sort -> viewModel.setCatalogSort(sort) },
                            onMovieClick = { movie ->
                                viewModel.openDetail(movie.safeSlug, movie.subjectId, movie.safeName, movie.type)
                            },
                            onLoadMore = { viewModel.loadCatalog(reset = false) }
                        )
                    }

                    AppTab.SEARCH -> {
                        SearchScreen(
                            uiState = searchState,
                            onQueryChanged = { q -> viewModel.onSearchQueryChanged(q) },
                            onSearchSubmit = { q -> viewModel.performSearch(q) },
                            onMovieClick = { movie ->
                                viewModel.openDetail(movie.safeSlug, movie.subjectId, movie.safeName, movie.type)
                            }
                        )
                    }

                    AppTab.LIBRARY -> {
                        LibraryScreen(
                            watchlist = watchlist,
                            watchHistory = watchHistory,
                            onMovieClick = { movie ->
                                viewModel.openDetail(movie.safeSlug, movie.subjectId, movie.safeName, movie.type)
                            },
                            onResumeHistoryClick = { history ->
                                viewModel.openDetail(history.slug, history.subjectId, history.title, history.type)
                                viewModel.playItem(history.slug, history.subjectId, history.season, history.episode, history.positionMs)
                            },
                            onDeleteHistoryItem = { subId ->
                                viewModel.deleteHistory(subId)
                            },
                            onClearHistory = { viewModel.clearAllHistory() },
                            onExploreClick = { viewModel.selectTab(AppTab.HOME) },
                            onOpenDevCredit = { showDevCreditDialog = true },
                            onAppearanceClick = { showAppearanceScreen = true }
                        )
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}


