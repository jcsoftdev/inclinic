package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.doctors.application.GetAdminDoctorDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminDoctorDetailComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getDetail: GetAdminDoctorDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminDoctorDetailComponent.Output) -> Unit,
) : AdminDoctorDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminDoctorDetailState())
    override val state: Value<AdminDoctorDetailState> = _state

    init { load() }

    override fun onBack() {
        onOutput(AdminDoctorDetailComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDetail(doctorId)
                .onSuccess { detail ->
                    _state.update { it.copy(isLoading = false, detail = detail) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando doctor")) }
                }
        }
    }
}
