package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.X
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.presentation.component.AdminPatientsComponent
import com.inclinic.app.features.admin.presentation.component.AdminPatientsFilter
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.atoms.SearchBar
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPatientsScreen(component: AdminPatientsComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    var searchVisible by remember { mutableStateOf(false) }

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
                        "Pacientes",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    Icon(
                        if (searchVisible) Lucide.X else Lucide.Search,
                        contentDescription = if (searchVisible) "Cerrar búsqueda" else "Buscar",
                        tint = colors.navy,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(22.dp)
                            .clickable {
                                searchVisible = !searchVisible
                                if (!searchVisible) component.onSearchQueryChange("")
                            },
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
                    AnimatedVisibility(visible = searchVisible) {
                        Column {
                            SearchBar(
                                query = state.searchQuery,
                                onQueryChange = component::onSearchQueryChange,
                                placeholder = "Buscar por nombre o email...",
                            )
                            Spacer(Modifier.height(dimens.spacing12))
                        }
                    }
                }

                item {
                    FilterChipRow(
                        options = AdminPatientsFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminPatientsFilter.entries.first { it.label == label }
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

                if (state.isGapFilter) {
                    item {
                        InfoBanner(
                            title = "Datos no disponibles",
                            description = "El endpoint de pacientes no expone un campo de 'observado'. Esta función requeriría un endpoint dedicado o un campo adicional en el modelo de usuario.",
                            tone = InfoBannerTone.Warning,
                            modifier = Modifier.padding(top = dimens.spacingSm),
                        )
                    }
                } else if (state.visibleItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = dimens.spacingXl), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Lucide.Users,
                                title = "Sin pacientes",
                                subtitle = "No hay pacientes que coincidan con los filtros.",
                            )
                        }
                    }
                }

                items(state.visibleItems, key = { it.id }) { patient ->
                    PatientListCard(
                        patient = patient,
                        onViewProfile = { component.onPatientClicked(patient) },
                    )
                }
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun PatientListCard(
    patient: AdminPatientListItem,
    onViewProfile: () -> Unit,
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
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        // Initials avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.navyTint),
        ) {
            Text(
                text = patient.initials,
                color = colors.navy,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // Name + status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            ) {
                Text(
                    patient.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                    modifier = Modifier.weight(1f, fill = false),
                )
                PatientStatusBadge(patient.isSuspended)
            }
            // Tier line
            Text(
                patient.tierLabel,
                fontSize = 11.sp,
                color = colors.muted,
            )
            // Last login line (if available)
            patient.lastLoginLabel?.let { label ->
                Text(
                    "Último acceso $label",
                    fontSize = 11.sp,
                    color = colors.light,
                )
            }
        }

        AppButton(
            text = "Ver perfil",
            onClick = onViewProfile,
            size = AppButtonSize.Sm,
        )
    }
}

@Composable
private fun PatientStatusBadge(isSuspended: Boolean) {
    if (isSuspended) {
        AppBadge(text = "Suspendido", tone = AppBadgeTone.Error)
    } else {
        AppBadge(text = "Activo", tone = AppBadgeTone.Success)
    }
}
