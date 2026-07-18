package com.inclinic.app.features.doctor.prescriptions.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.inclinic.app.features.doctor.prescriptions.presentation.component.CreatePrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.MedicationItemDraft
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun CreatePrescriptionScreen(
    component: CreatePrescriptionComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Header: back + "Emitir Receta"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Atras")
            Text(
                text = "Emitir Receta",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacingSm),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
        ) {
            // Field: diagnosis
            AppTextField(
                value = state.diagnosis,
                onValueChange = component::onDiagnosisChange,
                label = "Diagnostico",
                placeholder = "Diagnostico del paciente...",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            )

            // Medication items list - one card per item
            state.medicationItems.forEachIndexed { index, item ->
                MedicationItemCard(
                    index = index,
                    item = item,
                    totalItems = state.medicationItems.size,
                    onNameChange = { v -> component.onUpdateItemName(index, v) },
                    onDoseChange = { v -> component.onUpdateItemDose(index, v) },
                    onFrequencyChange = { v -> component.onUpdateItemFrequency(index, v) },
                    onDurationChange = { v -> component.onUpdateItemDuration(index, v) },
                    onNotesChange = { v -> component.onUpdateItemNotes(index, v) },
                    onRemove = { component.onRemoveItem(index) },
                )
            }

            // Add medication button
            AppButton(
                text = "Agregar medicamento",
                onClick = component::onAddItem,
                variant = AppButtonVariant.Outline,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )

            // Field: instructions textarea (shared across all items)
            AppTextField(
                value = state.instructions,
                onValueChange = component::onInstructionsChange,
                label = "Indicaciones para el paciente",
                placeholder = "Indicaciones para el paciente...",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            )

            ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())
        }

        // Bottom action bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            AppButton(
                text = if (state.isSubmitting) "Emitiendo..." else "Emitir Receta",
                onClick = component::onSubmit,
                loading = state.isSubmitting,
                enabled = !state.isSubmitting,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Cancelar",
                onClick = component::onBack,
                size = AppButtonSize.Lg,
                variant = AppButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MedicationItemCard(
    index: Int,
    item: MedicationItemDraft,
    totalItems: Int,
    onNameChange: (String) -> Unit,
    onDoseChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusMd))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusMd))
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        // Card header: "Medicamento N" + remove button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Medicamento ${index + 1}",
                style = typography.subtitle,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                fontSize = 13.sp,
            )
            if (totalItems > 1) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Lucide.Minus,
                        contentDescription = "Eliminar medicamento",
                        tint = colors.red,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Field: medication name
        AppTextField(
            value = item.name,
            onValueChange = onNameChange,
            label = "Medicamento",
            placeholder = "Losartan 50 mg",
            error = item.nameError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Row: dose + frequency
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppTextField(
                value = item.dose,
                onValueChange = onDoseChange,
                label = "Dosis",
                placeholder = "1 comprimido",
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            AppTextField(
                value = item.frequency,
                onValueChange = onFrequencyChange,
                label = "Frecuencia",
                placeholder = "Cada 12h",
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        // Field: duration
        AppTextField(
            value = item.duration,
            onValueChange = onDurationChange,
            label = "Duracion",
            placeholder = "30 dias",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Field: notes for this item
        AppTextField(
            value = item.notes,
            onValueChange = onNotesChange,
            label = "Notas del medicamento",
            placeholder = "Ej: tomar con comida...",
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        )
    }
}
