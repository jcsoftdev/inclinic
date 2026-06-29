package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.presentation.component.CreateMedicalRecordComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMedicalRecordScreen(component: CreateMedicalRecordComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    if (state.hasSavedDraft) {
        AlertDialog(
            onDismissRequest = component::onDiscardDraft,
            title = { Text("Borrador guardado") },
            text = { Text("Tienes un borrador guardado. ¿Recuperarlo?") },
            confirmButton = {
                Button(onClick = component::onRestoreDraft) { Text("Recuperar") }
            },
            dismissButton = {
                TextButton(onClick = component::onDiscardDraft) { Text("Descartar") }
            },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = { Text("Ficha clínica", style = typography.titleLarge, color = colors.text) },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Atrás", tint = colors.text)
                }
            },
            actions = {
                TextButton(onClick = component::onSubmit) {
                    Text("Guardar", style = typography.link, color = colors.navy)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.surface),
        )

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
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

        Box(Modifier.background(colors.sand).padding(horizontal = dimens.spacingMd).padding(bottom = 12.dp)) {
            AppButton(
                text = "Guardar ficha clínica",
                onClick = component::onSubmit,
                size = AppButtonSize.Lg,
                loading = state.isSubmitting,
                enabled = !state.isSubmitting && state.draft.diagnosis.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun MedicalRecordForm(
    diagnosis: String,
    onDiagnosisChange: (String) -> Unit,
    symptoms: String,
    onSymptomsChange: (String) -> Unit,
    treatment: String,
    onTreatmentChange: (String) -> Unit,
    prescription: String,
    onPrescriptionChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    FormSection(label = "DIAGNÓSTICO") {
        AppTextField(
            value = diagnosis,
            onValueChange = onDiagnosisChange,
            label = "",
            placeholder = "Diagnóstico principal",
            error = if (diagnosis.isBlank()) "Requerido" else null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    FormSection(label = "SÍNTOMAS") {
        AppTextField(
            value = symptoms,
            onValueChange = onSymptomsChange,
            label = "",
            placeholder = "Síntomas reportados",
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    FormSection(label = "TRATAMIENTO") {
        AppTextField(
            value = treatment,
            onValueChange = onTreatmentChange,
            label = "",
            placeholder = "Plan de tratamiento",
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    FormSection(label = "RECETA") {
        AppTextField(
            value = prescription,
            onValueChange = onPrescriptionChange,
            label = "",
            placeholder = "Medicamentos recetados",
            modifier = Modifier.fillMaxWidth(),
        )
    }
    FormSection(label = "NOTAS ADICIONALES") {
        AppTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = "",
            placeholder = "Notas sobre la consulta",
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FormSection(label: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(12.dp),
    ) {
        Text(label, style = typography.caption, color = colors.muted)
        content()
    }
}
