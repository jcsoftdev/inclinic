package com.inclinic.app.features.patient.moderation.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.moderation.application.ReportUserUseCase
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultReportUserComponent(
    componentContext: ComponentContext,
    private val targetUserId: String,
    private val targetUserName: String,
    private val reportUser: ReportUserUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ReportUserComponent.Output) -> Unit,
) : ReportUserComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(
        ReportUserState(targetUserId = targetUserId, targetUserName = targetUserName)
    )
    override val state: Value<ReportUserState> = _state

    override fun onReasonChanged(reason: String) {
        _state.update { it.copy(reason = reason, error = null) }
    }

    override fun onCategorySelected(category: ReportCategory) {
        _state.update { it.copy(selectedCategory = category, error = null) }
    }

    override fun onSubmit() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            reportUser(targetUserId, _state.value.reason, _state.value.selectedCategory)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                    onOutput(ReportUserComponent.Output.Submitted)
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }

    override fun onBack() { onOutput(ReportUserComponent.Output.Back) }
}
