package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.MovieItem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.ui.theme.LocalHighContrast


// ─────────────────────────────────────────────────────────────────────────────
//  MoviePosterCard — Liquid glass poster card with authentic brand accents
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MoviePosterCard(
    movie: MovieItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 135.dp,
    cardHeight: Dp = 200.dp
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .width(cardWidth)
            .testTag("movie_card_${movie.safeSlug}")
    ) {
        PosterImage(
            imageUrl = movie.posterUrl,
            contentDescription = movie.safeName,
            aspectRatio = 2f / 3f,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            // Bottom gradient fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.90f)) // legibility-ok: thumbnail gradient scrim
                        )
                    )
            )

            // Rating badge — top right
            if (!movie.rating.isNullOrBlank()) {
                PosterBadge(
                    text = "★ ${movie.rating}",
                    modifier = Modifier
                        .padding(7.dp)
                        .align(Alignment.TopEnd)
                )
            }

            // Type/badge pill — top left
            val badgeText = movie.badge?.takeIf { it.isNotBlank() } ?: movie.type?.uppercase()
            if (!badgeText.isNullOrBlank()) {
                PosterBadge(
                    text = badgeText,
                    modifier = Modifier
                        .padding(7.dp)
                        .align(Alignment.TopStart)
                )
            }
        }

        // Title + meta below poster
        Spacer(Modifier.height(7.dp))
        Text(
            text = movie.safeName,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp
        )
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = movie.year ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = movie.genre?.split(",")?.firstOrNull()?.trim() ?: "",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 70.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ContinueWatchingCard — horizontal progress card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ContinueWatchingCard(
    history: WatchHistoryEntity,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cw_scale"
    )

    val isHighContrast = LocalHighContrast.current
    val border = if (isHighContrast) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)

    Column(
        modifier = modifier
            .width(210.dp)
            .scale(scale)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(border, shape)
            .clickable(interactionSource = interactionSource, indication = null) { onPlayClick() }
            .testTag("continue_card_${history.subjectId}")
    ) {
        // Thumbnail section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
        ) {
            PosterImage(
                imageUrl = history.posterUrl,
                contentDescription = history.title,
                aspectRatio = 16f / 9f,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.80f)) // legibility-ok: thumbnail gradient scrim
                            )
                        )
                )

                // Brand Gradient Play Button in Center
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onPlayClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Delete button top right
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0x99000000)) // legibility-ok: delete icon backdrop scrim
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White, // legibility-ok: icon over thumbnail scrim
                        modifier = Modifier.size(14.dp)
                    )
                }

                // S:E badge
                if (history.season > 0 || history.episode > 0) {
                    PosterBadge(
                        text = "S${history.season}:E${history.episode}",
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopStart)
                    )
                }

                // Progress bar at bottom of thumbnail
                val progress = if (history.durationMs > 0) {
                    (history.positionMs.toFloat() / history.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Info section
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = history.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            val remainingMins = ((history.durationMs - history.positionMs) / 60000L).coerceAtLeast(0)
            Text(
                text = if (remainingMins > 0) "${remainingMins}m remaining" else "Watched",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.5.sp
            )
        }
    }
}
