package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.doctors.application.ApproveDoctorUseCase
import com.inclinic.app.features.admin.doctors.application.GetPendingDoctorByIdUseCase
import com.inclinic.app.features.admin.doctors.application.RejectDoctorUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminPendingDoctorDetailComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getPendingDoctorById: GetPendingDoctorByIdUseCase,
    private val approveDoctor: ApproveDoctorUseCase,
    private val rejectDoctor: RejectDoctorUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminPendingDoctorDetailComponent.Output) -> Unit,
) : AdminPendingDoctorDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminPendingDoctorDetailState())
    override val state: Value<AdminPendingDoctorDetailState> = _state

    init { load() }

    override fun onBack() {
        onOutput(AdminPendingDoctorDetailComponent.Output.Back)
    }

    override fun onReasonChange(reason: String) {
        _state.update { it.copy(rejectReason = reason, rejectError = null) }
    }

    override fun onApprove() {
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            approveDoctor(doctorId)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onOutput(AdminPendingDoctorDetailComponent.Output.ApproveSuccess)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Error al aprobar")) }
                }
        }
    }

    override fun onConfirmReject() {
        val reason = _state.value.rejectReason.trim()
        if (reason.length < 10) {
            _state.update { it.copy(rejectError = "El motivo debe tener al menos 10 caracteres") }
            return
        }
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, rejectError = null, error = null) }
        scope.launch {
            rejectDoctor(doctorId, reason)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onOutput(AdminPendingDoctorDetailComponent.Output.Back)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Error al rechazar")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPendingDoctorById(doctorId)
                .onSuccess { doctor ->
                    _state.update { it.copy(isLoading = false, doctor = doctor) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando solicitud")) }
                }
        }
    }
}
