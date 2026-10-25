package com.example.ui.screens.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MovieItem
import com.example.ui.CatalogType
import com.example.ui.CatalogUiState
import com.example.util.RegionalCategoryConfig
import com.example.ui.components.GlassChip
import com.example.ui.components.MoviePosterCard
import com.example.ui.theme.LocalHighContrast
import com.example.ui.theme.selectableColors


private val GENRES = listOf(
    "All", "Action", "Drama", "Comedy", "Sci-Fi", "Horror",
    "Thriller", "Adventure", "Romance", "Fantasy", "Crime", "Mystery", "Animation"
)

private val SORTS = listOf(
    "foryou"  to "For You",
    "hottest" to "Trending",
    "latest"  to "Latest",
    "rating"  to "Top Rated"
)

@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onTypeSelected: (CatalogType) -> Unit,
    onRegionSelected: (String) -> Unit,
    onGenreSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onMovieClick: (MovieItem) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // Load more when reaching bottom of catalog grid
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 6 && !uiState.isLoading && uiState.hasMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ── Floating glass page header ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Explore & Browse",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Segmented Switcher: Movies | TV Series | Animation ──
                val isHighContrast = LocalHighContrast.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(
                            width = if (isHighContrast) 1.5.dp else 0.5.dp,
                            color = if (isHighContrast) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CatalogType.entries.forEach { catalogType ->
                        val isSelected = uiState.type == catalogType
                        val selColors = selectableColors(isSelected)
                        val shape = RoundedCornerShape(11.dp)
                        val containerColor = if (isSelected) selColors.container else Color.Transparent
                        val contentColor = selColors.content
                        val borderWidth = if (isHighContrast) 1.5.dp else if (isSelected) 0.5.dp else 0.dp
                        val border = if (borderWidth > 0.dp) BorderStroke(borderWidth, selColors.border) else null

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .background(containerColor)
                                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                                .clickable { onTypeSelected(catalogType) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isSelected && isHighContrast) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    text = catalogType.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = contentColor,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Region, Sort & Genre filter chips ─────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            // Region chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegionalCategoryConfig.REGION_PRESETS.forEach { regionPreset ->
                    val isSelected = uiState.selectedRegion == regionPreset.label
                    GlassChip(
                        label = regionPreset.label,
                        selected = isSelected,
                        onClick = { onRegionSelected(regionPreset.label) }
                    )
                }
            }

            // Sort chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SORTS.forEach { (sortKey, label) ->
                    val isSelected = uiState.selectedSort == sortKey
                    GlassChip(
                        label = label,
                        selected = isSelected,
                        onClick = { onSortSelected(sortKey) }
                    )
                }
            }

            // Genre chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GENRES.forEach { genre ->
                    val isSelected = uiState.selectedGenre == genre
                    GlassChip(
                        label = genre,
                        selected = isSelected,
                        onClick = { onGenreSelected(genre) }
                    )
                }
            }
        }

        // ── Movie Grid ────────────────────────────────────────────────────────
        if (uiState.items.isEmpty() && uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(40.dp)
                )
            }
        } else if (uiState.items.isEmpty() && uiState.errorMessage != null) {
            // Glass error panel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(28.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Unable to Load",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val uniqueItems = remember(uiState.items) {
                uiState.items.distinctBy { "${it.safeSlug}_${it.subjectId}" }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                state = gridState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("catalog_grid")
            ) {
                items(
                    count = uniqueItems.size,
                    key = { index ->
                        "${uniqueItems[index].safeSlug}_${uniqueItems[index].subjectId}_$index"
                    }
                ) { index ->
                    val movie = uniqueItems[index]
                    MoviePosterCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        cardWidth = 110.dp,
                        cardHeight = 165.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom pagination spinner
                if (uiState.isLoading && uiState.items.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
