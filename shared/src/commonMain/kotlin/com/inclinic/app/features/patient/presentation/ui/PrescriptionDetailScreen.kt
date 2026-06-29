package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.Droplet
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pill
import com.inclinic.app.core.model.Medication
import com.inclinic.app.core.model.PrescriptionStatus
import com.inclinic.app.core.platform.rememberPdfOpener
import com.inclinic.app.features.patient.presentation.component.PrescriptionDetailComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionDetailScreen(component: PrescriptionDetailComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val pdfOpener = rememberPdfOpener()

    LaunchedEffect(state.pdfDownload) {
        state.pdfDownload?.let { pdf ->
            pdfOpener.open(pdf.bytes, pdf.fileName)
            component.onPdfConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle Receta", color = colors.text, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        bottomBar = {
            if (state.prescription != null) {
                PrescriptionActionBar(
                    onDownloadPdf = component::onDownloadPdf,
                    onPrint = component::onDownloadPdf,
                    isDownloading = state.isDownloading,
                )
            }
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
                state.prescription == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontró la receta", color = colors.muted, fontSize = 14.sp)
                }
                else -> {
                    val prescription = state.prescription!!

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Hero card — Receta + doctor
                        item {
                            PrescriptionHeroCard(
                                code = prescription.code,
                                issuedAt = prescription.issuedAt.toString(),
                                status = prescription.status,
                                doctorName = prescription.doctorName,
                                doctorLicense = prescription.doctorLicense,
                                doctorSpecialty = prescription.specialtyName,
                            )
                        }

                        // Medications section label
                        item {
                            Text(
                                text = "MEDICAMENTOS",
                                color = colors.muted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }

                        // Medication items
                        items(prescription.medications) { medication ->
                            MedicationCard(medication)
                        }

                        // General instructions (optional)
                        prescription.generalInstructions?.takeIf { it.isNotBlank() }?.let { instructions ->
                            item {
                                GeneralInstructionsCard(instructions)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrescriptionHeroCard(
    code: String,
    issuedAt: String,
    status: PrescriptionStatus,
    doctorName: String?,
    doctorLicense: String?,
    doctorSpecialty: String?,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top row: code + status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Receta $code",
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Emitida $issuedAt",
                    color = colors.muted,
                    fontSize = 12.sp,
                )
            }
            ChipStatus(label = status.label(), kind = status.chipKind())
        }

        // Divider + Doctor row
        HorizontalDivider(color = colors.border)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.navy),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    doctorName?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull()?.toString() }?.joinToString("") ?: "DR",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(doctorName ?: "—", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                val meta = listOfNotNull(doctorLicense, doctorSpecialty).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(meta, color = colors.muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun PrescriptionActionBar(
    onDownloadPdf: () -> Unit,
    onPrint: () -> Unit,
    isDownloading: Boolean,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
            )
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppButton(
            text = if (isDownloading) "Descargando…" else "Descargar PDF",
            onClick = onDownloadPdf,
            variant = AppButtonVariant.Navy,
            size = AppButtonSize.Lg,
            loading = isDownloading,
            enabled = !isDownloading,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = "Imprimir",
            onClick = onPrint,
            variant = AppButtonVariant.Outline,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GeneralInstructionsCard(instructions: String) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Lucide.ClipboardList, contentDescription = null, tint = colors.navy, modifier = Modifier.size(14.dp))
            Text(
                text = "INDICACIONES GENERALES",
                color = colors.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
        }
        Text(
            text = instructions,
            color = colors.text,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun MedicationCard(medication: Medication) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(12.dp)

    // Determine icon by medication type heuristic
    val isSyrup = medication.name.contains("gotas", ignoreCase = true) ||
        medication.name.contains("jarabe", ignoreCase = true) ||
        medication.name.contains("solución", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (isSyrup) Lucide.Droplet else Lucide.Pill,
                contentDescription = null,
                tint = colors.teal,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = medication.name,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        HorizontalDivider(color = colors.border.copy(alpha = 0.5f))

        MedicationInfoRow(label = "Dosis", value = medication.dosage)
        MedicationInfoRow(label = "Frecuencia", value = medication.frequency)
        MedicationInfoRow(label = "Duración", value = medication.duration)
    }
}

@Composable
private fun MedicationInfoRow(label: String, value: String) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = colors.muted, fontSize = 12.sp)
        Text(value, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun PrescriptionStatus.label(): String = when (this) {
    PrescriptionStatus.ACTIVE -> "VIGENTE"
    PrescriptionStatus.EXPIRED -> "EXPIRADA"
}

private fun PrescriptionStatus.chipKind(): ChipStatusKind = when (this) {
    PrescriptionStatus.ACTIVE -> ChipStatusKind.Success
    PrescriptionStatus.EXPIRED -> ChipStatusKind.Error
}
