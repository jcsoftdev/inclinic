package com.inclinic.app.features.doctor.patients.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.UserSearch
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.presentation.component.SearchPatientComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.molecules.PatientRow
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPatientScreen(component: SearchPatientComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = { Text("Buscar paciente", style = typography.titleLarge, color = colors.text) },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Atrás", tint = colors.text)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.surface),
        )

        Column(
            Modifier.fillMaxSize().padding(dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Búsqueda por correo electrónico",
                style = typography.subtitle,
                color = colors.muted,
            )

            AppTextField(
                value = state.query,
                onValueChange = component::onQueryChange,
                label = "",
                placeholder = "juan.perez@correo.com",
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { component.onSearch() }),
                leadingIcon = {
                    Icon(Lucide.Mail, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            AppButton(
                text = "Buscar",
                onClick = component::onSearch,
                size = AppButtonSize.Lg,
                loading = state.isSearching,
                modifier = Modifier.fillMaxWidth(),
            )

            when {
                state.error != null -> Text(
                    text = state.error!!,
                    style = typography.body,
                    color = colors.red,
                )

                state.hasSearched && state.results.isEmpty() -> {
                    Text(
                        "NO SE ENCONTRÓ",
                        style = typography.label,
                        color = colors.light,
                    )
                    EmptyResults()
                }

                state.results.isNotEmpty() -> {
                    Text(
                        "RESULTADO",
                        style = typography.label,
                        color = colors.light,
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.results, key = { it.id }) { patient ->
                            ResultCard(patient = patient, onClick = { component.onPatientClicked(patient.id) })
                        }
                    }
                }

                else -> EmptyResults()
            }
        }
    }
}

@Composable
private fun ResultCard(patient: PatientListItem, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .clickable(onClick = onClick),
    ) {
        PatientRow(
            name = patient.name,
            lastVisit = patient.lastVisitDate ?: "Sin visitas previas",
            avatarUrl = patient.avatarUrl,
            onClick = onClick,
        )
    }
}

@Composable
private fun EmptyResults() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(18.dp),
    ) {
        Icon(
            Lucide.UserSearch,
            contentDescription = null,
            tint = colors.lav,
            modifier = Modifier.size(40.dp),
        )
        Text(
            "Busca pacientes por email para crear paquetes o invitarlos a tu clínica",
            style = typography.subtitle,
            color = colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}
