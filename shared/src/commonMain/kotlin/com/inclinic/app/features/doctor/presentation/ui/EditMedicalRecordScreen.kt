package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.presentation.component.EditMedicalRecordComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicalRecordScreen(component: EditMedicalRecordComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = { Text("Editar ficha clínica", style = typography.titleLarge, color = colors.text) },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ChevronLeft, contentDescription = "Atrás", tint = colors.navy)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.surface),
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            return@Column
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.error?.let { Text(it, style = typography.body, color = colors.red) }

            MedicalRecordForm(
                diagnosis = state.draft.diagnosis,
                onDiagnosisChange = component::onDiagnosisChange,
                symptoms = state.draft.symptoms,
                onSymptomsChange = component::onSymptomsChange,
                treatment = state.draft.treatment,
                onTreatmentChange = component::onTreatmentChange,
                prescription = state.draft.prescription,
                onPrescriptionChange = component::onPrescriptionChange,
                notes = state.draft.notes,
                onNotesChange = component::onNotesChange,
            )
        }

        Row(
            Modifier.background(colors.sand).fillMaxWidth().padding(start = dimens.spacingMd, end = dimens.spacingMd, top = 10.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            AppButton(
                text = "Cancelar",
                onClick = component::onBack,
                variant = AppButtonVariant.Outline,
                size = AppButtonSize.Lg,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = "Guardar cambios",
                onClick = component::onSubmit,
                size = AppButtonSize.Lg,
                loading = state.isSubmitting,
                enabled = !state.isSubmitting && state.draft.diagnosis.isNotBlank(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
