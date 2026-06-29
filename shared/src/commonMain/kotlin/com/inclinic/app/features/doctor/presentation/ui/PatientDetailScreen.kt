package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.FileLock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.ShieldAlert
import com.inclinic.app.features.doctor.infrastructure.remote.PatientDetail
import com.inclinic.app.features.doctor.presentation.component.PatientDetailComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(component: PatientDetailComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        val data = state.data
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = { Text("Detalle Paciente", style = typography.titleLarge, color = colors.text) },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Atrás", tint = colors.text)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            return@Column
        }
        if (data == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "No se encontró el paciente", style = typography.body, color = colors.muted)
            }
            return@Column
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeroCard(data.patient)

            DetailTabs(onHistorial = component::onViewMedicalRecords)

            val lastVisit = data.recentAppointments.firstOrNull()?.startsAt?.toString() ?: "Sin visitas previas"
            InfoCard(icon = Lucide.CalendarClock, iconTint = colors.navy, label = "Última visita", value = lastVisit)

            val pkg = data.patient.chronicConditions?.takeIf { it.isNotBlank() }
            InfoCard(
                icon = Lucide.Package,
                iconTint = colors.purple,
                label = "Paquete activo",
                value = pkg ?: "Sin paquete contratado",
            )

            data.patient.allergies?.takeIf { it.isNotBlank() }?.let { allergies ->
                AllergiesCard(allergies)
            }

            RequestAccessCard(onSend = component::onViewMedicalRecords)
        }
    }
}

@Composable
private fun HeroCard(patient: PatientDetail) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(14.dp),
    ) {
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.lavLight))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(patient.fullName, style = typography.body.copy(fontWeight = FontWeight.Bold), color = colors.text)
            val meta = listOfNotNull(patient.dateOfBirth, patient.gender).joinToString(" · ")
            if (meta.isNotBlank()) Text(meta, style = typography.subtitle, color = colors.muted)
            Text(patient.email, style = typography.subtitle, color = colors.text)
            patient.phone?.let { Text(it, style = typography.subtitle, color = colors.text) }
        }
    }
}

@Composable
private fun DetailTabs(onHistorial: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Row(
        Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(dimens.radiusPill))
            .background(colors.navyTint)
            .padding(3.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(dimens.radiusPill)).background(colors.elevated),
        ) {
            Text("Resumen", style = typography.subtitle.copy(fontWeight = FontWeight.Bold), color = colors.navy)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f).fillMaxSize().clickable(onClick = onHistorial),
        ) {
            Text("Historial", style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold), color = colors.muted)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f).fillMaxSize(),
        ) {
            Text("Compartir", style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold), color = colors.muted)
        }
    }
}

@Composable
private fun InfoCard(icon: ImageVector, iconTint: Color, label: String, value: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(label, style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold), color = colors.muted)
            Text(value, style = typography.body.copy(fontWeight = FontWeight.SemiBold), color = colors.text)
        }
    }
}

@Composable
private fun AllergiesCard(allergies: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Lucide.ShieldAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(18.dp))
            Text("Alergias", style = typography.link, color = colors.text)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            allergies.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(4).forEach { item ->
                Text(
                    item,
                    style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.red,
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimens.radiusChip))
                        .background(colors.redBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun RequestAccessCard(onSend: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.navyTint)
            .border(1.dp, colors.lavLight, RoundedCornerShape(dimens.radiusLarge))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Lucide.FileLock, contentDescription = null, tint = colors.navy, modifier = Modifier.size(18.dp))
            Text(
                "Solicitar acceso al historial",
                style = typography.titleLarge.copy(fontSize = 14.sp),
                color = colors.navy,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(dimens.radiusMd))
                .background(colors.elevated)
                .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusMd))
                .padding(12.dp),
        ) {
            Text(
                "Explica la razón médica para acceder al historial...",
                style = typography.subtitle,
                color = colors.light,
            )
        }
        AppButton(
            text = "Enviar solicitud",
            onClick = onSend,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
