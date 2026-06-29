package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.patients.application.SuspendUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminSuspendUserComponent(
    componentContext: ComponentContext,
    patient: AdminPatientListItem,
    private val suspendUser: SuspendUserUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminSuspendUserComponent.Output) -> Unit,
) : AdminSuspendUserComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminSuspendUserState(patient = patient))
    override val state: Value<AdminSuspendUserState> = _state

    override fun onReasonSelected(reason: SuspendReason) {
        _state.update { it.copy(selectedReason = reason, error = null) }
    }

    override fun onFreeTextChange(text: String) {
        _state.update { it.copy(freeText = text, error = null) }
    }

    override fun onSubmit() {
        val st = _state.value
        if (!st.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            suspendUser(st.patient.userId, st.composedReason)
                .onSuccess {
                    onOutput(AdminSuspendUserComponent.Output.SuspendSuccess(st.patient.userId))
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Error suspendiendo usuario")) }
                }
        }
    }

    override fun onBack() {
        onOutput(AdminSuspendUserComponent.Output.Back)
    }
}
