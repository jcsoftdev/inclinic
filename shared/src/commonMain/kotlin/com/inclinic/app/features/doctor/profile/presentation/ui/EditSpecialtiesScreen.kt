package com.inclinic.app.features.doctor.profile.presentation.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.HeartPulse
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.doctor.profile.presentation.component.EditSpecialtiesComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme

/**
 * Editar Especialidades screen (ItizF).
 *
 * Layout (Pencil):
 *   Header (back arrow + "Mis Especialidades")
 *   amber-bg info banner
 *   specialty cards (toggle selection)
 *   outlined "Solicitar nueva especialidad" CTA + "Guardar"
 */
@Composable
fun EditSpecialtiesScreen(
    component: EditSpecialtiesComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val dimens = AppTheme.dimens
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize().background(colors.sand)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(colors.surface)
                        .padding(horizontal = dimens.spacingMd),
                ) {
                    AppBackButton(onClick = component::onBack)
                    Text(
                        text = "Mis Especialidades",
                        style = typography.titleLarge,
                        color = colors.text,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd - 2.dp),
                ) {
                    ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.radius))
                            .background(colors.amberBg)
                            .padding(dimens.spacing12),
                    ) {
                        Icon(
                            imageVector = Lucide.Info,
                            contentDescription = null,
                            tint = colors.amber,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Cada especialidad debe ofrecer al menos una modalidad",
                            color = colors.amber,
                            style = typography.fieldError.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    state.availableSpecialties.forEach { specialty ->
                        SpecialtyCard(
                            specialty = specialty,
                            selected = specialty.id in state.selectedIds,
                            onClick = { component.onToggleSpecialty(specialty.id) },
                        )
                    }

                    AppButton(
                        text = "Solicitar nueva especialidad",
                        onClick = component::onNavigateToRequestSpecialty,
                        variant = AppButtonVariant.Outline,
                        size = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(dimens.spacingXs))

                    AppButton(
                        text = "Guardar",
                        onClick = component::onSave,
                        size = AppButtonSize.Lg,
                        loading = state.isSaving,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

@Composable
private fun SpecialtyCard(
    specialty: Specialty,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm + 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(dimens.spacingMd - 2.dp),
    ) {
        Icon(
            imageVector = Lucide.HeartPulse,
            contentDescription = null,
            tint = colors.navy,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = specialty.name,
            style = typography.body.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(dimens.spacing12))
                .background(if (selected) colors.navy else colors.border),
        ) {
            if (selected) {
                Icon(
                    imageVector = Lucide.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
