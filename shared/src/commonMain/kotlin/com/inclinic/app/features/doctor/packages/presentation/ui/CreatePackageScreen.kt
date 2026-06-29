package com.inclinic.app.features.doctor.packages.presentation.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.AppToggle
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun CreatePackageScreen(component: CreatePackageComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Atrás")
            Text(
                text = "Crear Paquete",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacingSm),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
        ) {
            // ── Patient ───────────────────────────────────────────────────────
            SectionLabel("PACIENTE")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(dimens.radiusLarge))
                    .background(colors.surface)
                    .padding(dimens.spacingMd),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(dimens.radiusPill))
                        .background(colors.blueBg),
                ) {
                    Icon(Lucide.User, contentDescription = null, tint = colors.blue, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.patientName.ifBlank { "Selecciona un paciente" },
                        style = typography.body,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    if (state.patientEmail.isNotBlank()) {
                        Text(state.patientEmail, style = typography.subtitle, color = colors.muted)
                    }
                }
            }

            // ── Detail ────────────────────────────────────────────────────────
            SectionLabel("DETALLE DEL PAQUETE")
            AppTextField(
                value = state.packageName,
                onValueChange = component::onPackageNameChange,
                label = "Nombre",
                placeholder = "Plan nutricional 6 meses",
                error = state.nameError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing12), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                    Text("Especialidad", style = typography.label, color = colors.muted)
                    SpecialtyDropdown(component, state)
                }
                AppTextField(
                    value = state.totalSessions,
                    onValueChange = component::onTotalSessionsChange,
                    label = "Sesiones",
                    error = state.sessionsError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Price per session ─────────────────────────────────────────────
            SectionLabel("PRECIO POR SESIÓN")
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing12), modifier = Modifier.fillMaxWidth()) {
                AppTextField(
                    value = state.regularPrice,
                    onValueChange = component::onRegularPriceChange,
                    label = "Regular",
                    placeholder = "70",
                    error = state.regularPriceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = state.packagePrice,
                    onValueChange = component::onPackagePriceChange,
                    label = "Paquete",
                    placeholder = "60",
                    error = state.packagePriceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Options ───────────────────────────────────────────────────────
            ToggleRow(
                title = "Prepago",
                subtitle = "Paciente paga todas las sesiones por adelantado",
                checked = state.isPrepaid,
                onCheckedChange = component::onPrepaidToggle,
            )
            ToggleRow(
                title = "Visita a domicilio",
                subtitle = "Sesiones en casa del paciente",
                checked = state.isHomeVisit,
                onCheckedChange = component::onHomeVisitToggle,
            )

            // ── Total ─────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(dimens.radiusLarge))
                    .background(colors.navyTint)
                    .padding(dimens.spacingMd),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.isPrepaid) "Total con descuento" else "Total",
                        style = typography.body,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    if (state.isPrepaid && state.discountSavings > 0) {
                        Text(
                            text = "Ahorra ${formatSoles(state.discountSavings)}",
                            style = typography.subtitle,
                            color = colors.teal,
                        )
                    }
                }
                Text(
                    text = formatSoles(state.totalWithDiscount),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.navy,
                )
            }

            ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacing12),
        ) {
            AppButton(
                text = if (state.isSubmitting) "Enviando..." else "Enviar propuesta",
                onClick = component::onSubmit,
                loading = state.isSubmitting,
                enabled = !state.isSubmitting,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.label,
        color = AppTheme.colors.muted,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun SpecialtyDropdown(
    component: CreatePackageComponent,
    state: com.inclinic.app.features.doctor.packages.presentation.component.CreatePackageState,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    // Simple cycling selector (no overlay menu in shared atoms): tapping advances
    // to the next specialty. Matches the design's single-select dropdown intent.
    val selected = state.specialties.firstOrNull { it.id == state.selectedSpecialtyId }
    val onTap = {
        val list = state.specialties
        if (list.isNotEmpty()) {
            val idx = list.indexOfFirst { it.id == state.selectedSpecialtyId }
            val next = list[(idx + 1).mod(list.size)]
            component.onSpecialtySelected(next.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(dimens.radius))
            .background(colors.lav50)
            .then(
                if (state.specialtyError != null) {
                    Modifier.border(1.dp, colors.error, androidx.compose.foundation.shape.RoundedCornerShape(dimens.radius))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onTap)
            .padding(horizontal = dimens.spacing12),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = selected?.name ?: "Selecciona",
            style = typography.body,
            color = if (selected != null) colors.text else colors.light,
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = typography.body, fontWeight = FontWeight.Bold, color = colors.text)
            Text(subtitle, style = typography.subtitle, color = colors.muted)
        }
        AppToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}
