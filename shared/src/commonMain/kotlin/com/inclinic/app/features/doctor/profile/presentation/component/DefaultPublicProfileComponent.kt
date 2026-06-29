package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.application.GetDoctorProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Public profile screen component (o1WrKB — Doctor Mi Perfil Público).
 * Reuses the same [GetDoctorProfileUseCase] as Mi Perfil — the public view
 * shows a read-only subset of the doctor's own data.
 */
class DefaultPublicProfileComponent(
    componentContext: ComponentContext,
    private val getProfile: GetDoctorProfileUseCase,
    private val dispatchers: AppDispatchers,
    private val onBack: () -> Unit,
) : PublicProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    private val _state = MutableValue(PublicProfileState())
    override val state: Value<PublicProfileState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        load()
    }

    override fun onBack() = onBack.invoke()

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
