package com.inclinic.app.features.doctor.patients.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.UserPlus
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.core.model.PatientStatus
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsFilter
import com.inclinic.app.features.doctor.patients.presentation.component.PatientsListComponent
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.ErrorState
import com.inclinic.app.ui.atoms.SkeletonListRows
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsListScreen(component: PatientsListComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = androidx.compose.foundation.layout.WindowInsets(0),
            title = { Text("Pacientes", style = typography.titleLarge, color = colors.text) },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Atrás", tint = colors.text)
                }
            },
            actions = {
                IconButton(onClick = component::onSearchClicked) {
                    Icon(Lucide.UserPlus, contentDescription = "Buscar paciente", tint = colors.navy)
                }
            },
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
                        modifier   = Modifier.fillMaxWidth(),
                        title      = "No se pudo cargar",
                        subtitle   = "Revisa tu conexion e intentalo de nuevo.",
                        retryLabel = "Reintentar",
                        onRetry    = component::onRefresh,
                    )
                }
            }

            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = dimens.spacingMd, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SearchPill(onClick = component::onSearchClicked)
                }
                item {
                    KpiRow(
                        total = state.stats.total,
                        active = state.stats.active,
                        premium = state.stats.premium,
                    )
                }
                item {
                    FilterTabs(selected = state.filter, onSelect = component::onFilterChange)
                }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Mis pacientes", style = typography.titleLarge.copy(fontSize = 16.sp), color = colors.text)
                        Text("Ordenar", style = typography.subtitle, color = colors.muted)
                    }
                }
                val visible = state.visiblePatients
                if (visible.isEmpty()) {
                    item {
                        EmptyState(
                            title       = "Sin pacientes aun",
                            subtitle    = "Cuando atiendas pacientes apareceran aqui.",
                            actionLabel = "Buscar paciente",
                            onAction    = component::onSearchClicked,
                        )
                    }
                } else {
                    items(visible, key = { it.id }) { patient ->
                        PatientCard(patient = patient, onClick = { component.onPatientClicked(patient.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPill(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(dimens.radiusPill))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        Icon(Lucide.Search, contentDescription = null, tint = colors.lav, modifier = Modifier.size(18.dp))
        Text("Buscar paciente", style = typography.body, color = colors.muted)
    }
}

@Composable
private fun KpiRow(total: Int, active: Int, premium: Int) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(value = total.toString(), label = "Total", valueColor = colors.navy, modifier = Modifier.weight(1f))
        KpiCard(value = active.toString(), label = "Activos", valueColor = colors.green, modifier = Modifier.weight(1f))
        KpiCard(value = premium.toString(), label = "Premium", valueColor = colors.lav, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun KpiCard(value: String, label: String, valueColor: Color, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            value,
            color = valueColor,
            style = AppTheme.typography.titleLarge.copy(fontSize = 22.sp),
        )
        Text(label, color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilterTabs(selected: PatientsFilter, onSelect: (PatientsFilter) -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Row(
        Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(dimens.radiusPill))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusPill))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        val tabs = listOf(
            PatientsFilter.ALL to "Todos",
            PatientsFilter.ACTIVE to "Activos",
            PatientsFilter.PREMIUM to "Premium",
        )
        tabs.forEach { (filter, label) ->
            val isSelected = filter == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(dimens.radiusPill))
                    .background(if (isSelected) colors.navy else Color.Transparent)
                    .clickable { onSelect(filter) },
            ) {
                Text(
                    label,
                    style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) Color.White else colors.muted,
                )
            }
        }
    }
}

@Composable
private fun PatientCard(patient: PatientListItem, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val initials = patient.name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    val (chipLabel, chipColor) = when (patient.status) {
        PatientStatus.PREMIUM -> "PREMIUM" to colors.lav
        PatientStatus.ACTIVE -> "ACTIVO" to colors.green
        PatientStatus.INACTIVE -> "INACTIVO" to colors.amber
        PatientStatus.UNKNOWN -> null to colors.muted
    }
    val subtitleColor = when (patient.status) {
        PatientStatus.INACTIVE -> colors.amber
        else -> colors.muted
    }
    val subtitle = patient.lastVisitDate?.let { "Última visita: $it" }
        ?: if (patient.status == PatientStatus.INACTIVE) "Sin visitas" else "Sin visitas previas"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.navy),
        ) {
            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(patient.name, style = typography.body.copy(fontWeight = FontWeight.Bold), color = colors.text)
            Text(subtitle, style = typography.subtitle, color = subtitleColor)
        }
        if (chipLabel != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimens.radiusChip))
                    .background(chipColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(chipLabel, color = chipColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
    }
}
