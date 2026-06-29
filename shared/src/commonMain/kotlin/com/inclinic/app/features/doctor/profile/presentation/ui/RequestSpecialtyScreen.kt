package com.inclinic.app.features.doctor.profile.presentation.ui

import androidx.compose.foundation.background
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.molecules.DocUploadState
import com.inclinic.app.ui.molecules.DocumentUploader
import com.inclinic.app.ui.theme.AppTheme

/**
 * Solicitar Especialidad screen (zSMeU).
 *
 * Layout (Pencil):
 *   Header (back + "Solicitar especialidad")
 *   amber warning banner (3-strike policy)
 *   "Especialidad" label + field
 *   "Documentos de respaldo" label + 2 document uploaders
 *   "Comentario para admin" label + text area
 *   spacer + "Enviar solicitud" CTA
 */
@Composable
fun RequestSpecialtyScreen(
    component: RequestSpecialtyComponent,
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
                        text = "Solicitar especialidad",
                        style = typography.titleLarge,
                        color = colors.text,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing12 + 2.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(dimens.spacingMd),
                ) {
                    ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm + 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.radiusLarge))
                            .background(colors.amberBg)
                            .padding(dimens.spacingMd - 2.dp),
                    ) {
                        Icon(
                            imageVector = Lucide.TriangleAlert,
                            contentDescription = null,
                            tint = colors.amber,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Al 3.º rechazo quedarás bloqueado permanentemente",
                            color = colors.amber,
                            style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    AppTextField(
                        value = state.specialtyName,
                        onValueChange = component::onSpecialtyNameChange,
                        label = "Especialidad",
                        placeholder = "Ej. Cardiología Intervencionista",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Documentos de respaldo",
                        style = typography.subtitle.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.text,
                    )

                    DocumentUploader(
                        label = "Certificación SUNEDU",
                        hint = "PDF · máx 5MB",
                        state = state.documentUrls.getOrNull(0)
                            ?.let { DocUploadState.Done(it) } ?: DocUploadState.Empty,
                        onPickClick = { },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    DocumentUploader(
                        label = "Diploma",
                        hint = "PDF · máx 5MB",
                        state = state.documentUrls.getOrNull(1)
                            ?.let { DocUploadState.Done(it) } ?: DocUploadState.Empty,
                        onPickClick = { },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    AppTextField(
                        value = state.comment,
                        onValueChange = component::onCommentChange,
                        label = "Comentario para admin",
                        placeholder = "Explica por qué solicitas esta especialidad...",
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    AppButton(
                        text = "Enviar solicitud",
                        onClick = component::onSubmit,
                        size = AppButtonSize.Lg,
                        loading = state.isSubmitting,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            LoadingOverlay(visible = state.isSubmitting)
        }
    }
}
