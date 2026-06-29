package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.profile.application.GetMedicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.GetPatientProfileUseCase
import com.inclinic.app.features.patient.profile.application.UpdatePatientProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPatientProfileComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getProfile: GetPatientProfileUseCase,
    private val getMedicalProfile: GetMedicalProfileUseCase,
    private val updateProfile: UpdatePatientProfileUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PatientProfileComponent.Output) -> Unit,
) : PatientProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PatientProfileState())
    override val state: Value<PatientProfileState> = _state

    init {
        load()
        loadMedicalProfile()
    }

    override fun onNameChange(name: String) { _state.update { it.copy(name = name) } }
    override fun onPhoneChange(phone: String) { _state.update { it.copy(phone = phone) } }
    override fun onDateOfBirthChange(dob: String) { _state.update { it.copy(dateOfBirth = dob) } }
    override fun onBack() { onOutput(PatientProfileComponent.Output.Back) }
    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    override fun onToggleEdit() {
        _state.update { s ->
            if (s.isEditing) {
                val p = s.profile
                s.copy(
                    isEditing = false,
                    error = null,
                    name = p?.name ?: s.name,
                    phone = p?.phone ?: "",
                    dateOfBirth = p?.dateOfBirth ?: "",
                )
            } else {
                s.copy(isEditing = true, error = null)
            }
        }
    }

    override fun onSave() {
        val s = _state.value
        val original = s.profile ?: return
        val nameChanged = s.name != (original.name)
        val phoneChanged = s.phone != (original.phone ?: "")
        val dobChanged = s.dateOfBirth != (original.dateOfBirth ?: "")
        if (!nameChanged && !phoneChanged && !dobChanged) return

        _state.update { it.copy(isSaving = true, error = null) }
        scope.launch {
            updateProfile(
                patientId = patientId,
                name = s.name,
                phone = s.phone.takeIf { it.isNotBlank() },
                dateOfBirth = s.dateOfBirth.takeIf { it.isNotBlank() },
            )
                .onSuccess { updated ->
                    _state.update { it.copy(isSaving = false, isEditing = false, profile = updated) }
                    onOutput(PatientProfileComponent.Output.Saved)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSaving = false, error = err.toUserMessage("Save failed")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getProfile(patientId)
                .onSuccess { profile ->
                    _state.update { it.copy(
                        isLoading = false,
                        profile = profile,
                        name = profile.name,
                        phone = profile.phone ?: "",
                        dateOfBirth = profile.dateOfBirth ?: "",
                    ) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Failed to load profile")) }
                }
        }
    }

    private fun loadMedicalProfile() {
        scope.launch {
            getMedicalProfile(patientId)
                .onSuccess { medical ->
                    _state.update { it.copy(medicalProfile = medical) }
                }
        }
    }
}
