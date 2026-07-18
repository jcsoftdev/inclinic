package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.isNotFoundError
import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.doctors.application.GetAdminDoctorDetailUseCase
import com.inclinic.app.features.admin.patients.application.SuspendUserUseCase
import com.inclinic.app.features.admin.patients.application.UnsuspendUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * [onSuspend]/[onUnsuspend] mirror the calling pattern in [DefaultAdminSuspendUserComponent]:
 * flip a submitting flag, call the shared [SuspendUserUseCase] / [UnsuspendUserUseCase] with
 * `detail.user.id`, and on success re-[load] the detail so [AdminDoctorDetailState.detail]
 * reflects the new `isSuspended` value (the backend doesn't return it inline).
 */
class DefaultAdminDoctorDetailComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getDetail: GetAdminDoctorDetailUseCase,
    private val suspendUser: SuspendUserUseCase,
    private val unsuspendUser: UnsuspendUserUseCase,
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

    override fun onSuspend(reason: String) {
        val userId = _state.value.detail?.user?.id ?: return
        if (_state.value.isSuspending) return
        _state.update { it.copy(isSuspending = true, suspendError = null) }
        scope.launch {
            suspendUser(userId, reason)
                .onSuccess {
                    _state.update { it.copy(isSuspending = false) }
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(isSuspending = false, suspendError = err.toUserMessage("Error suspendiendo doctor")) }
                }
        }
    }

    override fun onUnsuspend() {
        val userId = _state.value.detail?.user?.id ?: return
        if (_state.value.isSuspending) return
        _state.update { it.copy(isSuspending = true, suspendError = null) }
        scope.launch {
            unsuspendUser(userId)
                .onSuccess {
                    _state.update { it.copy(isSuspending = false) }
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(isSuspending = false, suspendError = err.toUserMessage("Error reactivando doctor")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null, notFound = false) }
        scope.launch {
            getDetail(doctorId)
                .onSuccess { detail ->
                    _state.update { it.copy(isLoading = false, detail = detail) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.toUserMessage("Error cargando doctor"),
                            notFound = err.isNotFoundError(),
                        )
                    }
                }
        }
    }
}
