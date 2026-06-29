package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Patient list row with avatar circle, name, and last-visit subtitle.
 *
 * When [avatarUrl] is null the avatar falls back to a circle with the
 * patient's initials (no image loading dependency in shared module).
 *
 * Triangulation skipped: pure Composable, no extractable logic.
 *
 * @param name      Patient full name.
 * @param lastVisit Formatted last-visit string (e.g. "15 may 2025").
 * @param avatarUrl Optional remote avatar URL (reserved for platform-specific rendering).
 * @param onClick   Click handler.
 */
@Composable
fun PatientRow(
    name: String,
    lastVisit: String,
    avatarUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    val initials = name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier              = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.navyTint),
        ) {
            Text(
                text       = initials,
                color      = colors.navy,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(dimens.spacingMd))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = name,
                style      = AppTheme.typography.body,
                fontWeight = FontWeight.Bold,
                color      = colors.text,
            )
            Text(
                text  = "Última visita: $lastVisit",
                style = AppTheme.typography.subtitle,
                color = colors.muted,
            )
        }
    }
}
