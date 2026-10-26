package com.example.ui.screens.appearance

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HighContrastScheme
import com.example.ui.theme.OledScheme
import com.example.ui.theme.PaperScheme
import com.example.ui.theme.ThemeMode

fun ThemeMode.getDisplayName(): String = when (this) {
    ThemeMode.CRIMSON -> "Crimson"
    ThemeMode.OLED -> "OLED Black"
    ThemeMode.DYNAMIC -> "System Colors"
    ThemeMode.HIGH_CONTRAST -> "High Contrast"
    ThemeMode.PAPER -> "Paper"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onBackClick: () -> Unit,
    isAdultEnabled: Boolean = false,
    onAdultToggle: (Boolean) -> Unit = {}
) {
    var showAdultConfirmDialog by remember { mutableStateOf(false) }
    var expandedThemeDropdown by remember { mutableStateOf(false) }
    val isDynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose theme",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedThemeDropdown,
                        onExpandedChange = { expandedThemeDropdown = it },
                        modifier = Modifier.width(160.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentThemeMode.getDisplayName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedThemeDropdown)
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = expandedThemeDropdown,
                            onDismissRequest = { expandedThemeDropdown = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val isSupported = mode != ThemeMode.DYNAMIC || isDynamicSupported
                                if (isSupported) {
                                    DropdownMenuItem(
                                        text = { Text(mode.getDisplayName(), color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            onThemeSelected(mode)
                                            expandedThemeDropdown = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adult content (18+)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Show 18+ adult content preset and filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAdultEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showAdultConfirmDialog = true
                            } else {
                                onAdultToggle(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }

        }
    }

    if (showAdultConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showAdultConfirmDialog = false },
            title = {
                Text(
                    text = "Adult Content Confirmation",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This shows content for adults only. Confirm that you are 18 or older.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdultConfirmDialog = false
                        onAdultToggle(true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("I am 18+", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAdultConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
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
    outlineSwatch: Color? = null,
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
                    .border(2.dp, outlineSwatch ?: primarySwatch, CircleShape),
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
