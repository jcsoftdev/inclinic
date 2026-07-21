package com.inclinic.app.features.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.inclinic.app.core.platform.rememberFilePicker
import com.inclinic.app.features.auth.core.error.toUserMessage
import com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto
import com.inclinic.app.features.auth.presentation.component.RegisterDoctorComponent
import com.inclinic.app.features.auth.presentation.component.RegisterDoctorState
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ConfirmDialog
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RegisterDoctorScreen(
    component: RegisterDoctorComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()

        Box(modifier = modifier.fillMaxSize()) {
            when (state.step) {
                RegisterDoctorState.Step.PersonalData    -> PersonalDataStep(state, component)
                RegisterDoctorState.Step.SpecialtyAndPrice -> SpecialtyAndPriceStep(state, component)
                RegisterDoctorState.Step.Documents       -> DocumentsStep(state, component)
                RegisterDoctorState.Step.Schedules       -> SchedulesStep(state, component)
                RegisterDoctorState.Step.Review          -> ReviewStep(state, component)
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

// ── Step progress header ──────────────────────────────────────────────────────

@Composable
private fun StepHeader(
    currentStep: Int,
    stepTitle: String,
    stepSubtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography
    val totalSteps = 5

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
        ) {
            AppBackButton(onClick = onBack)
            Text(
                text  = "Crear cuenta",
                style = typography.displayNano,
                color = colors.text,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text  = "Paso $currentStep de $totalSteps",
                style = typography.caption,
                color = colors.navy,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(totalSteps) { idx ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (idx < currentStep) colors.navy else colors.border),
                    )
                }
            }
            Text(text = stepTitle, style = typography.titleLarge, color = colors.text)
            Text(text = stepSubtitle, style = typography.subtitle, color = colors.muted)
        }
    }
}

// ── Step 1: Datos personales ──────────────────────────────────────────────────

@Composable
private fun PersonalDataStep(
    state: RegisterDoctorState,
    component: RegisterDoctorComponent,
) {
    var showDiscardConfirm by remember { mutableStateOf(false) }

    if (showDiscardConfirm) {
        ConfirmDialog(
            title = "¿Descartar registro?",
            message = "Perderás los datos ingresados en este formulario. Esta acción no se puede deshacer.",
            confirmText = "Descartar",
            onConfirm = {
                showDiscardConfirm = false
                component.onBack()
            },
            onDismiss = { showDiscardConfirm = false },
        )
    }

    AuthScaffold {
        StepHeader(
            currentStep = 1,
            stepTitle = "Datos personales",
            stepSubtitle = "Empieza con tu información básica",
            onBack = { showDiscardConfirm = true },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            val bannerMessage = state.serverError?.toUserMessage()
            if (bannerMessage != null) {
                ErrorBanner(message = bannerMessage, modifier = Modifier.fillMaxWidth())
            }

            AppTextField(
                value           = state.firstName,
                onValueChange   = component::onFirstNameChanged,
                label           = "Nombres",
                placeholder     = "Patricia",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                error           = state.firstNameError,
                enabled         = !state.isLoading,
                modifier        = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value           = state.lastName,
                onValueChange   = component::onLastNameChanged,
                label           = "Apellidos",
                placeholder     = "Huamán Ríos",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                error           = state.lastNameError,
                enabled         = !state.isLoading,
                modifier        = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value         = state.email,
                onValueChange = component::onEmailChanged,
                label         = "Correo electrónico",
                placeholder   = "patricia.huaman@gmail.com",
                leadingIcon   = {
                    Icon(
                        imageVector        = Lucide.Mail,
                        contentDescription = null,
                        tint               = AppTheme.colors.muted,
                        modifier           = Modifier.size(16.dp),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next,
                ),
                error   = state.emailError,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value         = state.phone,
                onValueChange = component::onPhoneChanged,
                label         = "Celular",
                placeholder   = "987 654 321",
                leadingIcon   = {
                    Text(
                        text  = "+51",
                        style = AppTheme.typography.body.copy(
                            color      = AppTheme.colors.navy,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction    = ImeAction.Next,
                ),
                error   = state.phoneError,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value           = state.licenseNumber,
                onValueChange   = component::onLicenseNumberChanged,
                label           = "N° CMP / Licencia (opcional)",
                placeholder     = "CMP-12345",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                enabled         = !state.isLoading,
                modifier        = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            AppButton(
                text     = "Continuar",
                onClick  = component::onNextStep,
                loading  = state.isLoading,
                enabled  = !state.isLoading,
                size     = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Step 2: Especialidad + precio ─────────────────────────────────────────────

@Composable
private fun SpecialtyAndPriceStep(
    state: RegisterDoctorState,
    component: RegisterDoctorComponent,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    AuthScaffold {
        StepHeader(
            currentStep = 2,
            stepTitle = "Especialidad y precio",
            stepSubtitle = "Elige tus especialidades y tarifa de consulta",
            onBack = component::onBack,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (state.specialtyError != null) {
                ErrorBanner(message = state.specialtyError, modifier = Modifier.fillMaxWidth())
            }

            Text(
                text  = "Especialidades",
                style = typography.label,
                color = colors.text,
            )

            if (state.specialtiesLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                ) {
                    CircularProgressIndicator(color = colors.navy)
                }
            } else {
                state.specialties.forEach { specialty ->
                    val isSelected = specialty.id in state.selectedSpecialtyIds
                    val isPrimary  = specialty.id == state.primarySpecialtyId

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPrimary) colors.navy else if (isSelected) colors.surface else colors.surface)
                            .border(
                                1.dp,
                                if (isPrimary) colors.navy else if (isSelected) colors.navy else colors.border,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = { component.onToggleSpecialty(specialty.id) },
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Column {
                            Text(
                                text  = specialty.name,
                                style = typography.body.copy(
                                    fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = if (isPrimary) Color.White else colors.text,
                            )
                            if (isSelected && !isPrimary) {
                                Text(
                                    text  = "Marcar como principal",
                                    style = typography.caption,
                                    color = colors.navy,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { component.onPrimarySpecialtySelected(specialty.id) },
                                    ),
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector        = Lucide.CircleCheck,
                                contentDescription = null,
                                tint               = if (isPrimary) Color.White else colors.navy,
                                modifier           = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.priceError != null) {
                ErrorBanner(message = state.priceError, modifier = Modifier.fillMaxWidth())
            }

            AppTextField(
                value         = state.consultationPriceText,
                onValueChange = component::onConsultationPriceChanged,
                label         = "Tarifa de consulta (S/.)",
                placeholder   = "80",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction    = ImeAction.Done,
                ),
                error   = null,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            // Appointment mode toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("BY_SCHEDULE" to "Por citas", "BY_TURN" to "Por turno").forEach { (mode, label) ->
                    val selected = state.appointmentMode == mode
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) colors.navy else colors.surface)
                            .border(1.dp, if (selected) colors.navy else colors.border, RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { component.onAppointmentModeChanged(mode) },
                            )
                            .padding(vertical = 10.dp),
                    ) {
                        Text(
                            text  = label,
                            style = typography.body,
                            color = if (selected) Color.White else colors.text,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            AppButton(
                text     = "Continuar",
                onClick  = component::onNextStep,
                loading  = state.isLoading,
                enabled  = !state.isLoading,
                size     = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Step 3: Documentos ────────────────────────────────────────────────────────

@Composable
private fun DocumentsStep(
    state: RegisterDoctorState,
    component: RegisterDoctorComponent,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography
    val docPicker  = rememberFilePicker { file -> if (file != null) component.onDocumentFilePicked(file) }

    AuthScaffold {
        StepHeader(
            currentStep = 3,
            stepTitle = "Documentos",
            stepSubtitle = "Sube tu título, CMP u otros documentos profesionales",
            onBack = component::onBack,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (state.documentError != null) {
                ErrorBanner(message = state.documentError, modifier = Modifier.fillMaxWidth())
            }

            state.documentUrls.forEachIndexed { idx, url ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text     = "Documento ${idx + 1}",
                        style    = typography.body,
                        color    = colors.text,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector        = Lucide.Trash2,
                        contentDescription = "Eliminar",
                        tint               = colors.error,
                        modifier           = Modifier.size(18.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { component.onDocumentRemoved(url) },
                        ),
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { docPicker.launch() },
                    )
                    .padding(vertical = 16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector        = Lucide.Plus,
                        contentDescription = null,
                        tint               = colors.navy,
                        modifier           = Modifier.size(18.dp),
                    )
                    Text(
                        text  = "Agregar documento",
                        style = typography.body,
                        color = colors.navy,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            AppButton(
                text     = "Continuar",
                onClick  = component::onNextStep,
                loading  = state.isLoading,
                enabled  = !state.isLoading && state.documentUrls.isNotEmpty(),
                size     = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Step 4: Horarios ──────────────────────────────────────────────────────────

@Composable
private fun SchedulesStep(
    state: RegisterDoctorState,
    component: RegisterDoctorComponent,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    // Local state for building a new schedule entry
    var selectedDay   by remember { mutableStateOf("MONDAY") }
    var startTime     by remember { mutableStateOf("09:00") }
    var endTime       by remember { mutableStateOf("17:00") }

    val days = listOf(
        "MONDAY" to "Lun",
        "TUESDAY" to "Mar",
        "WEDNESDAY" to "Mié",
        "THURSDAY" to "Jue",
        "FRIDAY" to "Vie",
        "SATURDAY" to "Sáb",
        "SUNDAY" to "Dom",
    )

    AuthScaffold {
        StepHeader(
            currentStep = 4,
            stepTitle = "Horarios",
            stepSubtitle = "Agrega tu disponibilidad semanal",
            onBack = component::onBack,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (state.scheduleError != null) {
                ErrorBanner(message = state.scheduleError, modifier = Modifier.fillMaxWidth())
            }

            // Existing schedules
            state.schedules.forEachIndexed { idx, schedule ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text  = "${days.find { it.first == schedule.dayOfWeek }?.second ?: schedule.dayOfWeek}  ${schedule.startTime}–${schedule.endTime}",
                        style = typography.body,
                        color = colors.text,
                    )
                    Icon(
                        imageVector        = Lucide.Trash2,
                        contentDescription = "Eliminar",
                        tint               = colors.error,
                        modifier           = Modifier.size(18.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { component.onScheduleRemoved(idx) },
                        ),
                    )
                }
            }

            // Day selector
            Text(text = "Día", style = typography.label, color = colors.text)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                days.forEach { (key, label) ->
                    val selected = selectedDay == key
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) colors.navy else colors.surface)
                            .border(1.dp, if (selected) colors.navy else colors.border, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selectedDay = key },
                            )
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            text  = label,
                            style = typography.caption,
                            color = if (selected) Color.White else colors.text,
                        )
                    }
                }
            }

            // Time inputs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppTextField(
                    value         = startTime,
                    onValueChange = { startTime = it },
                    label         = "Inicio",
                    placeholder   = "09:00",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier      = Modifier.weight(1f),
                )
                AppTextField(
                    value         = endTime,
                    onValueChange = { endTime = it },
                    label         = "Fin",
                    placeholder   = "17:00",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier      = Modifier.weight(1f),
                )
            }

            AppButton(
                text    = "Agregar horario",
                onClick = {
                    component.onScheduleAdded(
                        FreelanceScheduleDto(
                            dayOfWeek = selectedDay,
                            startTime = startTime,
                            endTime   = endTime,
                        )
                    )
                },
                enabled  = startTime.isNotBlank() && endTime.isNotBlank(),
                size     = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            AppButton(
                text     = "Continuar",
                onClick  = component::onNextStep,
                loading  = state.isLoading,
                enabled  = !state.isLoading && state.schedules.isNotEmpty(),
                size     = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Step 5: Revisar y enviar ──────────────────────────────────────────────────

@Composable
private fun ReviewStep(
    state: RegisterDoctorState,
    component: RegisterDoctorComponent,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    AuthScaffold {
        StepHeader(
            currentStep = 5,
            stepTitle = "Revisar solicitud",
            stepSubtitle = "Revisa tu información antes de enviar",
            onBack = component::onBack,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            val bannerMessage = state.serverError?.toUserMessage()
            if (bannerMessage != null) {
                ErrorBanner(message = bannerMessage, modifier = Modifier.fillMaxWidth())
            }

            ReviewSection(title = "Datos personales") {
                ReviewRow("Nombre", "${state.firstName} ${state.lastName}")
                ReviewRow("Email", state.email)
                ReviewRow("Teléfono", state.phone)
                if (state.licenseNumber.isNotBlank()) ReviewRow("CMP", state.licenseNumber)
            }

            ReviewSection(title = "Especialidades y precio") {
                ReviewRow("Especialidades", "${state.selectedSpecialtyIds.size} seleccionadas")
                ReviewRow("Tarifa", "S/. ${state.consultationPriceText}")
                ReviewRow("Modalidad", if (state.appointmentMode == "BY_SCHEDULE") "Por citas" else "Por turno")
            }

            ReviewSection(title = "Documentos") {
                ReviewRow("Documentos subidos", "${state.documentUrls.size}")
            }

            ReviewSection(title = "Horarios") {
                ReviewRow("Horarios configurados", "${state.schedules.size}")
            }

            Text(
                text  = "Al enviar tu solicitud, recibirás un correo de confirmación. Nuestro equipo revisará tu perfil en 24-48 horas.",
                style = typography.caption,
                color = colors.muted,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            AppButton(
                text     = "Enviar solicitud",
                onClick  = component::onSubmit,
                loading  = state.isLoading,
                enabled  = !state.isLoading && state.canSubmit,
                size     = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text(
            text  = title,
            style = typography.label,
            color = colors.text,
        )
        content()
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text  = label,
            style = AppTheme.typography.body,
            color = AppTheme.colors.muted,
        )
        Text(
            text  = value,
            style = AppTheme.typography.body,
            color = AppTheme.colors.text,
        )
    }
}
