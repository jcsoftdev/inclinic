package com.inclinic.app.features.doctor.therapy_offers.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferComponent
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.CreateTherapyOfferState
import com.inclinic.app.features.doctor.therapy_offers.presentation.component.SpecialtyOption
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.AppToggle
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun CreateTherapyOfferScreen(
    component: CreateTherapyOfferComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // AppHeader Doctor: back + title "Nueva oferta"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Atras")
            Text(
                text = "Nueva oferta",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        // Form body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacingSm),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
        ) {
            // Field 1: title
            AppTextField(
                value = state.title,
                onValueChange = component::onTitleChange,
                label = "Titulo de la oferta",
                placeholder = "Terapia Cardio Premium",
                error = state.titleError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Field 2: specialty
            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Especialidad", style = typography.label, color = colors.muted, fontWeight = FontWeight.SemiBold)
                OfferSpecialtyDropdown(component, state)
            }

            // Row: N sessions + price/session
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppTextField(
                    value = state.totalSessions,
                    onValueChange = component::onTotalSessionsChange,
                    label = "N sesiones",
                    placeholder = "8",
                    error = state.sessionsError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = state.pricePerSession,
                    onValueChange = component::onPricePerSessionChange,
                    label = "Precio / sesion",
                    placeholder = "80",
                    error = state.priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // Description text area
            AppTextField(
                value = state.description,
                onValueChange = component::onDescriptionChange,
                label = "Descripcion",
                placeholder = "Describe que incluye la oferta...",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            )

            // Active toggle row (matches design "Publicar como activa" switch)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radiusLarge))
                    .background(colors.surface)
                    .padding(dimens.spacingMd),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Publicar como activa",
                        style = typography.body,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    Text(
                        "Visible para pacientes al guardar",
                        style = typography.subtitle,
                        color = colors.muted,
                    )
                }
                AppToggle(checked = state.isActive, onCheckedChange = component::onActiveToggle)
            }

            ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())
        }

        // BottomActionBar: "Publicar oferta" primary + "Cancelar" secondary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            AppButton(
                text = if (state.isSubmitting) "Publicando..." else "Publicar oferta",
                onClick = component::onSubmit,
                loading = state.isSubmitting,
                enabled = !state.isSubmitting,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Cancelar",
                onClick = component::onBack,
                size = AppButtonSize.Lg,
                variant = AppButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OfferSpecialtyDropdown(
    component: CreateTherapyOfferComponent,
    state: CreateTherapyOfferState,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
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
            .clip(RoundedCornerShape(dimens.radius))
            .background(colors.lav50)
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
