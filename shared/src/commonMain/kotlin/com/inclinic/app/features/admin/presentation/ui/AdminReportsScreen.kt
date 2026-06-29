package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleHelp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.inclinic.app.features.admin.infrastructure.remote.AdminReportItem
import com.inclinic.app.features.admin.presentation.component.AdminReportsComponent
import com.inclinic.app.features.admin.presentation.component.AdminReportsFilter
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(component: AdminReportsComponent, modifier: Modifier = Modifier) {
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
                        "Reportes",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    Icon(
                        Lucide.CircleHelp,
                        contentDescription = "Información",
                        tint = colors.navy,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(22.dp),
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
                    FilterChipRow(
                        options = AdminReportsFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminReportsFilter.entries.first { it.label == label }
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

                if (state.visibleItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.ShieldAlert,
                                title = "Sin reportes",
                                subtitle = "No hay reportes que coincidan con el filtro.",
                            )
                        }
                    }
                }

                items(state.visibleItems, key = { it.id }) { report ->
                    ReportListCard(
                        report = report,
                        onRevisar = { component.onReportClicked(report) },
                    )
                }
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun ReportListCard(
    report: AdminReportItem,
    onRevisar: () -> Unit,
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
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Row(
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
                    text = report.reportedUser.initials,
                    color = colors.navy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Title row: "Reporte a {name}" + age badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
                ) {
                    Text(
                        "Reporte a ${report.reportedUser.fullName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        report.ageLabel,
                        fontSize = 10.sp,
                        color = colors.muted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.sand)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                // Category · role
                Text(
                    "Categoría: ${report.categoryLabel} · ${report.reportedUser.roleLabel}",
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }

            ReportStatusBadge(status = report.status)
        }

        // Reason excerpt
        Text(
            text = "Motivo: ${report.reasonExcerpt}",
            fontSize = 12.sp,
            color = colors.muted,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius))
                .background(colors.sand)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )

        Spacer(Modifier.height(2.dp))

        AppButton(
            text = "Revisar →",
            onClick = onRevisar,
            variant = AppButtonVariant.Navy,
            size = AppButtonSize.Sm,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReportStatusBadge(status: String) {
    val tone = when (status) {
        "PENDING" -> AppBadgeTone.Warning
        "ACTION_TAKEN" -> AppBadgeTone.Error
        "DISMISSED" -> AppBadgeTone.Neutral
        else -> AppBadgeTone.Success  // REVIEWED
    }
    val label = when (status) {
        "PENDING" -> "Pendiente"
        "REVIEWED" -> "Revisado"
        "ACTION_TAKEN" -> "Acción"
        "DISMISSED" -> "Descartado"
        else -> status
    }
    AppBadge(text = label, tone = tone)
}
