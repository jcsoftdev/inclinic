package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Stethoscope
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.features.doctor.presentation.component.MedicalRecordsListComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsListScreen(component: MedicalRecordsListComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = { Text("Historia Clínica", style = typography.titleLarge, color = colors.text) },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ChevronLeft, contentDescription = "Atrás", tint = colors.navy)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }

                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = dimens.spacingMd,
                        vertical = 14.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { HistoryTabs() }
                    item { AccessBanner() }
                    if (state.records.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                                Text("Sin registros médicos", style = typography.body, color = colors.muted)
                            }
                        }
                    } else {
                        items(state.records, key = { it.id }) { record ->
                            MedicalRecordCard(
                                record = record,
                                isExpanded = state.expandedRecordId == record.id,
                                onClick = { component.onRecordTap(record.id) },
                            )
                        }
                    }
                }
            }
        }

        Box(Modifier.background(colors.sand).padding(start = dimens.spacingMd, end = dimens.spacingMd, top = 10.dp, bottom = 14.dp)) {
            AppButton(
                text = "Crear nueva ficha",
                onClick = component::onCreateRecord,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HistoryTabs() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(dimens.radiusMd))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusMd))
            .padding(4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(dimens.radiusChip)).background(colors.navy),
        ) {
            Text("Historia", style = typography.subtitle.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxSize()) {
            Text("Perfil clínico", style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold), color = colors.muted)
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxSize()) {
            Text("Recetas", style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold), color = colors.muted)
        }
    }
}

@Composable
private fun AccessBanner() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.greenBg)
            .border(1.dp, colors.green, RoundedCornerShape(dimens.radiusLarge))
            .padding(14.dp),
    ) {
        Icon(Lucide.ShieldCheck, contentDescription = null, tint = colors.green, modifier = Modifier.size(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Acceso autorizado", style = typography.link, color = colors.green)
            Text("Acceso vigente · cita activa", style = typography.subtitle, color = colors.green)
        }
    }
}

@Composable
private fun MedicalRecordCard(record: MedicalRecord, isExpanded: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val date = record.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusMd))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusMd))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(record.diagnosis, style = typography.body.copy(fontWeight = FontWeight.Bold), color = colors.text)
            Text(date.toString(), style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold), color = colors.muted)
        }
        record.doctorName?.let { name ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Lucide.Stethoscope, contentDescription = null, tint = colors.muted, modifier = Modifier.size(13.dp))
                Text(name, style = typography.subtitle, color = colors.muted)
            }
        }
        record.specialtyName?.let { specialty ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimens.radiusChip))
                    .background(colors.navyTint)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(specialty, color = colors.lav, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        AnimatedVisibility(isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (record.symptoms.isNotBlank()) Text("Síntomas: ${record.symptoms}", style = typography.subtitle, color = colors.muted)
                if (record.treatment.isNotBlank()) Text("Tratamiento: ${record.treatment}", style = typography.subtitle, color = colors.muted)
                record.prescription?.let { Text("Prescripción: $it", style = typography.subtitle, color = colors.muted) }
                record.notes?.let { Text("Notas: $it", style = typography.subtitle, color = colors.muted) }
            }
        }
    }
}
