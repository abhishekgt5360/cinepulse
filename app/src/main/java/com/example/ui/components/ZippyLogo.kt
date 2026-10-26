package com.example.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Shared Zippy brand lockup (speed-play glyph + wordmark).
 *
 * The vector is single-color black, so it is always tinted in code. The default
 * [tint] follows the screen background content color; callers whose bar uses a
 * different container color should pass the matching content color.
 */
@Composable
fun ZippyLogo(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground
) {
    Icon(
        painter = painterResource(R.drawable.ic_zippy_lockup),
        contentDescription = "Zippy",
        tint = tint,
        modifier = modifier
            .height(28.dp)
            .aspectRatio(178f / 48f)
    )
}
