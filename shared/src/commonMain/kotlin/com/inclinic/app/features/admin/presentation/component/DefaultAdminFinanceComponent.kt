package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.finance.application.ExportFinanceCsvUseCase
import com.inclinic.app.features.admin.finance.application.GetFinanceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminFinanceComponent(
    componentContext: ComponentContext,
    private val getFinance: GetFinanceUseCase,
    private val exportFinanceCsv: ExportFinanceCsvUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminFinanceComponent.Output) -> Unit,
) : AdminFinanceComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminFinanceState())
    override val state: Value<AdminFinanceState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onBack() {
        onOutput(AdminFinanceComponent.Output.Back)
    }

    override fun onExport() {
        if (_state.value.isExporting) return
        _state.update { it.copy(isExporting = true, exportMessage = null, exportBytes = null) }
        scope.launch {
            exportFinanceCsv()
                .onSuccess { bytes ->
                    // Hand bytes to the screen; it will call onExportHandled() after saving.
                    _state.update { it.copy(isExporting = false, exportBytes = bytes) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportMessage = err.toUserMessage("Error exportando CSV"),
                        )
                    }
                }
        }
    }

    override fun onExportHandled() {
        val kb = (_state.value.exportBytes?.size ?: 0) / 1024
        _state.update {
            it.copy(
                exportBytes = null,
                exportMessage = "CSV guardado (${kb}KB). Revisa la app de descargas.",
            )
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getFinance()
                .onSuccess { data ->
                    val balanceTotal = data.totalReleasedRevenue + data.held.total
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedOnce = true,
                            balanceTotal = "S/ ${formatCompactMoney(balanceTotal)}",
                            released = "S/ ${formatCompactMoney(data.totalReleasedRevenue)}",
                            held = "S/ ${formatCompactMoney(data.held.total)}",
                            thisMonthRevenue = "S/ ${formatCompactMoney(data.thisMonth.revenue)}",
                            heldAmount = "S/ ${formatCompactMoney(data.held.total)}",
                            heldCount = data.held.count,
                            topDoctors = data.topDoctors,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando finanzas")) }
                }
        }
    }
}
