package com.inclinic.app.features.doctor.sharing.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.sharing.presentation.component.RequestShareComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RequestShareScreen(
    component: RequestShareComponent,
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
        // Header
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
                text = "Solicitar acceso",
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
            // Info banner
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radiusLarge))
                    .background(colors.blueBg)
                    .padding(dimens.spacing12),
            ) {
                Icon(Lucide.Info, contentDescription = null, tint = colors.blue, modifier = Modifier.size(18.dp))
                Text(
                    text = "El paciente recibirá una notificación y deberá aprobar tu acceso. Plazo: 7 días.",
                    style = typography.subtitle.copy(fontSize = 11.sp),
                    color = colors.blue,
                )
            }

            Text(
                text = "PACIENTE",
                style = typography.label,
                color = colors.muted,
                letterSpacing = 0.8.sp,
            )
            AppTextField(
                value = state.patientId,
                onValueChange = component::onPatientIdChange,
                label = "ID del paciente",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "RAZÓN DE LA SOLICITUD",
                style = typography.label,
                color = colors.muted,
                letterSpacing = 0.8.sp,
            )
            AppTextField(
                value = state.reason,
                onValueChange = component::onReasonChange,
                label = "Motivo (mínimo 20 caracteres)",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

            AppButton(
                text = "Enviar solicitud",
                onClick = component::onSubmit,
                loading = state.isSubmitting,
                enabled = !state.isSubmitting && state.patientId.isNotBlank() && state.reason.length >= 20,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
