package com.inclinic.app.features.doctor.onboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.doctor.onboarding.presentation.component.EnviadoComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun EnviadoScreen(
    component: EnviadoComponent,
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
                .padding(horizontal = AppTheme.dimens.spacingXl, vertical = AppTheme.dimens.spacingXxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacing20),
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colors.greenBg),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(colors.green),
                ) {
                    Icon(
                        imageVector = Lucide.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }

            Text(
                text = "Solicitud enviada",
                style = typography.displayMedium,
                color = colors.text,
                textAlign = TextAlign.Center,
            )

            ChipStatus(
                label = when (state.status) {
                    OnboardingStatus.PENDING -> "En revisión"
                    OnboardingStatus.APPROVED -> "Aprobado"
                    OnboardingStatus.REJECTED -> "Rechazado"
                    OnboardingStatus.NONE -> "Sin estado"
                },
                kind = when (state.status) {
                    OnboardingStatus.PENDING -> ChipStatusKind.Warning
                    OnboardingStatus.APPROVED -> ChipStatusKind.Success
                    OnboardingStatus.REJECTED -> ChipStatusKind.Error
                    OnboardingStatus.NONE -> ChipStatusKind.Neutral
                },
            )

            Text(
                text = when (state.status) {
                    OnboardingStatus.PENDING ->
                        "Estamos revisando tu solicitud. Te notificaremos cuando sea aprobada."
                    OnboardingStatus.APPROVED ->
                        "¡Tu solicitud fue aprobada! Ya puedes recibir pacientes."
                    OnboardingStatus.REJECTED ->
                        "Tu solicitud fue rechazada. Revisa los comentarios y corrige la información."
                    OnboardingStatus.NONE ->
                        "Tu solicitud está siendo procesada."
                },
                style = typography.body,
                color = colors.muted,
                textAlign = TextAlign.Center,
            )

            state.error?.let { msg ->
                ErrorBanner(message = msg, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.weight(1f))

            AppButton(
                text = "Cerrar sesión",
                onClick = component::onLogOutClicked,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        LoadingOverlay(visible = state.isLoading)
    }
}
