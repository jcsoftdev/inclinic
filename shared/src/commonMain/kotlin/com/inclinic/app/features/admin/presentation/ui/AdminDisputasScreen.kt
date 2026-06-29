package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.Users
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminNoShowItem
import com.inclinic.app.features.admin.presentation.component.AdminDisputasComponent
import com.inclinic.app.features.admin.presentation.component.DisputasSegment
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.theme.AppTheme

private val statusFilters = listOf(
    null to "Todas",
    "PENDING" to "Pagos",
    "NO_SHOW" to "No-shows",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDisputasScreen(
    component: AdminDisputasComponent,
    modifier: Modifier = Modifier,
) {
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
                        "Disputas",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                actions = {
                    Icon(
                        Lucide.ShieldAlert,
                        contentDescription = "Risk",
                        tint = colors.red,
                        modifier = Modifier.size(20.dp).padding(end = dimens.spacingMd),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

            // ── Segment tabs (Todas / Pagos / No-shows) ───────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier.fillMaxWidth().background(colors.surface),
            ) {
                // Disputes segment filters
                if (state.segment == DisputasSegment.Disputes) {
                    items(statusFilters) { (value, label) ->
                        val isActive = state.disputeStatusFilter == value
                        DisputasFilterChip(label = label, isActive = isActive) {
                            component.onDisputeStatusFilter(value)
                        }
                    }
                } else {
                    item {
                        DisputasFilterChip(label = "No-shows", isActive = true, onClick = {})
                    }
                }
            }

            // ── Segment switcher (Disputas / No-Shows) ────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.elevated)
                    .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                DisputasSegmentTab(
                    label = "Disputas",
                    isActive = state.segment == DisputasSegment.Disputes,
                    modifier = Modifier.weight(1f),
                ) { component.onSegmentSelected(DisputasSegment.Disputes) }
                DisputasSegmentTab(
                    label = "No-Shows",
                    isActive = state.segment == DisputasSegment.NoShows,
                    modifier = Modifier.weight(1f),
                ) { component.onSegmentSelected(DisputasSegment.NoShows) }
            }

            // ── List ──────────────────────────────────────────────────────────
            when (state.segment) {
                DisputasSegment.Disputes -> DisputesList(
                    items = state.disputes,
                    isLoading = state.disputesLoading,
                    error = state.disputesError,
                    onItemClick = component::onDisputeClicked,
                )
                DisputasSegment.NoShows -> NoShowsList(
                    items = state.noShows,
                    isLoading = state.noShowsLoading,
                    error = state.noShowsError,
                    onItemClick = component::onNoShowClicked,
                )
            }
        }
    }
}

// ── Disputes list ─────────────────────────────────────────────────────────────

@Composable
private fun DisputesList(
    items: List<AdminDisputeItem>,
    isLoading: Boolean,
    error: String?,
    onItemClick: (String) -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    if (isLoading && items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.navy)
        }
        return
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
        error?.let { err ->
            item { ErrorBannerItem(err) }
        }
        if (items.isEmpty() && !isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = dimens.spacingXl), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Lucide.ShieldAlert, title = "Sin disputas", subtitle = "No hay disputas activas.")
                }
            }
        }
        items(items, key = { it.id }) { dispute ->
            DisputeQueueCard(dispute = dispute, onClick = { onItemClick(dispute.id) })
        }
    }
}

// ── No-Shows list ─────────────────────────────────────────────────────────────

@Composable
private fun NoShowsList(
    items: List<AdminNoShowItem>,
    isLoading: Boolean,
    error: String?,
    onItemClick: (String) -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    if (isLoading && items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.navy)
        }
        return
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
        // Info banner
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.amberBg)
                    .border(1.dp, colors.amber, RoundedCornerShape(dimens.radius))
                    .padding(dimens.spacing12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Icon(Lucide.TriangleAlert, contentDescription = null, tint = colors.amber, modifier = Modifier.size(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Casos con retención automática",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                    )
                    Text(
                        "Revisa evidencia antes de liberar fondos o penalizar.",
                        fontSize = 11.sp,
                        color = colors.muted,
                    )
                }
            }
        }
        error?.let { err ->
            item { ErrorBannerItem(err) }
        }
        if (items.isEmpty() && !isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = dimens.spacingXl), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Lucide.Users, title = "Sin no-shows", subtitle = "No hay no-shows pendientes.")
                }
            }
        }
        items(items, key = { it.id }) { noShow ->
            NoShowQueueCard(noShow = noShow, onClick = { onItemClick(noShow.id) })
        }
    }
}

// ── Queue cards ───────────────────────────────────────────────────────────────

@Composable
private fun DisputeQueueCard(
    dispute: AdminDisputeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val accentColor = if (dispute.isUrgent) colors.red else colors.amber

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            // Initials avatar
            DisputasAvatar(initials = dispute.doctor.initials, color = accentColor)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Disputa #${dispute.id.takeLast(4)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                Text(
                    dispute.disputeReason ?: "Sin motivo registrado",
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }
            // Urgency badge
            Box(
                Modifier
                    .clip(RoundedCornerShape(dimens.radiusPill))
                    .background(if (dispute.isUrgent) colors.redBg else colors.amberBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    dispute.urgentLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        ) {
            Icon(Lucide.Hourglass, contentDescription = null, tint = colors.muted, modifier = Modifier.size(12.dp))
            Text(
                "S/ ${dispute.price} · ${dispute.specialty.name} · Resolver",
                fontSize = 11.sp,
                color = colors.muted,
                modifier = Modifier.weight(1f),
            )
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.light, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun NoShowQueueCard(
    noShow: AdminNoShowItem,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            DisputasAvatar(initials = noShow.doctor.initials, color = colors.amber)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    noShow.doctor.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                Text(
                    "Paciente ausente · ${noShow.specialty.name}",
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(dimens.radiusPill))
                    .background(colors.amberBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text("NS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.amber)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        ) {
            Icon(Lucide.Hourglass, contentDescription = null, tint = colors.muted, modifier = Modifier.size(12.dp))
            Text(
                "S/ ${noShow.price} retenido · doctor presente · Resolver",
                fontSize = 11.sp,
                color = colors.muted,
                modifier = Modifier.weight(1f),
            )
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.light, modifier = Modifier.size(14.dp))
        }
    }
}

// ── Shared atoms ──────────────────────────────────────────────────────────────

@Composable
private fun DisputasAvatar(initials: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
    ) {
        Text(initials, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DisputasFilterChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val bg = if (isActive) colors.navy else colors.elevated
    val textColor = if (isActive) androidx.compose.ui.graphics.Color.White else colors.text
    val borderColor = if (isActive) colors.navy else colors.border

    Box(
        Modifier
            .clip(RoundedCornerShape(dimens.radiusPill))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(dimens.radiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun DisputasSegmentTab(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val bg = if (isActive) colors.navyTint else colors.elevated
    val textColor = if (isActive) colors.navy else colors.muted
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun ErrorBannerItem(error: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius))
            .background(colors.redBg)
            .border(1.dp, colors.red, RoundedCornerShape(dimens.radius))
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Icon(Lucide.TriangleAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(16.dp))
        Text(error, color = colors.red, style = AppTheme.typography.body)
    }
}
