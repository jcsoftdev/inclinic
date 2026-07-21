package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.twofactor.application.GetTwoFactorStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminConfigComponent(
    componentContext: ComponentContext,
    private val getTwoFactorStatusUseCase: GetTwoFactorStatusUseCase,
    private val dispatchers: AppDispatchers,
    private val onOpenSecurity: () -> Unit,
    private val onBack: () -> Unit,
) : AdminConfigComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    // Declared before `init` on purpose: Kotlin initialises properties in declaration order,
    // so `loadStatus()` below would touch a still-null `_state` if this came after the block.
    private val _state = MutableValue(AdminConfigState())
    override val state: Value<AdminConfigState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        loadStatus()
    }

    override fun onOpenSecurity() = onOpenSecurity.invoke()

    override fun onRetry() = loadStatus()

    override fun onBack() = onBack.invoke()

    private fun loadStatus() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getTwoFactorStatusUseCase()
                .onSuccess { status ->
                    _state.update { it.copy(isLoading = false, twoFactorStatus = status) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.toUserMessage("Error al cargar configuración"))
                    }
                }
        }
    }
}
