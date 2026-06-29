package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * iOS-style home indicator bar — 134×5 rounded pill centered at bottom.
 */
@Composable
fun HomeIndicator(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(bottom = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(134.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(AppTheme.colors.text),
        )
    }
}
