package com.inclinic.app.features.patient.presentation.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Paperclip
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.ThumbsDown
import com.composables.icons.lucide.UserX
import com.inclinic.app.core.model.DisputeReason
import com.inclinic.app.features.patient.presentation.component.DisputeAppointmentComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeAppointmentScreen(component: DisputeAppointmentComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            title = { Text("Disputar Cita", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            windowInsets = WindowInsets(0),
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Red warning banner — design: sfg2r component (redBg fill, red icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.redBg)
                        .border(1.dp, colors.red, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(Lucide.ShieldCheck, contentDescription = null, tint = colors.red, modifier = Modifier.size(22.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text("Esto inicia una revisión administrativa", color = colors.red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "El pago quedará en custodia hasta que el equipo resuelva tu caso.",
                            color = colors.red,
                            fontSize = 12.sp,
                        )
                    }
                }

                // Section: ¿QUÉ SUCEDIÓ? — design uses bJsuz SectionLabel
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "¿QUÉ SUCEDIÓ?",
                        color = colors.muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    )

                    // Option cards — design uses AINse SelectableOptionCard with icon + label + subtitle
                    val options = listOf(
                        Triple(DisputeReason.DOCTOR_NO_SHOW, Lucide.UserX, "El doctor no asistió a la consulta" to "El médico no se presentó en el horario acordado"),
                        Triple(DisputeReason.INADEQUATE_SERVICE, Lucide.ThumbsDown, "Servicio inadecuado o incompleto" to "La atención no cumplió con lo prometido"),
                        Triple(DisputeReason.INCORRECT_CHARGE, Lucide.CreditCard, "Cobro indebido" to "Se realizó un cobro que no corresponde"),
                    )

                    options.forEach { (reason, icon, texts) ->
                        val (label, subtitle) = texts
                        val isSelected = state.selectedReason == reason
                        DisputeOptionCard(
                            icon = icon,
                            label = label,
                            subtitle = subtitle,
                            isSelected = isSelected,
                            onClick = { component.onReasonSelected(reason) },
                        )
                    }
                }

                // Section: CUÉNTANOS LOS DETALLES with counter on the right
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "CUÉNTANOS LOS DETALLES",
                            color = colors.muted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            "${state.details.length} / 20 mín",
                            color = if (state.details.length < 20) colors.muted else colors.green,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    BasicTextField(
                        value = state.details,
                        onValueChange = component::onDetailsChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.elevated)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                        decorationBox = { inner ->
                            Box {
                                if (state.details.isEmpty()) {
                                    Text("Describe con detalle lo que ocurrió...", color = colors.light, fontSize = 14.sp)
                                }
                                inner()
                            }
                        },
                    )
                }

                // Attach evidence row — design: paperclip icon + label, elevated bg, border
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.elevated)
                        .border(1.dp, colors.border, RoundedCornerShape(10.dp)),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Lucide.Paperclip, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
                    Text("Adjuntar evidencia (opcional)", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Error
                state.error?.let { err ->
                    Text(err, color = colors.red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // CTA — design uses ccEUc ButtonDestructive = red filled
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(1.dp, colors.border)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 12.dp),
            ) {
                val canSubmit = state.selectedReason != null && state.details.isNotBlank() && !state.isSubmitting
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canSubmit) colors.red else colors.red.copy(alpha = 0.4f))
                        .then(if (canSubmit) Modifier.clickable(onClick = component::onSubmit) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Enviar disputa", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisputeOptionCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.elevated)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.navy else colors.border,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) colors.navy else colors.muted,
            modifier = Modifier.size(20.dp),
        )

        // Label + subtitle
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label,
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
            Text(
                subtitle,
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            )
        }

        // Radio circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) colors.navy else colors.border, CircleShape)
                .background(if (isSelected) colors.navy else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
    }
}
