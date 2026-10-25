package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalHighContrast

// ─────────────────────────────────────────────────────────────────────────────
// LiquidGlassCard -> Material 3 Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isHighContrast = LocalHighContrast.current
    val border = if (isHighContrast) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else null
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(cornerRadius),
            border = border,
            elevation = elevation,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(cornerRadius),
            border = border,
            elevation = elevation,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LiquidGlassGrip -> Bottom sheet drag handle
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LiquidGlassGrip(modifier: Modifier = Modifier) {
    val isHighContrast = LocalHighContrast.current
    Box(
        modifier = modifier
            .width(32.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (isHighContrast) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// LiquidGlassPill -> Badge / Chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LiquidGlassPill(
    label: String,
    modifier: Modifier = Modifier
) {
    val isHighContrast = LocalHighContrast.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = if (isHighContrast) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else null,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun LiquidGlassPill(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    val isHighContrast = LocalHighContrast.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = if (isHighContrast) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else null,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                style = textStyle,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LiquidGlassButton -> Primary Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LiquidGlassButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isHighContrast = LocalHighContrast.current
    val border = if (isHighContrast) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else null
    val elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        border = border,
        elevation = elevation,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LiquidGlassSecondaryButton -> Outlined Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LiquidGlassSecondaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighContrast = LocalHighContrast.current
    val border = if (isHighContrast) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else ButtonDefaults.outlinedButtonBorder(enabled = true)

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        border = border,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GlassChip -> FilterChip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighContrast = LocalHighContrast.current
    val border = if (isHighContrast) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else FilterChipDefaults.filterChipBorder(enabled = true, selected = selected)

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        border = border,
        colors = if (isHighContrast && selected) {
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            FilterChipDefaults.filterChipColors()
        },
        modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// GlassIconButton -> IconButton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val isHighContrast = LocalHighContrast.current
    val borderModifier = if (isHighContrast) Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(24.dp)) else Modifier

    IconButton(
        onClick = onClick,
        modifier = modifier.then(borderModifier)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}
