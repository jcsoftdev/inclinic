package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminSubscriptionItem
import com.inclinic.app.features.admin.subscriptions.application.GetSubscriptionsUseCase
import com.inclinic.app.features.admin.subscriptions.application.SetUserSubscriptionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminSubscriptionsComponent(
    componentContext: ComponentContext,
    private val getSubscriptions: GetSubscriptionsUseCase,
    private val setUserSubscription: SetUserSubscriptionUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminSubscriptionsComponent.Output) -> Unit,
) : AdminSubscriptionsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminSubscriptionsState())
    override val state: Value<AdminSubscriptionsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onFilterChange(filter: AdminSubscriptionsFilter) {
        _state.update { it.copy(activeFilter = filter) }
    }

    override fun onManageClicked(item: AdminSubscriptionItem) {
        _state.update { it.copy(pendingManageItem = item, actionError = null) }
    }

    override fun onConfirmManage(item: AdminSubscriptionItem, newTier: String, expiresAt: String?) {
        _state.update { it.copy(isActioning = true, actionError = null) }
        scope.launch {
            setUserSubscription(userId = item.userId, tier = newTier, expiresAt = expiresAt)
                .onSuccess {
                    _state.update { it.copy(isActioning = false, pendingManageItem = null) }
                    load()
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isActioning = false,
                            actionError = err.toUserMessage("Error al cambiar suscripción"),
                        )
                    }
                }
        }
    }

    override fun onDismissDialog() {
        _state.update { it.copy(pendingManageItem = null, actionError = null) }
    }

    override fun onBack() {
        onOutput(AdminSubscriptionsComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getSubscriptions()
                .onSuccess { overview ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            stats = overview.stats,
                            allItems = overview.items,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.toUserMessage("Error cargando suscripciones"),
                        )
                    }
                }
        }
    }
}
