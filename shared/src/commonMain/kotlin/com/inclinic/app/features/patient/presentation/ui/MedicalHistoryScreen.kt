package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import androidx.compose.material3.IconButton
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.features.patient.presentation.component.MedicalHistoryComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(component: MedicalHistoryComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Historial",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    // Design connector B1VVk: "Historial · Perfil clínico"
                    IconButton(onClick = component::onNavigateToClinicalProfile) {
                        Icon(
                            Lucide.Activity,
                            contentDescription = "Perfil Clínico",
                            tint = colors.teal,
                            modifier = Modifier.size(20.dp),
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
        // El backend marca isLocked en cada record para pacientes FREE
        // (medical.service.ts stripForFreePatient). Si algún record viene
        // bloqueado, el paciente está en plan FREE -> mostramos el banner.
        val isFreePlan = state.records.any { it.isLocked }

        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = colors.navy,
                )
                else -> LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    if (isFreePlan) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            FreePlanBanner()
                            Spacer(Modifier.height(12.dp))
                        }
                    } else {
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                    item {
                        state.error?.let {
                            ErrorBanner(message = it, onDismiss = {})
                            Spacer(Modifier.height(8.dp))
                        }
                        if (state.records.isEmpty() && !state.isLoading) {
                            Box(
                                Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No medical records found", color = colors.muted)
                            }
                        }
                    }
                    items(state.records, key = { it.id }) { record ->
                        MedicalRecordCard(record)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FreePlanBanner() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.navy)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Plan FREE — Historial limitado",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Actualiza a PREMIUM para ver tu historial completo",
                color = colors.lavLight,
                fontSize = 11.sp,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colors.elevated)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("PREMIUM", color = colors.navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MedicalRecordCard(record: MedicalRecord) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Date header
        Text(record.createdAt.toString(), color = colors.muted, fontSize = 12.sp)

        // Doctor · specialty (cuando el backend lo incluye)
        val doctorLabel = listOfNotNull(record.doctorName, record.specialtyName)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (doctorLabel.isNotBlank()) {
            Text(doctorLabel, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        // Diagnosis + status chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                record.diagnosis,
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            if (!record.isLocked) {
                ChipStatus(label = "COMPLETO", kind = ChipStatusKind.Success)
            } else {
                ChipStatus(label = "BLOQUEADO", kind = ChipStatusKind.Error)
            }
        }

        if (record.isLocked) {
            // Estado bloqueado (plan FREE): backend nullea los campos sensibles.
            HorizontalDivider(color = colors.border)
            // Lock overlay row
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Lucide.Lock,
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Contenido bloqueado",
                    color = colors.muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                "Requiere plan PREMIUM para ver síntomas, vitales y recetas.",
                color = colors.text,
                fontSize = 13.sp,
            )
            // Ver con PREMIUM CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.navy)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Ver con PREMIUM ★",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            HorizontalDivider(color = colors.border)
            record.symptoms.let {
                Text("Síntomas: $it", color = colors.muted, fontSize = 12.sp)
            }
            record.treatment.let {
                Text("Tratamiento: $it", color = colors.muted, fontSize = 12.sp)
            }
            record.prescription?.let {
                Text("Receta: $it", color = colors.muted, fontSize = 12.sp)
            }
        }
    }
}
