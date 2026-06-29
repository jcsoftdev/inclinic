package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Sparkles
import com.inclinic.app.features.patient.presentation.component.SymptomInputComponent
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SymptomInputScreen(component: SymptomInputComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "IA Médica",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.navy)
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.text,
                ),
            )
        },
        containerColor = colors.sand,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.sand)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
        ) {
            // Hero intro card — "Asistente IA" with navy bg
            AssistantHeroCard()

            // Section label + textarea
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                Text(
                    text = "Describe tus síntomas",
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )

                OutlinedTextField(
                    value = state.symptomText,
                    onValueChange = component::onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text(
                            text = "Ej: Tengo dolor de cabeza fuerte hace 3 días, con fiebre leve y cansancio general...",
                            color = colors.muted,
                            fontSize = 13.sp,
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.navy,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        cursorColor = colors.navy,
                    ),
                    shape = RoundedCornerShape(dimens.radiusMd),
                )
            }

            // Common symptom chips
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                Text(
                    text = "Síntomas frecuentes",
                    color = colors.muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                ) {
                    val commonSymptoms = listOf(
                        "Fiebre",
                        "Dolor cabeza",
                        "Tos",
                        "Náuseas",
                        "Dolor asfixia",
                    )

                    commonSymptoms.forEach { chip ->
                        val isSelected = state.selectedChips.contains(chip)
                        SymptomChip(
                            text = chip,
                            isSelected = isSelected,
                            onClick = { component.onChipToggle(chip) },
                        )
                    }
                }
            }

            // Buscar Especialistas button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radiusMd))
                    .background(colors.navy)
                    .clickable(enabled = !state.isSearching) { component.onSearch() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isSearching) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                    ) {
                        Icon(
                            imageVector = Lucide.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Buscar Especialistas",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantHeroCard() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.navy)
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Icon(Lucide.Sparkles, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text("Asistente IA", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Cuéntame qué sientes y encuentro al especialista indicado para ti",
            color = colors.navyTint,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun SymptomChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimens.radiusPill))
            .border(
                width = 1.dp,
                color = if (isSelected) colors.navy else colors.border,
                shape = RoundedCornerShape(dimens.radiusPill),
            )
            .background(if (isSelected) colors.navyTint else colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = dimens.spacingSm),
    ) {
        Text(
            text = text,
            color = if (isSelected) colors.navy else colors.text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
