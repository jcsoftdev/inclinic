package com.inclinic.app.features.doctor.onboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.inclinic.app.features.doctor.onboarding.presentation.component.StepHorariosComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.OnboardingHeader
import com.inclinic.app.ui.atoms.OnboardingProgress
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.theme.AppTheme

private val DAY_LABELS = mapOf(
    "MONDAY" to "Lunes",
    "TUESDAY" to "Martes",
    "WEDNESDAY" to "Miércoles",
    "THURSDAY" to "Jueves",
    "FRIDAY" to "Viernes",
    "SATURDAY" to "Sábado",
    "SUNDAY" to "Domingo",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingHorariosScreen(
    component: StepHorariosComponent,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.sand)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.dimens.spacing20, vertical = AppTheme.dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingMd),
        ) {
            OnboardingHeader(onBack = onBack, modifier = Modifier.fillMaxWidth())

            OnboardingProgress(current = 4, total = 5, modifier = Modifier.fillMaxWidth())

            SectionHeader(
                title = "Tus horarios",
                subtitle = "Define los días y horas que atiendes",
            )

            state.error?.let { msg ->
                ErrorBanner(message = msg, modifier = Modifier.fillMaxWidth())
            }

            state.enabledDays.forEach { (day, enabled) ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppTheme.dimens.radiusMd))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radiusMd))
                        .padding(AppTheme.dimens.spacing12),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = DAY_LABELS[day] ?: day,
                            style = AppTheme.typography.subtitle,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text,
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { component.onToggleDay(day) },
                        )
                    }

                    if (enabled) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            (8..20).forEach { hour ->
                                val selected = hour in (state.slots[day] ?: emptyList())
                                HourChip(
                                    hour = hour,
                                    selected = selected,
                                    onClick = { component.onToggleSlot(day, hour) },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            AppButton(
                text = "Continuar",
                onClick = component::onContinueClicked,
                enabled = state.canContinue,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AppTheme.dimens.spacingMd))
        }

        LoadingOverlay(visible = state.isLoading)
    }
}

@Composable
private fun HourChip(
    hour: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val bg = if (selected) colors.navy else colors.lav50
    val textColor = if (selected) Color.White else colors.text
    val label = "${hour}:00"
    val shape = RoundedCornerShape(AppTheme.dimens.radiusChip)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(52.dp, 32.dp)
            .clip(shape)
            .background(bg)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}
