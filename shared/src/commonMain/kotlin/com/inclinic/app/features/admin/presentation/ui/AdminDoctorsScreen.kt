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
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.UserPlus
import com.composables.icons.lucide.Users
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorListItem
import com.inclinic.app.features.admin.presentation.component.AdminDoctorsComponent
import com.inclinic.app.features.admin.presentation.component.AdminDoctorsFilter
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.SearchBar
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorsScreen(component: AdminDoctorsComponent, modifier: Modifier = Modifier) {
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
                        "Doctores",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                actions = {
                    Icon(
                        Lucide.UserPlus,
                        contentDescription = "Por aprobar",
                        tint = colors.navy,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(22.dp)
                            .clickable { component.onNavigateToPendingApprovals() },
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
                item {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = component::onSearchQueryChange,
                        placeholder = "Buscar doctor, especialidad...",
                    )
                }

                item {
                    FilterChipRow(
                        options = AdminDoctorsFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminDoctorsFilter.entries.first { it.label == label }
                            component.onFilterChange(filter)
                        },
                    )
                }

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

                // "En revisión" filter navigates away — show informational message instead of a list
                if (state.activeFilter == AdminDoctorsFilter.Pending) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = dimens.spacingXl), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Lucide.Users,
                                title = "Ver solicitudes pendientes",
                                subtitle = "Navega a la pantalla de aprobación de doctores.",
                            )
                        }
                    }
                } else if (state.visibleItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = dimens.spacingXl), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Lucide.Stethoscope,
                                title = "Sin doctores",
                                subtitle = "No hay doctores que coincidan con los filtros.",
                            )
                        }
                    }
                }

                items(state.visibleItems, key = { it.id }) { item ->
                    DoctorListCard(
                        item = item,
                        onClick = { component.onDoctorClicked(item.id) },
                    )
                }
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun DoctorListCard(
    item: AdminDoctorListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        DoctorInitialsAvatar(initials = item.initials)

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                Text(
                    item.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                    modifier = Modifier.weight(1f, fill = false),
                )
                DoctorStatusChip(statusLabel = item.statusLabel)
            }
            Text(
                item.primarySpecialty,
                fontSize = 11.sp,
                color = colors.muted,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Lucide.Calendar, contentDescription = null, tint = colors.muted, modifier = Modifier.size(11.dp))
                Text(
                    "${item.appointmentCount} citas",
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }
        }

        Icon(
            Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.light,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun DoctorInitialsAvatar(initials: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.navyTint),
    ) {
        Text(
            text = initials,
            color = colors.navy,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DoctorStatusChip(statusLabel: String) {
    val (label, kind) = doctorStatusChip(statusLabel)
    ChipStatus(label = label, kind = kind)
}

internal fun doctorStatusChip(status: String): Pair<String, ChipStatusKind> = when (status.uppercase()) {
    "ACTIVO"     -> "Activo"     to ChipStatusKind.Success
    "SUSPENDIDO" -> "Suspendido" to ChipStatusKind.Error
    "INACTIVO"   -> "Inactivo"   to ChipStatusKind.Neutral
    else          -> status       to ChipStatusKind.Neutral
}
