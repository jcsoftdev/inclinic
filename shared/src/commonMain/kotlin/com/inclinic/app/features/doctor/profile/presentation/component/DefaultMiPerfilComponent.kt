package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.doctor.profile.application.GetDoctorProfileUseCase
import com.inclinic.app.features.doctor.profile.application.UpdateDoctorProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultMiPerfilComponent(
    componentContext: ComponentContext,
    private val getProfile: GetDoctorProfileUseCase,
    private val updateProfile: UpdateDoctorProfileUseCase,
    private val logout: LogoutUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (MiPerfilComponent.Output) -> Unit,
) : MiPerfilComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(MiPerfilState())
    override val state: Value<MiPerfilState> = _state

    init { load() }

    override fun onRetry() { load() }

    override fun onNavigateEditSpecialties() = onOutput(MiPerfilComponent.Output.EditSpecialties)
    override fun onNavigateRequestSpecialty() = onOutput(MiPerfilComponent.Output.RequestSpecialty)
    override fun onNavigateMySpecialtyRequests() = onOutput(MiPerfilComponent.Output.MySpecialtyRequests)
    override fun onNavigateIncome() = onOutput(MiPerfilComponent.Output.Income)
    override fun onNavigateReviews() = onOutput(MiPerfilComponent.Output.Reviews)
    override fun onNavigatePublicProfile() = onOutput(MiPerfilComponent.Output.PublicProfile)
    override fun onNavigateEditHorarios() = onOutput(MiPerfilComponent.Output.EditHorarios)
    override fun onNavigatePackages() = onOutput(MiPerfilComponent.Output.Packages)
    override fun onNavigateSharing() = onOutput(MiPerfilComponent.Output.Sharing)
    override fun onNavigateSettings() = onOutput(MiPerfilComponent.Output.Settings)
    override fun onNavigateTherapyOffers() = onOutput(MiPerfilComponent.Output.TherapyOffers)
    override fun onNavigateNoShowQueue() = onOutput(MiPerfilComponent.Output.NoShowQueue)

    override fun onLogout() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        scope.launch {
            logout()
            _state.update { it.copy(isLoggingOut = false) }
            onOutput(MiPerfilComponent.Output.Logout)
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getProfile()
                .onSuccess { profile ->
                    _state.update { it.copy(isLoading = false, profile = profile) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading profile")) }
                }
        }
    }
}
