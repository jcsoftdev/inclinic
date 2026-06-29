package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.presentation.component.PriceConfigComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceConfigScreen(component: PriceConfigComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Precio y modalidad") },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.saveSuccess) {
                Text("Configuración guardada", color = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = state.price,
                onValueChange = component::onPriceChange,
                label = { Text("Precio de consulta (S/.)") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("S/.") },
                singleLine = true,
            )

            Text("Modalidades disponibles", style = MaterialTheme.typography.titleSmall)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.supportsPresential, onCheckedChange = { component.onPresentialToggle() })
                Text("Presencial")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.supportsVirtual, onCheckedChange = { component.onVirtualToggle() })
                Text("Virtual")
            }

            Button(
                onClick = component::onSave,
                enabled = !state.isSaving && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) CircularProgressIndicator()
                else Text("Guardar")
            }
        }
    }
}
