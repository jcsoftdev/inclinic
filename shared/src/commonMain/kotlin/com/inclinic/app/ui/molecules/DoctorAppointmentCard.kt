package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.theme.AppTheme

/**
 * Appointment card for the doctor's agenda list.
 *
 * Shows an avatar circle with the patient's initials, the patient name and
 * appointment time stacked on the left, and a status chip on the right.
 *
 * Triangulation skipped: pure Composable, no extractable logic.
 *
 * @param patientName Patient full name; initials are derived from first two words.
 * @param time        Formatted appointment time string (e.g. "10:00 AM").
 * @param status      Status label displayed in the badge (e.g. "Confirmado").
 * @param isVirtual   When true, a "(Virtual)" suffix is shown beside the time.
 * @param onClick     Click handler for the entire card.
 */
@Composable
fun DoctorAppointmentCard(
    patientName: String,
    time: String,
    status: String,
    isVirtual: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    val initials = patientName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    val badgeTone = when (status.lowercase()) {
        "confirmado", "confirmada" -> AppBadgeTone.Success
        "pendiente"                -> AppBadgeTone.Warning
        "cancelado", "cancelada"   -> AppBadgeTone.Error
        "virtual"                  -> AppBadgeTone.Info
        else                       -> AppBadgeTone.Neutral
    }

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier              = modifier
            .fillMaxWidth()
            .background(colors.surface, AppTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar circle with initials
            androidx.compose.foundation.layout.Box(
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
                    text       = patientName,
                    style      = AppTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    color      = colors.text,
                )
                val timeLabel = if (isVirtual) "$time · Virtual" else time
                Text(
                    text  = timeLabel,
                    style = AppTheme.typography.subtitle,
                    color = colors.muted,
                )
            }
        }

        AppBadge(text = status, tone = badgeTone)
    }
}
