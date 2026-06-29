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
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.UserSearch
import com.inclinic.app.features.admin.infrastructure.remote.AdminPendingDoctor
import com.inclinic.app.features.admin.presentation.component.AdminPendingDoctorsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPendingDoctorsScreen(
    component: AdminPendingDoctorsComponent,
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
                        "Por aprobar",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = {
                    AppBackButton(onClick = component::onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

            if (state.isLoading && state.items.isEmpty()) {
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
                // SLA info banner
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
                        Icon(Lucide.Clock, contentDescription = null, tint = colors.amber, modifier = Modifier.size(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "SLA de revisión: 24 horas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.text,
                            )
                            Text(
                                "Ordenado por antigüedad y riesgo documental.",
                                fontSize = 11.sp,
                                color = colors.muted,
                            )
                        }
                    }
                }

                state.error?.let { err ->
                    item {
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
                            Text(err, color = colors.red, style = AppTheme.typography.body)
                        }
                    }
                }

                if (state.items.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.UserSearch,
                                title = "Sin solicitudes",
                                subtitle = "No hay doctores pendientes de aprobación.",
                            )
                        }
                    }
                }

                items(state.items, key = { it.id }) { doctor ->
                    PendingDoctorCard(
                        doctor = doctor,
                        onClick = { component.onDoctorClicked(doctor.id) },
                    )
                }
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun PendingDoctorCard(
    doctor: AdminPendingDoctor,
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
        // Header: avatar + name + wait badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            PendingAvatar(initials = doctor.initials)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    doctor.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                Text(
                    doctor.primarySpecialty,
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }
            // Wait time badge
            Box(
                Modifier
                    .clip(RoundedCornerShape(dimens.radiusPill))
                    .background(colors.amberBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    doctor.waitLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.amber,
                )
            }
        }

        // Meta row: price + docs
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        ) {
            Icon(Lucide.FileText, contentDescription = null, tint = colors.muted, modifier = Modifier.size(12.dp))
            Text(
                "${doctor.documentCount} documentos · CMP: ${doctor.cmpNumber ?: "—"}",
                fontSize = 11.sp,
                color = colors.muted,
                modifier = Modifier.weight(1f),
            )
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.light, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun PendingAvatar(initials: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.lav.copy(alpha = 0.25f)),
    ) {
        Text(
            text = initials,
            color = colors.lav,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
