package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.User
import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.features.patient.presentation.component.HistoryAccessLogDetailComponent
import com.inclinic.app.ui.theme.AppTheme
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val SPANISH_MONTHS_SHORT = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

/**
 * Full "who / when / access type" breakdown for a single [HistoryAccessLog] entry.
 *
 * Reached by tapping a row on [HistoryAccessLogsScreen]. Renders only fields already
 * present on [HistoryAccessLog] — no extra network call, since
 * `GET /api/patients/me/access-log` already returned everything shown here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAccessLogDetailScreen(component: HistoryAccessLogDetailComponent, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val entry = component.entry
    val style = accessTypeStyle(entry.accessType)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle de acceso", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.sand)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Who ──────────────────────────────────────────────────────────
            DoctorCard(entry)

            // ── Access type ──────────────────────────────────────────────────
            DetailSection(title = "TIPO DE ACCESO") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(style.bg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(imageVector = style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(style.description, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(style.actionLabel, color = colors.muted, fontSize = 11.sp)
                    }
                }
            }

            // ── When ─────────────────────────────────────────────────────────
            DetailSection(title = "FECHA Y HORA") {
                DetailRow(label = "Fecha exacta", value = formatFullDateTime(entry.accessedAt))
                DetailRow(label = "Hace", value = relativeTimeLabel(entry.accessedAt))
            }

            // ── Origin (device/IP) — only when present ──────────────────────
            if (entry.ipAddress != null || entry.deviceInfo != null) {
                DetailSection(title = "ORIGEN") {
                    entry.ipAddress?.let {
                        DetailRow(label = "Dirección IP", value = it, icon = Lucide.Globe)
                    }
                    entry.deviceInfo?.let {
                        DetailRow(label = "Dispositivo", value = it, icon = Lucide.Smartphone)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorCard(entry: HistoryAccessLog) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Lucide.User, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                entry.doctorName ?: "Médico no identificado",
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("ID: ${entry.doctorId}", color = colors.muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.elevated)
                .border(1.dp, colors.border, shape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            icon?.let { Icon(it, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp)) }
            Text(label, color = colors.muted, fontSize = 12.sp)
        }
        Text(value, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** "17 jul 2026, 14:32" — full local date/time, since the card only shows a relative label. */
private fun formatFullDateTime(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = SPANISH_MONTHS_SHORT.getOrElse(dt.monthNumber - 1) { "" }
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "${dt.day} $month ${dt.year}, $hour:$minute"
}
