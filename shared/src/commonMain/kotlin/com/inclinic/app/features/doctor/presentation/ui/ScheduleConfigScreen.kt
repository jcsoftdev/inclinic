package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.presentation.component.DayScheduleUi
import com.inclinic.app.features.doctor.presentation.component.ScheduleConfigComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.AppToggle
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.DayOfWeek

private val SLOT_OPTIONS = listOf(15, 20, 30, 45, 60)

private fun DayOfWeek.label(): String = when (this) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miércoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleConfigScreen(component: ScheduleConfigComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(Modifier.fillMaxSize().background(colors.sand).then(modifier)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(colors.surface).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Atrás")
            Text("Editar Horarios", style = AppTheme.typography.titleLarge, color = colors.text)
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.error?.let { Text(it, style = AppTheme.typography.subtitle, color = colors.red) }
            if (state.saveSuccess) {
                Text("Horario guardado correctamente", style = AppTheme.typography.subtitle, color = colors.green)
            }

            Text(
                "SEMANA ESTÁNDAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = colors.light,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            state.days.forEach { day ->
                DayCard(
                    ui = day,
                    onToggle = { component.onToggleDay(day.day) },
                    onExpand = { component.onExpandDay(day.day) },
                    onStart = { component.onStartTimeChange(day.day, it) },
                    onEnd = { component.onEndTimeChange(day.day, it) },
                    onMax = { component.onMaxPatientsChange(day.day, it) },
                    onSlot = { component.onSlotDurationChange(day.day, it) },
                    onPrice = { component.onPriceChange(day.day, it) },
                    onNegotiation = { component.onToggleAllowNegotiation(day.day) },
                )
            }

            Spacer(Modifier.height(4.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(AppTheme.dimens.radius))
                    .background(colors.navy)
                    .clickable(enabled = !state.isSaving, onClick = component::onSave),
            ) {
                if (state.isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Guardar horarios", color = Color.White, style = AppTheme.typography.buttonLg, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun DayCard(
    ui: DayScheduleUi,
    onToggle: () -> Unit,
    onExpand: () -> Unit,
    onStart: (String) -> Unit,
    onEnd: (String) -> Unit,
    onMax: (String) -> Unit,
    onSlot: (Int) -> Unit,
    onPrice: (String) -> Unit,
    onNegotiation: () -> Unit,
) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radiusLarge))
            .background(colors.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ui.day.label(), style = typography.titleLarge, fontSize = 16.sp, color = colors.text)
                if (!ui.expanded) {
                    Text(
                        if (ui.enabled)
                            "${ui.startTime} — ${ui.endTime} · ${ui.slotDuration} min"
                        else "Cerrado",
                        fontSize = 12.sp,
                        color = colors.muted,
                    )
                }
            }
            AppToggle(checked = ui.enabled, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Lucide.ChevronDown,
                contentDescription = if (ui.expanded) "Contraer" else "Expandir",
                tint = colors.muted,
                modifier = Modifier.size(20.dp).clickable { onExpand() },
            )
        }

        if (ui.enabled && ui.expanded) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = ui.startTime, onValueChange = onStart, label = "Inicio",
                    placeholder = "08:00", modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = ui.endTime, onValueChange = onEnd, label = "Fin",
                    placeholder = "13:00", modifier = Modifier.weight(1f),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = ui.maxPatients, onValueChange = onMax, label = "Max pacientes",
                    placeholder = "8", modifier = Modifier.weight(1f),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Duración slot", style = typography.subtitle, color = colors.muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SLOT_OPTIONS.forEach { minutes ->
                            val selected = minutes == ui.slotDuration
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppTheme.dimens.radius))
                                    .background(if (selected) colors.navy else colors.sand)
                                    .clickable { onSlot(minutes) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    "$minutes",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else colors.text,
                                )
                            }
                        }
                    }
                }
            }
            AppTextField(
                value = ui.price, onValueChange = onPrice, label = "Precio (S/)",
                placeholder = "120", modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Permitir cambio domicilio/consultorio",
                    style = typography.subtitle,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                AppToggle(checked = ui.allowNegotiation, onCheckedChange = { onNegotiation() })
            }
        }
    }
}
