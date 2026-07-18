package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.List
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldX
import com.inclinic.app.core.model.AccessType
import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.features.patient.presentation.component.HistoryAccessLogsComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAccessLogsScreen(component: HistoryAccessLogsComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Accesos a Historial",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
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
                .padding(padding),
        ) {
            state.error?.let { ErrorBanner(message = it, onDismiss = { }) }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                state.logs.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AccessInfoBanner()
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay registros de acceso", color = colors.muted, fontSize = 14.sp)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 14.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "info-banner") { AccessInfoBanner() }
                    items(state.logs, key = { it.id }) { log ->
                        AccessLogCard(log, onClick = { component.onLogClick(log) })
                    }
                    item(key = "revoke-cta") {
                        RevokeAccessBanner(onManageAccess = component::onManageAccess)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessInfoBanner() {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.blueBg)
            .border(1.dp, colors.blue, shape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Lucide.Shield,
            contentDescription = null,
            tint = colors.blue,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Últimos 100 accesos a tu historial médico",
            color = colors.blue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun RevokeAccessBanner(onManageAccess: () -> Unit) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.navy)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onManageAccess,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Lucide.ShieldX,
            contentDescription = null,
            tint = colors.amber,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "¿Quieres revocar acceso?",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Gestiona quién puede leer tu historial",
                color = colors.muted,
                fontSize = 11.sp,
            )
        }
        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
    }
}

// internal (not private) — reused by HistoryAccessLogDetailScreen so the drill-down
// screen matches the exact icon/label/color mapping shown in the list.
internal data class AccessTypeStyle(
    val icon: ImageVector,
    val actionLabel: String,
    val description: String,
    val tint: Color,
    val bg: Color,
)

@Composable
internal fun accessTypeStyle(type: AccessType): AccessTypeStyle {
    val colors = AppTheme.colors
    return when (type) {
        AccessType.READ -> AccessTypeStyle(
            icon = Lucide.Eye,
            actionLabel = "READ",
            description = "Historia completa",
            tint = colors.blue,
            bg = colors.blueBg,
        )
        AccessType.FULL_HISTORY -> AccessTypeStyle(
            icon = Lucide.FileText,
            actionLabel = "LIST",
            description = "Historia completa",
            tint = colors.navy,
            bg = colors.navyTint,
        )
        AccessType.RECORDS_ONLY -> AccessTypeStyle(
            icon = Lucide.List,
            actionLabel = "LIST",
            description = "Listado de registros",
            tint = colors.green,
            bg = colors.greenBg,
        )
        AccessType.EXPORT_PDF -> AccessTypeStyle(
            icon = Lucide.Download,
            actionLabel = "DOWNLOAD",
            description = "Exportar PDF",
            tint = colors.amber,
            bg = colors.amberBg,
        )
    }
}

@Composable
private fun AccessLogCard(log: HistoryAccessLog, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val style = accessTypeStyle(log.accessType)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.tint,
                modifier = Modifier.size(16.dp),
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = log.doctorName ?: "Acceso registrado",
                color = colors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${style.actionLabel} · ${style.description}",
                color = colors.text,
                fontSize = 11.sp,
            )
            val ip = log.ipAddress
            val time = relativeTimeLabel(log.accessedAt)
            val ipLine = if (ip != null) "$ip · $time" else time
            Text(
                text = ipLine,
                color = colors.muted,
                fontSize = 10.sp,
            )
        }

        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(16.dp),
        )
    }
}

