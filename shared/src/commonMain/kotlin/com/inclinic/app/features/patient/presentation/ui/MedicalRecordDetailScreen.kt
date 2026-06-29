package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pill
import com.inclinic.app.core.model.MedicalRecordDetail
import com.inclinic.app.core.model.RecordPrescription
import com.inclinic.app.core.model.VitalSigns
import com.inclinic.app.core.platform.rememberUrlOpener
import com.inclinic.app.features.patient.presentation.component.MedicalRecordDetailComponent
import com.inclinic.app.ui.atoms.ChipSpecialty
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicalRecordDetailScreen(component: MedicalRecordDetailComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial Médico", color = colors.text, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.sand)
                .padding(padding),
        ) {
            state.error?.let { ErrorBanner(message = it, onDismiss = { }) }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                state.record == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontró el registro", color = colors.muted, fontSize = 14.sp)
                }
                else -> RecordBody(state.record!!, onNavigateToMembership = component::onNavigateToMembership)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordBody(record: MedicalRecordDetail, onNavigateToMembership: () -> Unit) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Estado del plan: PREMIUM (completo) vs FREE (bloqueado)
        if (record.isLocked) LockedBanner() else PremiumBanner()

        // Doctor + especialidad
        DoctorCard(record.doctorName, record.specialtyName)

        if (record.isLocked) {
            // Sólo se muestra el diagnóstico (recortado por el backend) + upsell.
            record.diagnosis?.let { Section(title = "DIAGNÓSTICO") { DiagnosisBody(it) } }
            UpsellCard(onNavigateToMembership = onNavigateToMembership)
            return@Column
        }

        // Motivo de consulta
        record.chiefComplaint?.takeIf { it.isNotBlank() }?.let {
            Section(title = "MOTIVO DE CONSULTA") { BodyText(it) }
        }

        // Síntomas como chips (backend = texto libre → mostrar como chips si hay comas)
        record.symptoms?.takeIf { it.isNotBlank() }?.let { symptoms ->
            val chips = symptoms.split(",").map { it.trim() }.filter { it.isNotBlank() }
            Section(title = "SÍNTOMAS") {
                if (chips.size > 1) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        chips.forEach { ChipSpecialty(label = it) }
                    }
                } else {
                    BodyText(symptoms)
                }
            }
        }

        // Signos vitales — grid 2 columnas
        record.vitalSigns?.let { Section(title = "SIGNOS VITALES") { VitalsGrid(it) } }

        // Diagnóstico con chip ICD-10 si hay código
        record.diagnosis?.let {
            Section(title = "DIAGNÓSTICO") { DiagnosisBody(it) }
        }

        // Recetas
        if (record.prescriptions.isNotEmpty()) {
            Section(title = "RECETAS") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    record.prescriptions.forEach { PrescriptionRow(it) }
                }
            }
        }

        // Plan de tratamiento
        record.treatmentPlan?.takeIf { it.isNotBlank() }?.let {
            Section(title = "PLAN DE TRATAMIENTO") { BodyText(it) }
        }

        // Estudios ordenados (chips)
        if (record.studiesOrdered.isNotEmpty()) {
            Section(title = "ESTUDIOS ORDENADOS") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    record.studiesOrdered.forEach { ChipSpecialty(label = it) }
                }
            }
        }

        // Adjuntos (filas tocables — se abren con UrlOpener)
        if (record.attachments.isNotEmpty()) {
            Section(title = "ADJUNTOS") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    record.attachments.forEach { AttachmentRow(it) }
                }
            }
        }

        // Notas
        record.notes?.takeIf { it.isNotBlank() }?.let {
            Section(title = "NOTAS") { BodyText(it) }
        }

        // CTA — Descargar PDF completo
        // Desactivado: el backend no expone aún un endpoint PDF para registros médicos
        // (solo existe para recetas — ver KtorPrescriptionDataSource.downloadPrescriptionPdf).
        // Cuando el backend lo implemente: agregar onDownloadPdf a MedicalRecordDetailComponent
        // y cablear igual que PrescriptionDetailScreen (rememberPdfOpener + LaunchedEffect).
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.navy.copy(alpha = 0.4f))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Descargar PDF completo", color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "Disponible próximamente",
                color = colors.muted,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun PremiumBanner() {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.purpleBg)
            .border(1.dp, colors.purple, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.Crown, contentDescription = null, tint = colors.purple, modifier = Modifier.size(18.dp))
        Text(
            "Acceso PREMIUM activo · Ficha completa",
            color = colors.purple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LockedBanner() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.elevated)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.Lock, contentDescription = null, tint = colors.muted)
        Text(
            "Plan FREE — Ficha limitada",
            color = colors.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun UpsellCard(onNavigateToMembership: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.elevated)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Lucide.Lock, contentDescription = null, tint = colors.muted)
            Text(
                "Requiere plan PREMIUM para ver síntomas, vitales y recetas.",
                color = colors.text,
                fontSize = 13.sp,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.navy)
                .clickable { onNavigateToMembership() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Ver con PREMIUM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DoctorCard(doctorName: String?, specialtyName: String?) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                doctorName?.take(2)?.uppercase() ?: "DR",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(doctorName ?: "—", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            specialtyName?.let { ChipSpecialty(label = it) }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AppTheme.colors.elevated)
                .border(1.dp, colors.border, shape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun BodyText(value: String) {
    Text(value, color = AppTheme.colors.text, fontSize = 13.sp, lineHeight = 20.sp)
}

@Composable
private fun DiagnosisBody(value: String) {
    Text(value, color = AppTheme.colors.navy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VitalsGrid(v: VitalSigns) {
    val colors = AppTheme.colors
    val items = buildList {
        if (v.bloodPressureSystolic != null && v.bloodPressureDiastolic != null) {
            add("Presión" to "${v.bloodPressureSystolic}/${v.bloodPressureDiastolic}")
        }
        v.heartRate?.let { add("Frec. cardíaca" to "$it bpm") }
        v.temperature?.let { add("Temperatura" to "$it °C") }
        v.oxygenSaturation?.let { add("Sat. O₂" to "$it%") }
        v.weight?.let { add("Peso" to "$it kg") }
        v.height?.let { add("Talla" to "$it cm") }
    }
    // Render as 2-column grid using FlowRow pairs
    val shape = RoundedCornerShape(12.dp)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2,
    ) {
        items.forEach { (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(colors.sand)
                    .border(1.dp, colors.border, shape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(label, color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(value, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PrescriptionRow(p: RecordPrescription) {
    val colors = AppTheme.colors
    val meta = listOfNotNull(p.dosage, p.frequency, p.duration)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.Pill, contentDescription = null, tint = colors.navy)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(p.medication ?: "Medicamento", color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            if (meta.isNotBlank()) Text(meta, color = colors.muted, fontSize = 12.sp)
            p.instructions?.takeIf { it.isNotBlank() }?.let { Text(it, color = colors.muted, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun AttachmentRow(url: String) {
    val colors = AppTheme.colors
    val urlOpener = rememberUrlOpener()
    val fileName = url.substringAfterLast('/').ifBlank { url }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { urlOpener.open(url) },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.FileText, contentDescription = null, tint = colors.red)
        Text(fileName, color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
