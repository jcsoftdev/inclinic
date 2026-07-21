package com.inclinic.app.features.doctor.presentation.ui

import kotlinx.datetime.number

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.FileCheck
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.UserX
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.core.platform.rememberFilePicker
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.doctor.presentation.component.DoctorAppointmentDetailComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppTheme
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentDetailScreen(
    component: DoctorAppointmentDetailComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    var showEvidenceSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val colors = AppTheme.colors

    Box(modifier.fillMaxSize().background(colors.sand)) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Box
        }
        val appt = state.appointment
        if (appt == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "No se encontró la cita")
            }
            return@Box
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppBackButton(onClick = component::onBack, contentDescription = "Atrás")
                Text("Detalle de cita", style = AppTheme.typography.displayNano, color = colors.text)
                Spacer(Modifier.weight(1f))
                Icon(Lucide.EllipsisVertical, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
            }

            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PatientHeaderCard(patientName = appt.patientId)
            }

            Spacer(Modifier.height(14.dp))

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val time = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
                val end = appt.endsAt.toLocalDateTime(TimeZone.currentSystemDefault())
                val durationMin = (appt.endsAt - appt.startsAt).inWholeMinutes
                AppointmentInfoCard(
                    visitType = appt.visitType,
                    monthLabel = monthAbbrev(time.month.number),
                    dayLabel = time.day.toString(),
                    dateLine = "${time.date}",
                    timeLine = "${hhmm(time.hour, time.minute)} — ${hhmm(end.hour, end.minute)} ($durationMin min)",
                    status = appt.status,
                    notes = appt.notes,
                    onJoinCall = component::onNavigateToChat,
                )

                if (appt.isPackageSession) {
                    PackageBanner(feeLabel = "S/${appt.consultationFee.formatDecimal(2)} — incluido en paquete")
                }

                state.error?.let { Text(it, style = AppTheme.typography.subtitle, color = colors.red) }
            }

            Spacer(Modifier.height(14.dp))

            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(dimensSpacingSm()),
            ) {
                if (appt.status == AppointmentStatus.CONFIRMED || appt.status == AppointmentStatus.IN_PROGRESS) {
                    NavyCta("Abrir chat", onClick = component::onNavigateToChat)
                }

                // Status-based action buttons
                val now = Clock.System.now()
                val withinWindow = appt.startsAt.minus(30.minutes) <= now && now <= appt.startsAt.plus(30.minutes)

                when (appt.status) {
                    AppointmentStatus.SCHEDULED, AppointmentStatus.PENDING_PAYMENT -> {
                        NavyCta(
                            text = "Confirmar cita",
                            onClick = component::onConfirm,
                            enabled = !state.actionInProgress,
                            loading = state.actionInProgress,
                        )
                    }
                    AppointmentStatus.CONFIRMED -> {
                        NavyCta(
                            text = "Completar consulta",
                            onClick = { showEvidenceSheet = true },
                            enabled = !state.actionInProgress,
                        )
                        if (withinWindow) {
                            NoShowCta(onClick = component::onNoShow, enabled = !state.actionInProgress)
                        }
                    }
                    else -> { /* No actions */ }
                }

                if (appt.status == AppointmentStatus.SCHEDULED || appt.status == AppointmentStatus.CONFIRMED) {
                    OutlineCta(text = "Solicitar reagenda", icon = Lucide.CalendarClock, onClick = component::onRequestReschedule)
                }

                // Crear ficha clínica enlazada a ESTA cita (appointmentId real). Se ofrece
                // cuando la consulta ya fue atendida (COMPLETED), que es el momento de
                // registrar la ficha del paciente.
                if (appt.status == AppointmentStatus.COMPLETED) {
                    OutlineCta(text = "Crear ficha", icon = Lucide.Stethoscope, onClick = component::onCreateMedicalRecord)
                }

                // Backend doesn't populate prescriptionId on GET /api/appointments/:id yet
                // (see Appointment.prescriptionId doc comment), so this always renders as
                // "Emitir receta" today; the "Ver receta" branch is ready for when it does.
                if (appt.status == AppointmentStatus.CONFIRMED ||
                    appt.status == AppointmentStatus.IN_PROGRESS ||
                    appt.status == AppointmentStatus.COMPLETED
                ) {
                    if (appt.prescriptionId == null) {
                        NavyCta(text = "Emitir receta", onClick = component::onNavigateToCreatePrescription)
                    } else {
                        OutlineCta(text = "Ver receta", icon = Lucide.FileCheck, onClick = component::onNavigateToEditPrescription)
                    }
                }

                OutlineCta(text = "Ver historial paciente", icon = Lucide.FileText, onClick = component::onNavigateToPatient)
            }
        }

        if (state.showNoShowDialog) {
            NoShowConfirmationDialog(
                patientId = appt.patientId,
                onConfirm = component::onNoShowConfirmed,
                onDismiss = component::onNoShowDismissed,
            )
        }

        if (showEvidenceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEvidenceSheet = false },
                sheetState = sheetState,
            ) {
                EvidenceUploadSheet(
                    photoUrls = state.evidencePhotoUrls,
                    isUploading = state.isUploadingPhoto,
                    onPickPhoto = component::onEvidencePhotoPicked,
                    onRemovePhoto = component::onRemoveEvidencePhoto,
                    onComplete = {
                        showEvidenceSheet = false
                        component.onComplete()
                    },
                    onDismiss = { showEvidenceSheet = false },
                )
            }
        }
    }
}

@Composable
fun NoShowConfirmationDialog(
    patientId: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Marcar como no se presentó?") },
        text = {
            Column {
                Text("Paciente: $patientId")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Esta acción registrará que el paciente no asistió. Tendrá impacto en el proceso de pago.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
fun EvidenceUploadSheet(
    photoUrls: List<String>,
    isUploading: Boolean,
    onPickPhoto: (PickedFile) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val picker = rememberFilePicker { file -> if (file != null) onPickPhoto(file) }

    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Subir evidencia de consulta", style = MaterialTheme.typography.titleMedium)
        Text("${photoUrls.size}/3 fotos subidas", style = MaterialTheme.typography.bodySmall)

        if (photoUrls.size < 3) {
            OutlinedButton(
                onClick = { picker.launch() },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isUploading) "Subiendo…" else "Agregar foto")
            }
        }

        photoUrls.forEachIndexed { i, _ ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Foto ${i + 1}", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { onRemovePhoto(i) }) {
                    Text("Eliminar")
                }
            }
        }

        Button(
            onClick = onComplete,
            enabled = photoUrls.isNotEmpty() && !isUploading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (photoUrls.isEmpty()) "Agrega al menos 1 foto de evidencia" else "Completar consulta")
        }
        TextButton(onClick = onDismiss, Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}

@Composable
private fun dimensSpacingSm() = AppTheme.dimens.spacingSm

private fun hhmm(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

private fun monthAbbrev(month: Int): String = listOf(
    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC",
).getOrElse(month - 1) { "" }

@Composable
private fun PatientHeaderCard(patientName: String) {
    val colors = AppTheme.colors
    val initials = patientName
        .split(" ", "-", "_")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radiusLarge))
            .background(colors.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.navyTint),
        ) {
            Text(initials, style = AppTheme.typography.displayNano, fontSize = 18.sp, color = colors.navy)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(patientName, style = AppTheme.typography.displayNano, fontSize = 18.sp, color = colors.text)
        }
    }
}

@Composable
private fun AppointmentInfoCard(
    visitType: VisitType,
    monthLabel: String,
    dayLabel: String,
    dateLine: String,
    timeLine: String,
    status: AppointmentStatus,
    notes: String?,
    onJoinCall: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radiusLarge))
            .background(colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(colors.navy),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(dayLabel, style = AppTheme.typography.titleLarge, fontSize = 18.sp, color = Color.White)
                Text(monthLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = colors.lavLight)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(dateLine, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Text(timeLine, fontSize = 12.sp, color = colors.muted)
            }
            DetailStatusChip(status)
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

        val (icon, tint, bg, label) = when (visitType) {
            VisitType.VIRTUAL -> InfoVisit(Lucide.Video, colors.blue, colors.blueBg, "Telemedicina")
            VisitType.HOME -> InfoVisit(Lucide.House, colors.teal, colors.tealBg, "Domicilio")
            VisitType.CLINIC -> InfoVisit(Lucide.Building2, colors.navy, colors.navyTint, "Consultorio")
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(bg),
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
                notes?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 11.sp, color = colors.muted)
                }
            }
        }

        if (visitType == VisitType.VIRTUAL) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(AppTheme.dimens.radius))
                    .background(colors.blue)
                    .clickable(onClick = onJoinCall),
            ) {
                Text("Iniciar videollamada", color = Color.White, style = AppTheme.typography.buttonLg, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun DetailStatusChip(status: AppointmentStatus) {
    val colors = AppTheme.colors
    val (label, textColor, bg) = when (status) {
        AppointmentStatus.CONFIRMED -> Triple("CONFIRMADA", colors.green, colors.greenBg)
        AppointmentStatus.IN_PROGRESS -> Triple("EN CURSO", colors.amber, colors.amberBg)
        AppointmentStatus.SCHEDULED -> Triple("AGENDADA", colors.navy, colors.navyTint)
        AppointmentStatus.COMPLETED -> Triple("COMPLETADA", colors.green, colors.greenBg)
        AppointmentStatus.NO_SHOW -> Triple("NO SHOW", colors.red, colors.redBg)
        AppointmentStatus.CANCELLED_BY_PATIENT, AppointmentStatus.CANCELLED_BY_DOCTOR -> Triple("CANCELADA", colors.red, colors.redBg)
        else -> Triple(status.name, colors.muted, colors.lav50)
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PackageBanner(feeLabel: String) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radiusMd))
            .background(colors.lav50)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.lav),
        ) {
            Icon(Lucide.Package, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Sesión de paquete", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Text(feeLabel, fontSize = 11.sp, color = colors.lav)
        }
    }
}

@Composable
private fun NavyCta(text: String, onClick: () -> Unit, enabled: Boolean = true, loading: Boolean = false) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.navy)
            .clickable(enabled = enabled && !loading, onClick = onClick),
    ) {
        if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
        else Text(text, color = Color.White, style = AppTheme.typography.buttonLg, fontSize = 16.sp)
    }
}

@Composable
private fun OutlineCta(text: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radius))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Icon(icon, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
        Text(text, color = colors.navy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun NoShowCta(onClick: () -> Unit, enabled: Boolean) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Icon(Lucide.UserX, contentDescription = null, tint = colors.red, modifier = Modifier.size(14.dp))
        Text("Marcar no-show", color = colors.red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
    }
}

private data class InfoVisit(val icon: ImageVector, val tint: Color, val bg: Color, val label: String)

