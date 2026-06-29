package com.inclinic.app.features.admin.twofactor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.twofactor.application.EnableTwoFactorUseCase
import com.inclinic.app.features.admin.twofactor.application.SetupTwoFactorUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminTwoFactorSetupComponent(
    componentContext: ComponentContext,
    private val setupTwoFactorUseCase: SetupTwoFactorUseCase,
    private val enableTwoFactorUseCase: EnableTwoFactorUseCase,
    private val dispatchers: AppDispatchers,
    private val onActivated: () -> Unit,
    private val onBack: () -> Unit,
) : AdminTwoFactorSetupComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    private val _state = MutableValue(AdminTwoFactorSetupState())
    override val state: Value<AdminTwoFactorSetupState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        loadSetup()
    }

    override fun onCodeChange(code: String) {
        val filtered = code.filter { it.isDigit() }.take(6)
        _state.update { it.copy(code = filtered, activateError = null) }
    }

    override fun onActivate() {
        val current = _state.value
        if (!current.canActivate) return

        _state.update { it.copy(isActivating = true, activateError = null) }
        scope.launch {
            enableTwoFactorUseCase(current.code)
                .onSuccess {
                    _state.update { it.copy(isActivating = false) }
                    onActivated()
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isActivating = false, activateError = err.toUserMessage("Código incorrecto"))
                    }
                }
        }
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(activateError = null, setupError = null) }
    }

    override fun onBack() = onBack.invoke()

    private fun loadSetup() {
        _state.update { it.copy(isLoadingSetup = true, setupError = null) }
        scope.launch {
            setupTwoFactorUseCase()
                .onSuccess { setup ->
                    _state.update { it.copy(isLoadingSetup = false, setup = setup) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoadingSetup = false, setupError = err.toUserMessage("Error al iniciar configuración"))
                    }
                }
        }
    }
}
