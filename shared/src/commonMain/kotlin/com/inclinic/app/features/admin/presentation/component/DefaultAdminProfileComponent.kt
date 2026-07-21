package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.GetCurrentUserUseCase
import com.inclinic.app.features.auth.application.LogoutUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminProfileComponent(
    componentContext: ComponentContext,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val dispatchers: AppDispatchers,
    private val onOpenSecurity: () -> Unit,
    private val onLogout: () -> Unit,
    private val onBack: () -> Unit,
) : AdminProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    // Declared before `init` on purpose: Kotlin initialises properties in declaration order,
    // so `loadUser()` below would touch a still-null `_state` if this came after the block.
    private val _state = MutableValue(AdminProfileState())
    override val state: Value<AdminProfileState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        loadUser()
    }

    override fun onOpenSecurity() = onOpenSecurity.invoke()

    override fun onLogout() {
        _state.update { it.copy(isLoggingOut = true) }
        scope.launch {
            logoutUseCase()
            onLogout.invoke()
        }
    }

    override fun onRetry() = loadUser()

    override fun onBack() = onBack.invoke()

    private fun loadUser() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getCurrentUserUseCase()
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.toUserMessage("Error al cargar el perfil"))
                    }
                }
        }
    }
}
