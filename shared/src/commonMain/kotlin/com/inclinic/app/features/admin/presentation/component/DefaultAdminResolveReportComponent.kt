package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.navigation.AdminConfig
import com.inclinic.app.features.admin.reports.application.ResolveReportUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminResolveReportComponent(
    componentContext: ComponentContext,
    config: AdminConfig.MasResolveReport,
    private val resolveReport: ResolveReportUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminResolveReportComponent.Output) -> Unit,
) : AdminResolveReportComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(
        AdminResolveReportState(
            reportId = config.reportId,
            reportStatus = config.reportStatus,
            category = config.category,
            reason = config.reason,
            reportedUserFirstName = config.reportedUserFirstName,
            reportedUserLastName = config.reportedUserLastName,
            reportedUserRole = config.reportedUserRole,
            createdAt = config.createdAt,
        )
    )
    override val state: Value<AdminResolveReportState> = _state

    override fun onSelectDecision(decision: ReportDecision) {
        _state.update { it.copy(selectedDecision = decision, submitError = null) }
    }

    override fun onAdminNoteChange(note: String) {
        _state.update { it.copy(adminNote = note) }
    }

    override fun onConfirm() {
        val decision = _state.value.selectedDecision ?: return
        submit(decision.apiStatus)
    }

    override fun onQuickDismiss() {
        // Same path as tapping the "Descartar" decision card + confirming.
        _state.update { it.copy(selectedDecision = ReportDecision.Dismissed) }
        submit(ReportDecision.Dismissed.apiStatus)
    }

    override fun onEscalate() {
        submit(ESCALATED_STATUS)
    }

    override fun onBack() {
        onOutput(AdminResolveReportComponent.Output.Back)
    }

    private fun submit(status: String) {
        _state.update { it.copy(isSubmitting = true, submitError = null) }
        scope.launch {
            resolveReport(
                reportId = _state.value.reportId,
                status = status,
                adminNote = _state.value.adminNote.trim().ifBlank { null },
            )
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onOutput(AdminResolveReportComponent.Output.ResolvedSuccess)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, submitError = err.toUserMessage("Error al resolver el reporte")) }
                }
        }
    }

    private companion object {
        /** Best-guess status literal for "escalated" — pending backend confirmation. */
        const val ESCALATED_STATUS = "ESCALATED"
    }
}
