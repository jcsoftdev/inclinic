package com.inclinic.app.features.doctor.onboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.onboarding.presentation.component.StepEspecialidadesComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.OnboardingHeader
import com.inclinic.app.ui.atoms.OnboardingProgress
import com.inclinic.app.ui.atoms.SectionHeader
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingEspecialidadesScreen(
    component: StepEspecialidadesComponent,
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

            OnboardingProgress(current = 3, total = 5, modifier = Modifier.fillMaxWidth())

            SectionHeader(
                title = "Tus especialidades",
                subtitle = "Selecciona todas las que ejerces",
            )

            state.error?.let { msg ->
                ErrorBanner(message = msg, modifier = Modifier.fillMaxWidth())
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
            ) {
                state.availableSpecialties.forEach { specialty ->
                    SpecialtyChip(
                        label = specialty,
                        selected = specialty in state.selectedSpecialtyIds,
                        onClick = { component.onToggleSpecialty(specialty) },
                    )
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
private fun SpecialtyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(18.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) colors.navy else colors.surface)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.border, shape))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = AppTheme.dimens.spacing12),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.label,
            color = if (selected) Color.White else colors.text,
        )
    }
}
