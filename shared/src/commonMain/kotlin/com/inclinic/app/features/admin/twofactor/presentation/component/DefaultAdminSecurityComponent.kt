package com.inclinic.app.features.admin.twofactor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.twofactor.application.DisableTwoFactorUseCase
import com.inclinic.app.features.admin.twofactor.application.GetTwoFactorStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminSecurityComponent(
    componentContext: ComponentContext,
    private val getTwoFactorStatusUseCase: GetTwoFactorStatusUseCase,
    private val disableTwoFactorUseCase: DisableTwoFactorUseCase,
    private val dispatchers: AppDispatchers,
    private val onNavigateToSetup: () -> Unit,
    private val onBack: () -> Unit,
) : AdminSecurityComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    private val _state = MutableValue(AdminSecurityState())
    override val state: Value<AdminSecurityState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        loadStatus()
    }

    override fun onRetry() = loadStatus()

    override fun onSetupTwoFactor() = onNavigateToSetup()

    override fun onDisableTwoFactor() {
        _state.update { it.copy(showDisableDialog = true, disableCode = "", disableError = null) }
    }

    override fun onDisableConfirm(code: String) {
        if (code.length != 6) return
        _state.update { it.copy(isDisabling = true, disableError = null) }
        scope.launch {
            disableTwoFactorUseCase(code)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isDisabling = false,
                            showDisableDialog = false,
                            successMessage = "2FA desactivado correctamente",
                        )
                    }
                    loadStatus()
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isDisabling = false, disableError = err.toUserMessage("Error al desactivar"))
                    }
                }
        }
    }

    override fun onDisableDialogDismiss() {
        _state.update { it.copy(showDisableDialog = false, disableError = null) }
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null, successMessage = null) }
    }

    override fun onBack() = onBack.invoke()

    private fun loadStatus() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getTwoFactorStatusUseCase()
                .onSuccess { status ->
                    _state.update { it.copy(isLoading = false, status = status) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.toUserMessage("Error al cargar estado"))
                    }
                }
        }
    }
}
