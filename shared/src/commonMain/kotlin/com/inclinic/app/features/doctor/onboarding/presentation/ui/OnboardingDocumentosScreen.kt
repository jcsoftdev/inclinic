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
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.presentation.component.StepDocumentosComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.OnboardingHeader
import com.inclinic.app.ui.atoms.OnboardingProgress
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.molecules.DocumentUploader
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun OnboardingDocumentosScreen(
    component: StepDocumentosComponent,
    onBack: () -> Unit = {},
    /**
     * Platform-level file picker. Callers (Android/iOS) inject the actual picker;
     * the Compose layer is fully stateless and receives the result as a lambda.
     * [kind] identifies which document slot was tapped.
     */
    onPickFile: (kind: DocKind) -> Unit = {},
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

            OnboardingProgress(current = 2, total = 5, modifier = Modifier.fillMaxWidth())

            SectionHeader(
                title = "Documentos legales",
                subtitle = "Sube imágenes claras o PDF (máx 5 MB c/u)",
            )

            state.error?.let { msg ->
                ErrorBanner(message = msg, modifier = Modifier.fillMaxWidth())
            }

            DocumentUploader(
                label = "Licencia CMP",
                hint = "Toca para subir tu licencia CMP",
                state = state.cmpState,
                onPickClick = { onPickFile(DocKind.CMP_LICENSE) },
                modifier = Modifier.fillMaxWidth(),
            )

            DocumentUploader(
                label = "DNI — Frente",
                hint = "Toca para subir el frente de tu DNI",
                state = state.idFrontState,
                onPickClick = { onPickFile(DocKind.ID_FRONT) },
                modifier = Modifier.fillMaxWidth(),
            )

            DocumentUploader(
                label = "DNI — Reverso",
                hint = "Toca para subir el reverso de tu DNI",
                state = state.idBackState,
                onPickClick = { onPickFile(DocKind.ID_BACK) },
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
