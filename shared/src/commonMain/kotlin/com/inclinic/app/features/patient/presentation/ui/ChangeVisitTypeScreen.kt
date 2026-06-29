package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

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
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.patient.presentation.component.ChangeVisitTypeComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChangeVisitTypeScreen(component: ChangeVisitTypeComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(modifier.fillMaxSize().background(colors.sand)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppBackButton(onClick = component::onBack)
            Text("Cambio de Modo", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else {
            val appt = state.appointment

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Doctor card with current type
                if (appt != null) {
                    val initials = appt.doctorName?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.uppercase() }
                        ?.take(2)?.joinToString("") ?: "DR"
                    val currentTypeLabel = when (appt.visitType) {
                        VisitType.VIRTUAL -> "TELEMEDICINA"
                        VisitType.HOME -> "DOMICILIO"
                        VisitType.CLINIC -> "OFICINA"
                    }
                    val dt = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateStr = "${dt.day}/${dt.month.number.toString().padStart(2, '0')}/${dt.year} ${formatTimeCVT(dt.hour, dt.minute)}"

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
                                    Text("Dr. ${appt.doctorName ?: ""}", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    appt.specialtyName?.let { Text(it, color = colors.muted, fontSize = 11.sp) }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.lav.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(currentTypeLabel, color = colors.navy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Lucide.Calendar, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                            Text(dateStr, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // "CAMBIAR A" section — elevated card wrapping options (design: #1A1D2B)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.elevated)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("CAMBIAR A", color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    val visitTypes = VisitType.entries.filter { it != appt?.visitType }
                    visitTypes.forEach { type ->
                        val isSelected = state.newVisitType == type
                        val (icon, label) = when (type) {
                            VisitType.VIRTUAL -> Lucide.Video to "Telemedicina"
                            VisitType.HOME -> Lucide.House to "Visita a domicilio"
                            VisitType.CLINIC -> Lucide.Building2 to "Consulta presencial"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) colors.navy else colors.border,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable { component.onNewVisitTypeSelected(type) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) colors.navy else colors.muted, modifier = Modifier.size(20.dp))
                            Text(label, color = colors.text, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }

                // Address field (only for HOME)
                if (state.newVisitType == VisitType.HOME) {
                    Text("DIRECCION", color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Lucide.MapPin, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
                        BasicTextField(
                            value = state.address,
                            onValueChange = component::onAddressChanged,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                            decorationBox = { inner ->
                                Box {
                                    if (state.address.isEmpty()) {
                                        Text("Ingresa tu direccion completa", color = colors.light, fontSize = 14.sp)
                                    }
                                    inner()
                                }
                            },
                        )
                    }
                }

                // Info banner
                InfoBanner(
                    title = "Esperando respuesta del doctor",
                    description = "Sujeto a aprobación. El precio puede ajustarse según la zona y horario.",
                    tone = InfoBannerTone.Warning,
                    icon = Lucide.Hourglass,
                )

                // Reason
                Text("MOTIVO DEL CAMBIO", color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                BasicTextField(
                    value = state.reason,
                    onValueChange = component::onReasonChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                    decorationBox = { inner ->
                        Box {
                            if (state.reason.isEmpty()) {
                                Text("Explica por que necesitas el cambio...", color = colors.light, fontSize = 14.sp)
                            }
                            inner()
                        }
                    },
                )

                // Error
                state.error?.let { err ->
                    Text(err, color = colors.red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // CTA — top divider matches design strokeWidth:{top:1}
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
            ) {
                val canSubmit = state.newVisitType != null && !state.isSubmitting &&
                    (state.newVisitType != VisitType.HOME || state.address.isNotBlank())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canSubmit) colors.navy else colors.navy.copy(alpha = 0.4f))
                        .then(if (canSubmit) Modifier.clickable(onClick = component::onSubmit) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Solicitar cambio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatTimeCVT(hour: Int, minute: Int): String {
    val period = if (hour >= 12) "p.m." else "a.m."
    val h = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
    return "${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $period"
}
