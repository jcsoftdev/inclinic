package com.inclinic.app.features.doctor.reschedule_request.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import androidx.compose.ui.text.font.FontWeight
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.reschedule_request.presentation.component.RequestRescheduleComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RequestRescheduleScreen(
    component: RequestRescheduleComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Box(modifier.fillMaxSize().background(colors.sand)) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            return@Box
        }

        Column(Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.spacing20)
                    .height(52.dp),
            ) {
                AppBackButton(onClick = component::onBack, contentDescription = "Volver")
                Text(
                    text = "Solicitar Reagenda",
                    style = typography.displayNano,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimens.spacing20, vertical = dimens.spacingSm),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.radiusLarge))
                        .background(colors.navyTint)
                        .padding(dimens.spacing12),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "CITA ACTUAL",
                                style = typography.label,
                                color = colors.navy,
                                letterSpacing = 0.6.sp,
                            )
                            Text(
                                text = state.appointment?.startsAt?.toString() ?: "",
                                style = typography.subtitle.copy(fontSize = 16.sp),
                                color = colors.navyDark,
                            )
                        }
                        Icon(
                            Lucide.CalendarClock,
                            contentDescription = null,
                            tint = colors.navy,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    state.appointment?.let { appt ->
                        Text(
                            text = "${appt.patientId} · ${appt.visitType.name}",
                            style = typography.caption,
                            color = colors.navy,
                        )
                    }
                }

                Text(
                    text = "SLOT DISPONIBLE",
                    style = typography.label,
                    color = colors.muted,
                    letterSpacing = 0.8.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.availableSlots.forEach { slot ->
                        SlotChip(
                            label = slot,
                            selected = state.proposedSlot == slot,
                            onClick = { component.onSlotChange(slot) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Text(
                    text = "MOTIVO (OBLIGATORIO · MÍN 20)",
                    style = typography.label,
                    color = colors.muted,
                    letterSpacing = 0.8.sp,
                )
                AppTextField(
                    value = state.message,
                    onValueChange = component::onMessageChange,
                    label = "Explica al paciente por qué necesitas mover la cita...",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                InfoBanner(
                    title = "Si no responde en 24h",
                    description = "La cita original se mantiene tal como está agendada.",
                    modifier = Modifier.fillMaxWidth(),
                )

                ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

                AppButton(
                    text = "Enviar solicitud",
                    onClick = component::onSubmit,
                    loading = state.isSubmitting,
                    enabled = state.canSubmit,
                    size = AppButtonSize.Lg,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SlotChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val shape = RoundedCornerShape(dimens.radius)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) colors.navy else colors.elevated)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.border, shape))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) androidx.compose.ui.graphics.Color.White else colors.text,
        )
    }
}
