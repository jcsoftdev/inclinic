package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.finance.application.GetFinanceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminFinanceComponent(
    componentContext: ComponentContext,
    private val getFinance: GetFinanceUseCase,
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

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getFinance()
                .onSuccess { data ->
                    val balanceTotal = data.totalReleasedRevenue + data.held.total
                    _state.update {
                        it.copy(
                            isLoading = false,
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
