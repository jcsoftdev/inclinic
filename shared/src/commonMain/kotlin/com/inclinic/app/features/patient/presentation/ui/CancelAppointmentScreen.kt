package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.patient.presentation.component.CancelAppointmentComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun CancelAppointmentScreen(component: CancelAppointmentComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppBackButton(onClick = component::onBack)
            Text("Cancelar cita", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (state.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else {
            val appt = state.appointment

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Appointment card
                if (appt != null) {
                    val dt = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateStr = "${dt.day} ${monthLabel(dt.month.number)}, ${formatTime(dt.hour, dt.minute)}"
                    val visitLabel = when (appt.visitType) {
                        VisitType.VIRTUAL -> "Telemedicina"
                        VisitType.HOME -> "Visita a domicilio"
                        VisitType.CLINIC -> "Consulta presencial"
                    }
                    val initials = appt.doctorName?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.toString() }
                        ?.take(2)?.joinToString("")?.uppercase() ?: "?"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.navy),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(initials, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "Dr. ${appt.doctorName ?: ""}",
                                        color = colors.text,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    appt.specialtyName?.let {
                                        Text(it, color = colors.muted, fontSize = 11.sp)
                                    }
                                }
                            }
                            val statusLabel = when (appt.status) {
                                AppointmentStatus.CONFIRMED, AppointmentStatus.SCHEDULED -> "CONFIRMADA"
                                else -> "PENDIENTE"
                            }
                            val (badgeBg, badgeFg) = if (appt.status == AppointmentStatus.CONFIRMED || appt.status == AppointmentStatus.SCHEDULED)
                                colors.greenBg to colors.green
                            else colors.amberBg to colors.amber
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(badgeBg).padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(statusLabel, color = badgeFg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.Calendar, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                                Text(dateStr, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.Video, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                                Text(visitLabel, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Warning banner
                InfoBanner(
                    title = "Esta acción no se puede deshacer",
                    description = "Una vez cancelada, deberás agendar una nueva cita si la necesitas.",
                    tone = InfoBannerTone.Warning,
                )

                // Cancellation policy
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.elevated)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("POLÍTICA DE CANCELACIÓN", color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    val policyText = if (appt?.status == AppointmentStatus.PENDING_PAYMENT) {
                        "Citas pendientes de pago se pueden cancelar sin restricción."
                    } else if (state.daysUntil >= 3) {
                        "Cancelaciones gratuitas hasta 3 días antes de la cita. Faltan ${state.daysUntil} días, sin penalidad."
                    } else {
                        "No puedes cancelar con menos de 3 días de anticipación. Faltan solo ${state.daysUntil} días."
                    }
                    Text(policyText, color = colors.text, fontSize = 12.sp, lineHeight = 17.sp)
                }

                // Package session return
                if (appt?.isPackageSession == true) {
                    InfoBanner(
                        title = "Se devolverá 1 sesión a tu paquete",
                        description = "Esta cita pertenece a tu paquete de terapia. La sesión vuelve a tu saldo.",
                        tone = InfoBannerTone.Info,
                        icon = Lucide.Package,
                    )
                }

                // Reason textarea
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Motivo (opcional)", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    BasicTextField(
                        value = state.reason,
                        onValueChange = component::onReasonChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.reason.isEmpty()) {
                                    Text("Cuéntanos por qué cancelas...", color = colors.light, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        },
                    )
                    Text("Ayuda al doctor a mejorar su servicio.", color = colors.muted, fontSize = 11.sp)
                }

                // Error
                state.error?.let { err ->
                    Text(err, color = colors.red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Actions — top divider matches design strokeWidth:{top:1}
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.border),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.canCancel) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!state.isCancelling) colors.red else colors.red.copy(alpha = 0.4f))
                            .then(if (!state.isCancelling) Modifier.clickable(onClick = component::onConfirmCancel) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isCancelling) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Cancelar cita", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.5.dp, colors.navy, RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .clickable(onClick = component::onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Volver", color = colors.navy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun monthLabel(m: Int) = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")[m - 1]

private fun formatTime(hour: Int, minute: Int): String {
    val period = if (hour >= 12) "p.m." else "a.m."
    val h = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
    return "${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $period"
}
