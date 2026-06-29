package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppTheme

enum class AppBadgeTone { Neutral, Success, Warning, Error, Info }

@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: AppBadgeTone = AppBadgeTone.Neutral,
) {
    val colors = AppTheme.colors
    val (bg, fg) = when (tone) {
        AppBadgeTone.Neutral -> colors.lav50 to colors.muted
        AppBadgeTone.Success -> colors.successBg to colors.green
        AppBadgeTone.Warning -> colors.amberBg to colors.amber
        AppBadgeTone.Error   -> colors.redBg to colors.red
        AppBadgeTone.Info    -> colors.infoBg to colors.navy
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(bg, AppTheme.shapes.pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
