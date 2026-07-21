package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.util.formatThousands
import com.inclinic.app.features.admin.dashboard.application.GetAdminDashboardUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class DefaultAdminDashboardComponent(
    componentContext: ComponentContext,
    private val getDashboard: GetAdminDashboardUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminDashboardComponent.Output) -> Unit,
) : AdminDashboardComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminDashboardState())
    override val state: Value<AdminDashboardState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onNavigateToNotifications() {
        onOutput(AdminDashboardComponent.Output.NavigateToNotifications)
    }

    override fun onNavigateToDoctorApprovals() {
        onOutput(AdminDashboardComponent.Output.NavigateToDoctorApprovals)
    }

    override fun onNavigateToDisputes() {
        onOutput(AdminDashboardComponent.Output.NavigateToDisputes)
    }

    override fun onNavigateToFinance() {
        onOutput(AdminDashboardComponent.Output.NavigateToFinance)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDashboard()
                .onSuccess { data ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedOnce = true,
                            appointmentsToday = data.appointmentsToday,
                            pendingDoctors = data.pendingDoctors,
                            monthRevenue = "S/ ${formatCompactMoney(data.monthRevenue)}",
                            pendingDisputes = data.pendingDisputes,
                            noShowAppointments = data.noShowAppointments,
                            riskCount = data.pendingDisputes + data.noShowAppointments,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando el panel")) }
                }
        }
    }
}

/**
 * Formats PEN amounts compactly for the KPI tile (e.g. 8420.0 -> "8.4K", 980.0 -> "980").
 * Mirrors the design's "S/ 8.4K" treatment.
 */
internal fun formatCompactMoney(value: Double): String {
    val rounded = value.roundToLong()
    if (rounded < 1000) return rounded.formatThousands()
    val thousands = rounded / 100L
    val intPart = thousands / 10L
    val decPart = thousands % 10L
    return if (decPart == 0L) "${intPart.formatThousands()}K" else "${intPart.formatThousands()}.${decPart}K"
}
