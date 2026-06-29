package com.inclinic.app.features.doctor.onboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.onboarding.presentation.component.StepPreciosComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.OnboardingHeader
import com.inclinic.app.ui.atoms.OnboardingProgress
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun OnboardingPreciosScreen(
    component: StepPreciosComponent,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val typography = AppTheme.typography

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.sand)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.dimens.spacing20, vertical = AppTheme.dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingMd),
        ) {
            OnboardingHeader(onBack = onBack, modifier = Modifier.fillMaxWidth())

            OnboardingProgress(current = 5, total = 5, modifier = Modifier.fillMaxWidth())

            SectionHeader(
                title = "Tus precios",
                subtitle = "Define tarifa base y modalidades extra",
            )

            state.error?.let { msg ->
                ErrorBanner(message = msg, modifier = Modifier.fillMaxWidth())
            }

            AppTextField(
                value = state.consultationFeeText,
                onValueChange = component::onConsultationFeeChanged,
                label = "Tarifa de consulta (S/.)",
                placeholder = "Ej. 150",
                error = state.consultationFeeError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "Modalidades de atención",
                style = typography.label,
                color = colors.muted,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
            ) {
                Checkbox(
                    checked = state.supportsPresential,
                    onCheckedChange = component::onTogglePresential,
                )
                Text(text = "Presencial", style = typography.body, color = colors.text)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
            ) {
                Checkbox(
                    checked = state.supportsVirtual,
                    onCheckedChange = component::onToggleVirtual,
                )
                Text(text = "Virtual", style = typography.body, color = colors.text)
            }

            Spacer(Modifier.weight(1f))

            AppButton(
                text = "Enviar solicitud",
                onClick = component::onContinueClicked,
                loading = state.isLoading,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AppTheme.dimens.spacingMd))
        }

        LoadingOverlay(visible = state.isLoading)
    }
}
