package com.example.ui.screens.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeQaScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme QA Inspector (Debug)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Section 1: Typography Showcase ────────────────────────────────
            Text("1. Typography Roles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Headline Large", style = MaterialTheme.typography.headlineLarge)
                    Text("Headline Small", style = MaterialTheme.typography.headlineSmall)
                    Text("Title Medium (onSurface)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Body Medium (onSurfaceVariant)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Label Small (tertiary)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }

            // ── Section 2: Shared Selectable Components ───────────────────────
            Text("2. Selectable Components State Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("GlassChips (Filter / Season / Page):", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassChip(label = "Selected", selected = true, onClick = {})
                        GlassChip(label = "Unselected", selected = false, onClick = {})
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Buttons:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiquidGlassButton(text = "Primary", icon = Icons.Default.PlayArrow, onClick = {})
                        LiquidGlassSecondaryButton(text = "Secondary", icon = Icons.Default.PlayArrow, onClick = {})
                    }
                    LiquidGlassButton(text = "Disabled Primary", icon = Icons.Default.PlayArrow, onClick = {}, enabled = false)
                }
            }

            // ── Section 3: Artwork Overlay Badges ────────────────────────────
            Text("3. Poster Badges over Light & Dark Sample Artwork", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Light Artwork Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0)) // legibility-ok: debug light sample image
                        .padding(8.dp)
                ) {
                    Text("Light Artwork Sample", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Center)) // legibility-ok: debug sample label
                    PosterBadge(text = "MOVIES", modifier = Modifier.align(Alignment.TopStart))
                    PosterBadge(text = "★ 8.5", modifier = Modifier.align(Alignment.TopEnd))
                }

                // Dark Artwork Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF101010)) // legibility-ok: debug dark sample image
                        .padding(8.dp)
                ) {
                    Text("Dark Artwork Sample", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Center)) // legibility-ok: debug sample label
                    PosterBadge(text = "SERIES", modifier = Modifier.align(Alignment.TopStart))
                    PosterBadge(text = "S1:E5", modifier = Modifier.align(Alignment.BottomStart))
                }
            }
        }
    }
}
