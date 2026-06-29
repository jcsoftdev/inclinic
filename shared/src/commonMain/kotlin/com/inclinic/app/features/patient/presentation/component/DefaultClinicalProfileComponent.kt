package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.EmergencyContact
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.features.patient.profile.application.GetMedicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.UpdateClinicalProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultClinicalProfileComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getProfile: GetMedicalProfileUseCase,
    private val updateProfile: UpdateClinicalProfileUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ClinicalProfileComponent.Output) -> Unit,
) : ClinicalProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ClinicalProfileState())
    override val state: Value<ClinicalProfileState> = _state

    init { load() }

    override fun onBack() { onOutput(ClinicalProfileComponent.Output.Back) }

    override fun onToggleEdit() {
        val s = _state.value
        if (s.isEditing) {
            // Cancel: revert drafts to loaded values
            _state.update { it.resetDraftsToLoaded() }
        } else {
            // Enter edit mode: populate drafts from current state
            _state.update { it.copy(
                isEditing = true,
                draftBloodType = it.bloodType ?: "",
                draftHeightCm = it.heightCm?.let { h -> h.toInt().toString() } ?: "",
                draftWeightKg = it.weightKg?.let { w -> w.toInt().toString() } ?: "",
                draftAllergies = it.allergies.joinToString(", "),
                draftConditions = it.conditions.joinToString(", "),
                draftEmergencyName = it.emergencyContactName ?: "",
                draftEmergencyPhone = it.emergencyContactPhone ?: "",
                draftEmergencyRelation = it.emergencyContactRelation ?: "",
            ) }
        }
    }

    override fun onBloodTypeChange(value: String) { _state.update { it.copy(draftBloodType = value) } }
    override fun onHeightCmChange(value: String) { _state.update { it.copy(draftHeightCm = value) } }
    override fun onWeightKgChange(value: String) { _state.update { it.copy(draftWeightKg = value) } }
    override fun onAllergiesChange(value: String) { _state.update { it.copy(draftAllergies = value) } }
    override fun onConditionsChange(value: String) { _state.update { it.copy(draftConditions = value) } }
    override fun onEmergencyNameChange(value: String) { _state.update { it.copy(draftEmergencyName = value) } }
    override fun onEmergencyPhoneChange(value: String) { _state.update { it.copy(draftEmergencyPhone = value) } }
    override fun onEmergencyRelationChange(value: String) { _state.update { it.copy(draftEmergencyRelation = value) } }

    override fun onDismissError() { _state.update { it.copy(error = null) } }

    override fun onSave() {
        if (_state.value.isSaving) return
        val s = _state.value
        val profile = MedicalProfile(
            bloodType = s.draftBloodType.trim().takeIf { it.isNotBlank() },
            heightCm = s.draftHeightCm.trim().toFloatOrNull(),
            weightKg = s.draftWeightKg.trim().toFloatOrNull(),
            allergies = s.draftAllergies.split(",").map { it.trim() }.filter { it.isNotBlank() },
            chronicConditions = s.draftConditions.split(",").map { it.trim() }.filter { it.isNotBlank() },
            emergencyContact = EmergencyContact(
                name = s.draftEmergencyName.trim().takeIf { it.isNotBlank() },
                phone = s.draftEmergencyPhone.trim().takeIf { it.isNotBlank() },
                relation = s.draftEmergencyRelation.trim().takeIf { it.isNotBlank() },
            ),
        )
        _state.update { it.copy(isSaving = true, error = null) }
        scope.launch {
            updateProfile(patientId, profile)
                .onSuccess { saved -> _state.update { it.applyLoaded(saved).copy(isSaving = false, isEditing = false) } }
                .onFailure { err -> _state.update { it.copy(isSaving = false, error = err.toUserMessage("No pudimos guardar el perfil clínico")) } }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getProfile(patientId)
                .onSuccess { profile -> _state.update { it.applyLoaded(profile).copy(isLoading = false) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("No pudimos cargar tu perfil clínico")) } }
        }
    }
}

// ── State helpers (pure) ──────────────────────────────────────────────────────

private fun ClinicalProfileState.applyLoaded(profile: MedicalProfile): ClinicalProfileState = copy(
    bloodType = profile.bloodType,
    heightCm = profile.heightCm,
    weightKg = profile.weightKg,
    allergies = profile.allergies,
    conditions = profile.chronicConditions,
    emergencyContactName = profile.emergencyContact.name,
    emergencyContactPhone = profile.emergencyContact.phone,
    emergencyContactRelation = profile.emergencyContact.relation,
)

private fun ClinicalProfileState.resetDraftsToLoaded(): ClinicalProfileState = copy(
    isEditing = false,
    draftBloodType = bloodType ?: "",
    draftHeightCm = heightCm?.let { it.toInt().toString() } ?: "",
    draftWeightKg = weightKg?.let { it.toInt().toString() } ?: "",
    draftAllergies = allergies.joinToString(", "),
    draftConditions = conditions.joinToString(", "),
    draftEmergencyName = emergencyContactName ?: "",
    draftEmergencyPhone = emergencyContactPhone ?: "",
    draftEmergencyRelation = emergencyContactRelation ?: "",
)
