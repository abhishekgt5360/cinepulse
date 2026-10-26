package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.MovieItem
import com.example.ui.HomeUiState
import com.example.ui.components.*

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    watchHistory: List<WatchHistoryEntity>,
    onMovieClick: (MovieItem) -> Unit,
    onWatchHeroClick: (MovieItem) -> Unit,
    onResumeHistoryClick: (WatchHistoryEntity) -> Unit,
    onDeleteHistoryClick: (String) -> Unit,
    onRetry: () -> Unit,
    onSearchClick: () -> Unit = {},
    onSeeAllClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Loading state
    if (uiState.isLoading && uiState.sections.isEmpty() && uiState.bannerItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(38.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Loading Zippy...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.5.sp
                )
            }
        }
        return
    }

    // Error state
    if (uiState.errorMessage != null && uiState.sections.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Connection Error",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    LiquidGlassButton(
                        text = "Retry",
                        onClick = onRetry,
                        icon = Icons.Default.Refresh,
                        modifier = Modifier.width(140.dp)
                    )
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_feed_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Full-Bleed Immersive Hero Banner (Extends under top bar) ──────
            if (uiState.bannerItems.isNotEmpty()) {
                item {
                    HeroBanner(
                        items = uiState.bannerItems,
                        onItemClick = onMovieClick,
                        onWatchClick = onWatchHeroClick
                    )
                }
            }

            // ── Continue Watching ────────────────────────────────────────────
            if (watchHistory.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp)
                                .clickable { onSeeAllClick("History") },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Continue Watching",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "See All History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(watchHistory, key = { it.subjectId }) { historyItem ->
                                ContinueWatchingCard(
                                    history = historyItem,
                                    onPlayClick = { onResumeHistoryClick(historyItem) },
                                    onDeleteClick = { onDeleteHistoryClick(historyItem.subjectId) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Content Sections ─────────────────────────────────────────────
            items(uiState.sections, key = { it.section }) { section ->
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    // Section heading with brand gradient accent
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = section.section,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(
                            onClick = { onSeeAllClick(section.section) }
                        ) {
                            Text(
                                text = "See all",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    val uniqueItems = remember(section.items) {
                        section.items.distinctBy { "${it.safeSlug}_${it.subjectId}" }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uniqueItems, key = { "${it.safeSlug}_${it.subjectId}" }) { movie ->
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                }
            }
        }

        // ── Floating Immersive Top Header Overlay ───────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.75f), // legibility-ok: top bar vignette scrim
                            Color.Black.copy(alpha = 0.35f), // legibility-ok: top bar vignette scrim
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Official Zippy Logo
            CinePulseLogo(iconSize = 30.dp)

            // Top right search & notification actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)) // legibility-ok: search icon circle
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White, // legibility-ok: search icon tint
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { /* Notifications */ },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)) // legibility-ok: notification icon circle
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White, // legibility-ok: notification icon tint
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
