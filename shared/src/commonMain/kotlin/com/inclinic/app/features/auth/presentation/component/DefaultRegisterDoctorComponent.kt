package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.core.upload.UploadFileUseCase
import com.inclinic.app.features.auth.application.GetSpecialtiesUseCase
import com.inclinic.app.features.auth.application.RegisterFreelanceDoctorUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRegisterDoctorComponent(
    componentContext: ComponentContext,
    private val registerFreelanceUseCase: RegisterFreelanceDoctorUseCase,
    private val getSpecialtiesUseCase: GetSpecialtiesUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RegisterDoctorComponent.Output) -> Unit,
) : RegisterDoctorComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(RegisterDoctorState())
    override val state: Value<RegisterDoctorState> = _state

    // ── Step 1 ────────────────────────────────────────────────────────────────

    override fun onFirstNameChanged(value: String) {
        _state.update { it.copy(firstName = value, firstNameError = null) }
    }

    override fun onLastNameChanged(value: String) {
        _state.update { it.copy(lastName = value, lastNameError = null) }
    }

    override fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    override fun onPhoneChanged(value: String) {
        _state.update { it.copy(phone = value, phoneError = null) }
    }

    override fun onLicenseNumberChanged(value: String) {
        _state.update { it.copy(licenseNumber = value) }
    }

    // ── Step 2 ────────────────────────────────────────────────────────────────

    override fun onToggleSpecialty(specialtyId: String) {
        _state.update { s ->
            val updated = if (specialtyId in s.selectedSpecialtyIds)
                s.selectedSpecialtyIds - specialtyId
            else
                s.selectedSpecialtyIds + specialtyId
            val primary = if (s.primarySpecialtyId !in updated) null else s.primarySpecialtyId
            s.copy(selectedSpecialtyIds = updated, primarySpecialtyId = primary, specialtyError = null)
        }
    }

    override fun onPrimarySpecialtySelected(specialtyId: String) {
        _state.update { s ->
            val selected = s.selectedSpecialtyIds + specialtyId
            s.copy(selectedSpecialtyIds = selected, primarySpecialtyId = specialtyId, specialtyError = null)
        }
    }

    override fun onConsultationPriceChanged(value: String) {
        _state.update { it.copy(consultationPriceText = value, priceError = null) }
    }

    override fun onAppointmentModeChanged(mode: String) {
        _state.update { it.copy(appointmentMode = mode) }
    }

    override fun onAppointmentDurationChanged(value: String) {
        _state.update { it.copy(appointmentDurationText = value) }
    }

    override fun onOffersHomeVisitToggled(value: Boolean) {
        _state.update { it.copy(offersHomeVisit = value) }
    }

    // ── Step 3 ────────────────────────────────────────────────────────────────

    override fun onDocumentUploaded(url: String) {
        _state.update { it.copy(documentUrls = it.documentUrls + url, documentError = null) }
    }

    override fun onDocumentRemoved(url: String) {
        _state.update { it.copy(documentUrls = it.documentUrls - url) }
    }

    override fun onDocumentFilePicked(file: PickedFile) {
        if (_state.value.isDocumentUploading) return
        _state.update { it.copy(isDocumentUploading = true, documentUploadError = null) }
        scope.launch {
            uploadFileUseCase(
                bucket = DOCUMENTS_BUCKET,
                bytes = file.bytes,
                fileName = file.fileName,
                mimeType = file.mimeType,
            ).onSuccess { url ->
                _state.update {
                    it.copy(
                        isDocumentUploading = false,
                        documentUrls = it.documentUrls + url,
                        documentError = null,
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        isDocumentUploading = false,
                        documentUploadError = err.toUserMessage("Error al subir documento"),
                    )
                }
            }
        }
    }

    // ── Step 4 ────────────────────────────────────────────────────────────────

    override fun onScheduleAdded(schedule: FreelanceScheduleDto) {
        _state.update { it.copy(schedules = it.schedules + schedule, scheduleError = null) }
    }

    override fun onScheduleRemoved(index: Int) {
        _state.update { s ->
            s.copy(schedules = s.schedules.toMutableList().also { it.removeAt(index) })
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    override fun onNextStep() {
        when (_state.value.step) {
            RegisterDoctorState.Step.PersonalData    -> advanceFromPersonalData()
            RegisterDoctorState.Step.SpecialtyAndPrice -> advanceFromSpecialty()
            RegisterDoctorState.Step.Documents       -> advanceFromDocuments()
            RegisterDoctorState.Step.Schedules       -> _state.update { it.copy(step = RegisterDoctorState.Step.Review) }
            RegisterDoctorState.Step.Review          -> onSubmit()
        }
    }

    override fun onBack() {
        when (_state.value.step) {
            RegisterDoctorState.Step.PersonalData    -> onOutput(RegisterDoctorComponent.Output.Back)
            RegisterDoctorState.Step.SpecialtyAndPrice -> _state.update { it.copy(step = RegisterDoctorState.Step.PersonalData) }
            RegisterDoctorState.Step.Documents       -> _state.update { it.copy(step = RegisterDoctorState.Step.SpecialtyAndPrice) }
            RegisterDoctorState.Step.Schedules       -> _state.update { it.copy(step = RegisterDoctorState.Step.Documents) }
            RegisterDoctorState.Step.Review          -> _state.update { it.copy(step = RegisterDoctorState.Step.Schedules) }
        }
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.isLoading) return

        _state.update { it.copy(isLoading = true, serverError = null) }

        scope.launch {
            val params = RegisterFreelanceDoctorUseCase.Params(
                firstName = s.firstName,
                lastName = s.lastName,
                email = s.email,
                phone = s.phone,
                licenseNumber = s.licenseNumber.takeIf { it.isNotBlank() },
                documents = s.documentUrls,
                appointmentMode = s.appointmentMode,
                appointmentDuration = s.appointmentDurationText.toIntOrNull() ?: 30,
                specialtyIds = s.selectedSpecialtyIds.toList(),
                primarySpecialtyId = s.primarySpecialtyId ?: s.selectedSpecialtyIds.firstOrNull() ?: "",
                consultationPrice = s.consultationPriceText.toDoubleOrNull() ?: 0.0,
                offersHomeVisit = s.offersHomeVisit,
                schedules = s.schedules,
            )

            registerFreelanceUseCase(params)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onOutput(RegisterDoctorComponent.Output.Success(s.email))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            serverError = error as? AuthError ?: AuthError.Unknown(error),
                        )
                    }
                }
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun advanceFromPersonalData() {
        val s = _state.value
        val firstNameErr = if (s.firstName.trim().length < 2) "Mínimo 2 caracteres" else null
        val lastNameErr = if (s.lastName.trim().length < 2) "Mínimo 2 caracteres" else null
        val emailErr = if (!EMAIL_REGEX.matches(s.email)) "Email no válido" else null
        val phoneErr = if (s.phone.trim().length < 6) "Mínimo 6 dígitos" else null

        if (firstNameErr != null || lastNameErr != null || emailErr != null || phoneErr != null) {
            _state.update {
                it.copy(
                    firstNameError = firstNameErr,
                    lastNameError = lastNameErr,
                    emailError = emailErr,
                    phoneError = phoneErr,
                )
            }
            return
        }

        _state.update { it.copy(step = RegisterDoctorState.Step.SpecialtyAndPrice, specialtiesLoading = true) }
        loadSpecialties()
    }

    private fun advanceFromSpecialty() {
        val s = _state.value
        val specialtyErr = if (s.selectedSpecialtyIds.isEmpty()) "Selecciona al menos una especialidad" else null
        val priceVal = s.consultationPriceText.toDoubleOrNull()
        val priceErr = when {
            priceVal == null -> "Ingresa un precio válido"
            priceVal < 50.0  -> "El precio mínimo es S/. 50"
            else             -> null
        }
        if (specialtyErr != null || priceErr != null) {
            _state.update { it.copy(specialtyError = specialtyErr, priceError = priceErr) }
            return
        }
        _state.update { it.copy(step = RegisterDoctorState.Step.Documents) }
    }

    private fun advanceFromDocuments() {
        val s = _state.value
        if (s.documentUrls.isEmpty()) {
            _state.update { it.copy(documentError = "Sube al menos un documento") }
            return
        }
        _state.update { it.copy(step = RegisterDoctorState.Step.Schedules) }
    }

    private fun loadSpecialties() {
        scope.launch {
            getSpecialtiesUseCase()
                .onSuccess { specialties ->
                    _state.update { it.copy(specialties = specialties, specialtiesLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(specialtiesLoading = false) }
                }
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        const val DOCUMENTS_BUCKET = "documents"
    }
}
