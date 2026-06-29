package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Timer
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.features.patient.presentation.component.BookingComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun BookingScreen(component: BookingComponent, modifier: Modifier = Modifier) {
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
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBackButton(onClick = component::onBack)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Confirmar Cita", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(48.dp))
        }

        // Progress bar — step 3 of 3 (all filled)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) {
                Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.navy))
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Summary card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x0F000000))
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.elevated)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // Doctor row
                state.doctor?.let { DoctorRow(it, colors) }

                // Separator
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

                // consultType hoisted so it's also available in the payment summary below
                val consultType = when (state.visitType?.name) {
                    "VIRTUAL" -> "Telemedicina"
                    "HOME"    -> "Visita a domicilio"
                    else      -> "Consulta presencial"
                }

                // Detail rows
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    val dateFormatted = state.date.ifBlank { "—" }
                    val timeFormatted = state.startTime.ifBlank { "—" }
                    DetailRow(Lucide.Building2, "Tipo",  consultType)
                    DetailRow(Lucide.Calendar,  "Fecha", dateFormatted)
                    DetailRow(Lucide.Timer,     "Hora",  timeFormatted, valueTestTag = "booking_hora_value")
                    DetailRow(Lucide.MapPin,    "Lugar", "Miraflores")
                }

                // Separator
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

                // Payment summary
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Text(
                        "RESUMEN DE PAGO",
                        color = colors.muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    val fee = state.doctor?.consultationFee?.toInt() ?: 0
                    val commission = (fee * 0.07).toInt().coerceAtLeast(5)
                    val total = fee + commission
                    PaymentRow(consultType, "S/.$fee")
                    PaymentRow("Comisión plataforma", "S/.$commission")
                    // Total row
                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Total", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("S/.$total", color = colors.navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Motivo field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.elevated)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "Motivo de consulta (opcional)",
                    color = colors.light,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                BasicTextField(
                    value = state.notes,
                    onValueChange = component::onNotesChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        color = colors.text,
                    ),
                    cursorBrush = SolidColor(colors.navy),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    decorationBox = { inner ->
                        Box {
                            if (state.notes.isEmpty()) {
                                Text(
                                    "Ej: Control rutinario, seguimiento dieta...",
                                    color = colors.light,
                                    fontSize = 13.sp,
                                )
                            }
                            inner()
                        }
                    },
                )
            }

            // Security note
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Lucide.ShieldCheck, contentDescription = null, tint = colors.teal, modifier = Modifier.size(14.dp))
                Text("Pago procesado de forma segura", color = colors.muted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(4.dp))
        }

        // Error banner
        state.error?.let { err ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.errorBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(err, color = colors.error, fontSize = 13.sp)
            }
        }

        // CTA area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            val fee = state.doctor?.consultationFee?.toInt() ?: 0
            val commission = (fee * 0.07).toInt().coerceAtLeast(5)
            val total = fee + commission
            val interactionSource = remember { MutableInteractionSource() }

            val busy = state.isLoading || state.isLoadingSkip

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (busy) colors.navy.copy(alpha = 0.6f) else colors.navy)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = component::onConfirm,
                        enabled = !busy,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Confirmar y Pagar  S/.$total",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = component::onSkipPayment,
                        enabled = !busy,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoadingSkip) {
                    CircularProgressIndicator(color = colors.muted, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        "Solo agendar, pagar después",
                        color = colors.muted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }


    }
}

@Composable
private fun DoctorRow(doctor: Doctor, colors: com.inclinic.app.ui.theme.AppColors) {
    val initials = doctor.fullName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.navyLight),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(doctor.fullName, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (doctor.specialties.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    doctor.specialties.take(1).forEach { spec ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.tealBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                spec.name.uppercase(),
                                color = colors.teal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    /** Optional semantic tag placed on the value [Text] node — used by instrumented e2e tests. */
    valueTestTag: String? = null,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(colors.base),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
        }
        Text(
            label,
            color = colors.light,
            fontSize = 13.sp,
            modifier = Modifier.width(60.dp),
        )
        Text(
            value,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = if (valueTestTag != null) Modifier.testTag(valueTestTag) else Modifier,
        )
    }
}

@Composable
private fun PaymentRow(label: String, amount: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = colors.muted, fontSize = 13.sp)
        Text(amount, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
