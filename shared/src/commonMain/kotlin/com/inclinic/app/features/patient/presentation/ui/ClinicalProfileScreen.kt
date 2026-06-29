package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.ShieldAlert
import com.inclinic.app.features.patient.presentation.component.ClinicalProfileComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClinicalProfileScreen(
    component: ClinicalProfileComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perfil Clínico",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    IconButton(onClick = component::onToggleEdit) {
                        Icon(
                            imageVector = if (state.isEditing) Lucide.Check else Lucide.Pencil,
                            contentDescription = if (state.isEditing) "Cancelar edición" else "Editar perfil clínico",
                            tint = colors.navy,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.sand),
                windowInsets = WindowInsets(0),
            )
        },
        containerColor = colors.sand,
        modifier = modifier,
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = colors.navy) }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.error?.let {
                    ErrorBanner(message = it, onDismiss = component::onDismissError)
                }

                // ── Información Básica ────────────────────────────────────────
                ClinicalSection(
                    icon = Lucide.Activity,
                    iconTint = colors.teal,
                    title = "Información Básica",
                ) {
                    if (state.isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = state.draftBloodType,
                                onValueChange = component::onBloodTypeChange,
                                label = { Text("Tipo de sangre") },
                                placeholder = { Text("A+, O-, etc.") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = state.draftHeightCm,
                                onValueChange = component::onHeightCmChange,
                                label = { Text("Altura (cm)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = state.draftWeightKg,
                                onValueChange = component::onWeightKgChange,
                                label = { Text("Peso (kg)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BasicField(
                                label = "TIPO DE SANGRE",
                                value = state.bloodType ?: "—",
                                modifier = Modifier.weight(1f),
                            )
                            BasicField(
                                label = "ALTURA (cm)",
                                value = state.heightCm?.let { it.toInt().toString() } ?: "—",
                                modifier = Modifier.weight(1f),
                            )
                            BasicField(
                                label = "PESO (kg)",
                                value = state.weightKg?.let { it.toInt().toString() } ?: "—",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // ── Alergias y Condiciones ────────────────────────────────────
                ClinicalSection(
                    icon = Lucide.ShieldAlert,
                    iconTint = colors.red,
                    title = "Alergias y Condiciones",
                ) {
                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.draftAllergies,
                            onValueChange = component::onAllergiesChange,
                            label = { Text("Alergias") },
                            placeholder = { Text("Penicilina, mariscos, ...") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.draftConditions,
                            onValueChange = component::onConditionsChange,
                            label = { Text("Condiciones crónicas") },
                            placeholder = { Text("Diabetes, hipertensión, ...") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        if (state.allergies.isNotEmpty()) {
                            Text(
                                text = "ALERGIAS",
                                color = colors.navy,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.allergies.forEach { AllergyTag(it) }
                            }
                        }
                        if (state.conditions.isNotEmpty()) {
                            Text(
                                text = "CONDICIONES CRÓNICAS",
                                color = colors.navy,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.conditions.forEach { ConditionTag(it) }
                            }
                        }
                        if (state.allergies.isEmpty() && state.conditions.isEmpty()) {
                            Text("Sin alergias ni condiciones registradas", color = colors.muted, fontSize = 13.sp)
                        }
                    }
                }

                // ── Contacto de Emergencia ────────────────────────────────────
                ClinicalSection(
                    icon = Lucide.Phone,
                    iconTint = colors.blue,
                    title = "Contacto de Emergencia",
                ) {
                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.draftEmergencyName,
                            onValueChange = component::onEmergencyNameChange,
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = state.draftEmergencyPhone,
                                onValueChange = component::onEmergencyPhoneChange,
                                label = { Text("Teléfono") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = state.draftEmergencyRelation,
                                onValueChange = component::onEmergencyRelationChange,
                                label = { Text("Relación") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        LabeledField(label = "NOMBRE", value = state.emergencyContactName ?: "—")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LabeledFieldColumn(
                                label = "TELÉFONO",
                                value = state.emergencyContactPhone ?: "—",
                                modifier = Modifier.weight(1f),
                            )
                            LabeledFieldColumn(
                                label = "RELACIÓN",
                                value = state.emergencyContactRelation ?: "—",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // ── Save button (edit mode only) ──────────────────────────────
                if (state.isEditing) {
                    Button(
                        onClick = component::onSave,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.navy),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Guardar cambios")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicalSection(
    icon: ImageVector?,
    iconTint: Color,
    title: String,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.elevated)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon?.let {
                Icon(it, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Text(title, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
private fun BasicField(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.sand)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LabeledField(label: String, value: String) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = colors.navy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.sand)
                .border(1.dp, colors.border, shape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(value, color = colors.text, fontSize = 14.sp)
        }
    }
}

@Composable
private fun LabeledFieldColumn(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = colors.text, fontSize = 13.sp)
    }
}

@Composable
private fun AllergyTag(label: String) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.redBg)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(label, color = colors.red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ConditionTag(label: String) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.amberBg)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(label, color = colors.amber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
