package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import com.inclinic.app.core.util.currentYearUtc
import com.inclinic.app.core.util.epochMillisToIsoDate
import com.inclinic.app.core.util.formatBirthDate
import com.inclinic.app.core.util.isoDateToEpochMillis
import com.inclinic.app.core.util.nowEpochMillis
import com.inclinic.app.features.patient.presentation.component.PatientProfileComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    component: PatientProfileComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.sand),
                windowInsets = WindowInsets(0),
            )
        },
        containerColor = colors.sand,
        modifier = modifier,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = colors.navy,
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.error?.let {
                        ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
                    }

                    AvatarCard(name = state.profile?.name.orEmpty().ifBlank { state.name })

                    PersonalDataCard(
                        component = component,
                        name = state.name,
                        email = state.profile?.email.orEmpty(),
                        phone = state.phone,
                        dateOfBirth = state.dateOfBirth,
                    )

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
private fun AvatarCard(name: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(colors.navy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.UserRound,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(name, color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Cambiar foto", color = colors.teal, fontSize = 13.sp)
    }
}

@Composable
private fun PersonalDataCard(
    component: PatientProfileComponent,
    name: String,
    email: String,
    phone: String,
    dateOfBirth: String,
) {
    ProfileCard {
        CardSectionHeader("DATOS PERSONALES")
        CardDivider()
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = component::onNameChange,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email") },
                singleLine = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = component::onPhoneChange,
                label = { Text("Teléfono") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            BirthDateField(
                isoValue = dateOfBirth,
                onChange = component::onDateOfBirthChange,
            )
        }
    }
}

/**
 * Campo de fecha de nacimiento: muestra la fecha formateada y abre un DatePicker
 * al tocarlo (no se escribe a mano). Guarda el valor como "AAAA-MM-DD".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateField(
    isoValue: String,
    onChange: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    LaunchedEffect(pressed) { if (pressed) showPicker = true }

    val display = formatBirthDate(isoValue).let { if (it == "—") "" else it }

    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text("Fecha de nacimiento") },
        placeholder = { Text("Selecciona una fecha") },
        trailingIcon = { Icon(Lucide.Calendar, contentDescription = "Elegir fecha") },
        singleLine = true,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth(),
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = isoDateToEpochMillis(isoValue),
            yearRange = 1900..currentYearUtc(),
            selectableDates = NoFutureSelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onChange(epochMillisToIsoDate(it)) }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private object NoFutureSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= nowEpochMillis()
}

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface),
    ) {
        content()
    }
}

@Composable
private fun CardSectionHeader(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun CardDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.border),
    )
}
