package com.inclinic.app.features.doctor.onboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.onboarding.presentation.component.CorregirSolicitudComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun CorregirSolicitudScreen(
    component: CorregirSolicitudComponent,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.sand)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.dimens.spacing20, vertical = AppTheme.dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingMd),
        ) {
            AppBackButton(onClick = onBack)

            SectionHeader(
                title = "Corregir solicitud",
                subtitle = "Actualiza los campos indicados y reenvía tu solicitud",
            )

            state.error?.let { msg ->
                ErrorBanner(message = msg, modifier = Modifier.fillMaxWidth())
            }

            // Render one editable field per correction key
            state.corrections.forEach { (field, value) ->
                AppTextField(
                    value = value,
                    onValueChange = { component.onFieldChanged(field, it) },
                    label = field,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.weight(1f))

            AppButton(
                text = if (state.submitSuccess) "Enviado ✓" else "Reenviar solicitud",
                onClick = component::onSubmitClicked,
                loading = state.isSubmitting,
                enabled = !state.isSubmitting && !state.submitSuccess,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AppTheme.dimens.spacingMd))
        }

        LoadingOverlay(visible = state.isSubmitting)
    }
}
