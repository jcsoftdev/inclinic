package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Star
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.patient.presentation.component.PatientHomeComponent
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.PatientTab
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme
import com.inclinic.app.ui.theme.AppTypography

@Composable
fun PatientHomeScreen(
    component: PatientHomeComponent,
    onNavTabSelected: (PatientTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state      by component.state.subscribeAsState()
        val colors     = AppTheme.colors
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize().background(colors.sand)) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // ── Scrollable content ────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier            = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
                ) {
                    // Header
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text  = "Buenos días, Juan 👋",
                            style = typography.body.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = colors.text,
                        )
                        Icon(
                            imageVector        = Lucide.Bell,
                            contentDescription = null,
                            tint               = colors.navy,
                            modifier           = Modifier.size(22.dp),
                        )
                    }

                    // Appointment card
                    AppointmentCard(
                        appt        = state.nextAppointment,
                        colors      = colors,
                        typography  = typography,
                        onApptClick = component::onAppointmentDetailTapped,
                    )

                    // Quick actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        QuickActionCard(
                            icon     = Lucide.Search,
                            title    = "Buscar Doctores",
                            subtitle = "Encuentra especialistas",
                            onClick  = component::onSearchTapped,
                            modifier = Modifier.weight(1f),
                        )
                        QuickActionCard(
                            icon     = Lucide.Calendar,
                            title    = "Mis Citas",
                            subtitle = "Ver todas tus citas",
                            onClick  = component::onAppointmentsTapped,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Stats row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        StatCard(
                            icon     = Lucide.Calendar,
                            label    = "${state.upcomingCount} Citas",
                            onClick  = component::onAppointmentsTapped,
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon     = Lucide.Package,
                            label    = "2 Paquetes",
                            onClick  = component::onPackagesTapped,
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon     = Lucide.Star,
                            label    = "PREMIUM",
                            onClick  = component::onPremiumTapped,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // ── CTA: ver mi cita ──────────────────────────────────────────
                val nextAppt = state.nextAppointment
                if (nextAppt != null) {
                    AppButton(
                        text     = "Ver mi cita →",
                        onClick  = { component.onAppointmentDetailTapped(nextAppt.id) },
                        size     = AppButtonSize.Lg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

private val CardShape  = RoundedCornerShape(16.dp)
private val CardShadow = Color(0x18000000)

/**
 * Elevated rounded card surface shared by every card on the home screen.
 * Pass [onClick] to make the surface tappable (ripple-less, like the rest of the UI).
 */
@Composable
private fun Modifier.cardSurface(onClick: (() -> Unit)? = null): Modifier {
    val base = this
        .shadow(4.dp, CardShape, ambientColor = CardShadow, spotColor = CardShadow)
        .clip(CardShape)
        .background(AppTheme.colors.elevated)
    return if (onClick == null) {
        base
    } else {
        base.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication        = null,
            onClick           = onClick,
        )
    }
}

@Composable
private fun AppointmentCard(
    appt: Appointment?,
    colors: AppColors,
    typography: AppTypography,
    onApptClick: (String) -> Unit,
) {
    if (appt == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .cardSurface()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("No tienes citas próximas", color = colors.muted, fontSize = 14.sp)
        }
        return
    }

    val ldt = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val doctorLabel = appt.doctorName?.let { "Dr. $it" } ?: "Doctor asignado"
    val subtitleLine = buildString {
        appt.specialtyName?.let { append("$it · ") }
        append("${ldt.day} ${monthShort(ldt.month.number)} · ${fmtHour(ldt.hour, ldt.minute)}")
    }
    val visitLabel = when (appt.visitType) {
        VisitType.VIRTUAL -> "Telemedicina"
        VisitType.HOME -> "Visita a domicilio"
        VisitType.CLINIC -> "Presencial"
    }
    val (statusLabel, statusBg, statusFg) = when (appt.status) {
        AppointmentStatus.CONFIRMED, AppointmentStatus.SCHEDULED -> Triple("CONFIRMADA", colors.greenBg, colors.green)
        AppointmentStatus.PENDING_PAYMENT -> Triple("PAGO PENDIENTE", colors.amberBg, colors.amber)
        AppointmentStatus.IN_PROGRESS -> Triple("EN CURSO", colors.navyTint, colors.navy)
        else -> Triple(appt.status.name, colors.navyTint, colors.navy)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .cardSurface(onClick = { onApptClick(appt.id) }),
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(colors.navy))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 12.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Text("PRÓXIMA CITA", color = colors.navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(doctorLabel, style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.text)
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(statusLabel, color = statusFg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(subtitleLine, style = typography.subtitle, color = colors.text)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("S/. ${appt.consultationFee.toInt()}", style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.text)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Lucide.House, contentDescription = null, tint = colors.text, modifier = Modifier.size(13.dp))
                    Text(visitLabel, style = typography.body.copy(fontSize = 12.sp), color = colors.text)
                }
            }
        }
    }
}

private fun monthShort(m: Int) = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")[m - 1]
private fun fmtHour(h: Int, min: Int): String {
    val period = if (h >= 12) "PM" else "AM"
    val dh = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
    return "${dh.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')} $period"
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = modifier
            .cardSurface(onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = colors.navy,
            modifier           = Modifier.size(24.dp),
        )
        Text(
            text  = title,
            style = typography.body.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
        Text(
            text  = subtitle,
            style = typography.body.copy(fontSize = 11.sp),
            color = colors.muted,
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier            = modifier
            .cardSurface(onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = colors.navy,
            modifier           = Modifier.size(18.dp),
        )
        Text(
            text     = label,
            style    = typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color    = colors.text,
            maxLines = 1,
        )
    }
}
