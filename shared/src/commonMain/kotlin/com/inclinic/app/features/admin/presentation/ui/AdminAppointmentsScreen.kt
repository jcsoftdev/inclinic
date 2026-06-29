package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import com.inclinic.app.features.admin.presentation.component.AdminAppointmentsComponent
import com.inclinic.app.features.admin.presentation.component.AdminAppointmentsFilter
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.SearchBar
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(component: AdminAppointmentsComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = component::onRefresh,
        modifier = modifier.fillMaxSize().background(colors.sand),
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        "Citas",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

            if (state.isLoading && state.allItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                return@PullToRefreshBox
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = dimens.spacingMd,
                    end = dimens.spacingMd,
                    top = dimens.spacing12,
                    bottom = dimens.spacingLg,
                ),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            ) {
                // Search bar
                item {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = component::onSearchQueryChange,
                        placeholder = "Buscar cita, doctor, paciente...",
                    )
                }

                // Filter chips
                item {
                    FilterChipRow(
                        options = AdminAppointmentsFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminAppointmentsFilter.entries.first { it.label == label }
                            component.onFilterChange(filter)
                        },
                    )
                }

                // Error banner
                state.error?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = colors.red,
                            style = AppTheme.typography.body,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimens.radius))
                                .background(colors.redBg)
                                .padding(dimens.spacing12),
                        )
                    }
                }

                // Empty state
                if (state.visibleItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.Calendar,
                                title = "Sin citas",
                                subtitle = "No hay citas que coincidan con los filtros seleccionados.",
                            )
                        }
                    }
                }

                // Appointment cards
                items(state.visibleItems, key = { it.id }) { item ->
                    AppointmentCard(
                        item = item,
                        onClick = { component.onAppointmentClicked(item.id) },
                    )
                }
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun AppointmentCard(
    item: AdminAppointmentListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        // Header row: initials + names + status chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            InitialsAvatar(
                initials = item.patient.initials,
                backgroundColor = colors.lav,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    item.patient.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                Text(
                    "Dr. ${item.doctor.fullName} · ${item.specialty.name}",
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }
            AppointmentStatusChip(status = item.status)
        }

        // Date / price row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        ) {
            Icon(
                Lucide.Calendar,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(13.dp),
            )
            Text(
                formatStartTime(item.startTime),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                "S/ ${item.price.formatDecimal(2)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
            )
        }

        // Dispute badge (if any)
        if (item.hasDispute) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            ) {
                Icon(
                    Lucide.CircleAlert,
                    contentDescription = "Disputa",
                    tint = colors.red,
                    modifier = Modifier.size(13.dp),
                )
                AppBadge(
                    text = "Disputa: ${item.disputeStatus}",
                    tone = AppBadgeTone.Error,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Lucide.ChevronRight,
                    contentDescription = null,
                    tint = colors.light,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Icon(
                    Lucide.ChevronRight,
                    contentDescription = null,
                    tint = colors.light,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun InitialsAvatar(
    initials: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AppointmentStatusChip(status: String) {
    val (label, kind) = appointmentStatusChip(status)
    ChipStatus(label = label, kind = kind)
}

/** Maps appointment status string to a display label and [ChipStatusKind]. */
internal fun appointmentStatusChip(status: String): Pair<String, ChipStatusKind> = when (status.uppercase()) {
    "COMPLETED"  -> "Completada"   to ChipStatusKind.Success
    "CANCELLED"  -> "Cancelada"    to ChipStatusKind.Error
    "CONFIRMED"  -> "Confirmada"   to ChipStatusKind.Info
    "NO_SHOW"    -> "No show"      to ChipStatusKind.Warning
    "PENDING"    -> "Pendiente"    to ChipStatusKind.Neutral
    "ACTIVE",
    "IN_PROGRESS" -> "En vivo"     to ChipStatusKind.Info
    else         -> status          to ChipStatusKind.Neutral
}

/**
 * Formats an ISO 8601 startTime string into a human-readable date+time.
 * e.g. "2026-06-01T10:30:00.000Z" -> "01/06/2026 · 10:30"
 */
internal fun formatStartTime(iso: String): String = try {
    // Safely extract date and time components from ISO string without a date library dependency.
    val date = iso.substring(0, 10)        // "YYYY-MM-DD"
    val time = iso.substring(11, 16)       // "HH:mm"
    val (y, m, d) = date.split("-")
    "$d/$m/$y · $time"
} catch (_: Exception) {
    iso
}
