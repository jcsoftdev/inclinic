package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import com.inclinic.app.features.admin.presentation.component.AdminPatientAppointmentsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.ErrorState
import com.inclinic.app.ui.atoms.SkeletonListRows
import com.inclinic.app.ui.theme.AppTheme

/**
 * Admin screen — appointments list for a specific patient.
 *
 * Data source: AdminDataSource.getAppointments(AdminAppointmentFilters(patientId = id)).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPatientAppointmentsScreen(
    component: AdminPatientAppointmentsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    "Historial de citas",
                    style = AppTheme.typography.displayXSmall,
                    fontSize = 20.sp,
                    color = colors.text,
                )
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        when {
            state.isLoading -> {
                SkeletonListRows(
                    count = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacingMd, vertical = 14.dp),
                )
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        modifier = Modifier.fillMaxWidth(),
                        title = "No se pudo cargar",
                        subtitle = "Revisa tu conexión e inténtalo de nuevo.",
                        retryLabel = "Reintentar",
                        onRetry = component::onRefresh,
                    )
                }
            }
            state.appointments.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "Sin citas",
                        subtitle = "Este paciente no tiene citas registradas.",
                        icon = Lucide.Calendar,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(state.appointments, key = { it.id }) { item ->
                        PatientAppointmentRow(
                            item = item,
                            onClick = { component.onAppointmentClicked(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientAppointmentRow(
    item: AdminAppointmentListItem,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(colors.elevated)
            .border(width = 0.5.dp, color = colors.border)
            .padding(horizontal = dimens.spacingMd, vertical = 12.dp),
    ) {
        // Doctor initials avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.navyTint),
        ) {
            Text(
                text = item.doctor.initials,
                color = colors.navy,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Dr. ${item.doctor.fullName}",
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.specialty.name,
                color = colors.muted,
                fontSize = 12.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Truncate ISO timestamp to date portion
                Text(
                    text = item.startTime.take(10),
                    color = colors.light,
                    fontSize = 11.sp,
                )
                Spacer(Modifier.width(4.dp))
                val statusColor = when (item.status) {
                    "CONFIRMED", "SCHEDULED" -> colors.green
                    "CANCELLED" -> colors.error
                    "COMPLETED" -> colors.navy
                    else -> colors.amber
                }
                Text(
                    text = item.status,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Text(
            text = "S/.${item.price.toInt()}",
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )

        Icon(
            Lucide.ChevronRight,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(16.dp),
        )
    }
}
