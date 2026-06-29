package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.patient.profile.application.GetMedicalProfileUseCase
import com.inclinic.app.features.patient.profile.application.GetPatientProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultProfileOverviewComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getProfile: GetPatientProfileUseCase,
    private val getMedicalProfile: GetMedicalProfileUseCase,
    private val logout: LogoutUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ProfileOverviewComponent.Output) -> Unit,
) : ProfileOverviewComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ProfileOverviewState())
    override val state: Value<ProfileOverviewState> = _state

    init {
        load()
        loadMedicalProfile()
    }

    override fun onEditProfile() { onOutput(ProfileOverviewComponent.Output.NavigateToEditProfile) }
    override fun onSettings() { onOutput(ProfileOverviewComponent.Output.NavigateToSettings) }
    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    // Session cleaned via LogoutUseCase → SessionEvents; RootComponent handles nav.
    override fun onLogout() { scope.launch { logout() } }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getProfile(patientId)
                .onSuccess { profile ->
                    _state.update { it.copy(isLoading = false, profile = profile) }
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
