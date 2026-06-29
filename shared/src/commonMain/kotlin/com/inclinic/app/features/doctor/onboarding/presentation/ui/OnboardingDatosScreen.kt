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
import com.inclinic.app.features.doctor.onboarding.presentation.component.StepDatosComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.OnboardingHeader
import com.inclinic.app.ui.atoms.OnboardingProgress
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun OnboardingDatosScreen(
    component: StepDatosComponent,
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
            OnboardingHeader(onBack = onBack, modifier = Modifier.fillMaxWidth())

            OnboardingProgress(current = 1, total = 5, modifier = Modifier.fillMaxWidth())

            SectionHeader(
                title = "Datos personales",
                subtitle = "Empieza con tu información básica",
            )

            state.error?.let { msg ->
                ErrorBanner(
                    message = msg,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AppTextField(
                value = state.firstName,
                onValueChange = component::onFirstNameChanged,
                label = "Nombres",
                placeholder = "Ej. Juan Carlos",
                error = state.firstNameError,
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value = state.lastName,
                onValueChange = component::onLastNameChanged,
                label = "Apellidos",
                placeholder = "Ej. Pérez García",
                error = state.lastNameError,
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value = state.cmpLicense,
                onValueChange = component::onCmpLicenseChanged,
                label = "Número de CMP",
                placeholder = "Ej. CMP-12345",
                error = state.cmpLicenseError,
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value = state.phone,
                onValueChange = component::onPhoneChanged,
                label = "Celular",
                placeholder = "Ej. 987 654 321",
                error = state.phoneError,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            AppButton(
                text = "Continuar",
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
