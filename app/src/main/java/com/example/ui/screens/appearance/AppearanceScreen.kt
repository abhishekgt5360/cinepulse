package com.example.ui.screens.appearance

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Crimson Option
            ThemeOptionCard(
                title = "Crimson",
                subtitle = "Default dark theme with crimson accents",
                selected = currentThemeMode == ThemeMode.CRIMSON,
                onClick = { onThemeSelected(ThemeMode.CRIMSON) },
                primarySwatch = Color(0xFFFFB2BC),
                backgroundSwatch = Color(0xFF1A1112)
            )

            // OLED Option
            ThemeOptionCard(
                title = "OLED black",
                subtitle = "Pitch black background for AMOLED screens",
                selected = currentThemeMode == ThemeMode.OLED,
                onClick = { onThemeSelected(ThemeMode.OLED) },
                primarySwatch = Color(0xFFFF5A78),
                backgroundSwatch = Color(0xFF000000)
            )

            // System Colors Option (Dynamic)
            val isDynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ThemeOptionCard(
                title = "System colors",
                subtitle = if (isDynamicSupported) "Uses dynamic colors from your wallpaper" else "Requires Android 12 or higher",
                selected = currentThemeMode == ThemeMode.DYNAMIC && isDynamicSupported,
                enabled = isDynamicSupported,
                onClick = {
                    if (isDynamicSupported) {
                        onThemeSelected(ThemeMode.DYNAMIC)
                    }
                },
                primarySwatch = MaterialTheme.colorScheme.primary,
                backgroundSwatch = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    primarySwatch: Color,
    backgroundSwatch: Color,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Swatch Preview
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundSwatch)
                    .border(2.dp, primarySwatch, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(primarySwatch)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = selected,
                onClick = null, // handled by Card click
                enabled = enabled
            )
        }
    }
}
