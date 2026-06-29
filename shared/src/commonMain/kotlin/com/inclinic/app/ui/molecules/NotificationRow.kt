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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Notification list row with icon, title, body, and time.
 *
 * Unread notifications show a navy dot indicator and bold title.
 *
 * Triangulation skipped: pure Composable, no extractable logic.
 *
 * @param title   Notification headline.
 * @param body    Notification body text.
 * @param timeAgo Relative time string (e.g. "hace 5 min").
 * @param isRead  When false, shows a dot and bolds the title.
 * @param icon    Icon to display in the left icon circle.
 * @param iconBg  Background color of the icon circle. Defaults to [AppTheme.colors.lav50]
 *                so existing call-sites need no change; callers can pass per-type
 *                semantic colors (e.g. [AppTheme.colors.greenBg] for appointments).
 * @param onClick Click handler.
 */
@Composable
fun NotificationRow(
    title: String,
    body: String,
    timeAgo: String,
    isRead: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconBg: Color = AppTheme.colors.lav50,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier              = modifier
            .fillMaxWidth()
            .background(if (isRead) colors.surface else colors.navyTint)
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
    ) {
        // Icon circle — background driven by caller; default lav50 preserves prior look
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(40.dp)
                .background(iconBg, CircleShape),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = colors.navy,
                modifier           = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(dimens.spacingMd))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = title,
                style      = AppTheme.typography.body,
                fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                color      = colors.text,
            )
            Text(
                text  = body,
                style = AppTheme.typography.subtitle,
                color = colors.muted,
                maxLines = 2,
            )
            Text(
                text  = timeAgo,
                style = AppTheme.typography.caption,
                color = colors.light,
            )
        }

        // Unread dot
        if (!isRead) {
            Spacer(Modifier.width(dimens.spacingSm))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.navy, CircleShape),
            )
        }
    }
}
