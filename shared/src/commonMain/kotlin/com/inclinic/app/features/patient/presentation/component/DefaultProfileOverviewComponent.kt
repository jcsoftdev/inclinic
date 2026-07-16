package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
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

    // Recarga cada vez que la pantalla vuelve a estar activa (p.ej. al regresar
    // de "Editar perfil" tras guardar), para que la UI refleje los cambios sin
    // depender de reconstruir el componente.
    init {
        lifecycle.doOnResume {
            load()
            loadMedicalProfile()
        }
    }

    override fun onEditProfile() { onOutput(ProfileOverviewComponent.Output.NavigateToEditProfile) }
    override fun onSettings() { onOutput(ProfileOverviewComponent.Output.NavigateToSettings) }
    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    // Session cleaned via LogoutUseCase → SessionEvents; RootComponent handles nav.
    override fun onLogout() { scope.launch { logout() } }

    private fun load() {
        // Spinner solo en la primera carga; los refrescos al volver son silenciosos
        // para no parpadear sobre datos ya visibles.
        val showSpinner = _state.value.profile == null
        _state.update { it.copy(isLoading = showSpinner, error = null) }
        scope.launch {
            getProfile(patientId)
                .onSuccess { profile ->
                    _state.update { it.copy(isLoading = false, profile = profile) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("No se pudo cargar el perfil")) }
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
