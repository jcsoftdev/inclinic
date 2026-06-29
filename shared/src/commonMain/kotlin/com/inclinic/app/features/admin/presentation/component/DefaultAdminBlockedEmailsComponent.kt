package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.blockedemails.application.BlockEmailUseCase
import com.inclinic.app.features.admin.blockedemails.application.GetBlockedEmailsUseCase
import com.inclinic.app.features.admin.blockedemails.application.UnblockEmailUseCase
import com.inclinic.app.features.admin.infrastructure.remote.AdminBlockedEmailItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminBlockedEmailsComponent(
    componentContext: ComponentContext,
    private val getBlockedEmails: GetBlockedEmailsUseCase,
    private val blockEmail: BlockEmailUseCase,
    private val unblockEmail: UnblockEmailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminBlockedEmailsComponent.Output) -> Unit,
) : AdminBlockedEmailsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminBlockedEmailsState())
    override val state: Value<AdminBlockedEmailsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onShowBlockDialog() {
        _state.update { it.copy(showBlockDialog = true, actionError = null) }
    }

    override fun onDismissBlockDialog() {
        _state.update { it.copy(showBlockDialog = false, actionError = null) }
    }

    override fun onBlock(email: String, reason: String, durationDays: Int?) {
        if (email.isBlank() || !email.contains("@")) {
            _state.update { it.copy(actionError = "Ingresa un email válido") }
            return
        }
        if (reason.length < 10) {
            _state.update { it.copy(actionError = "La razón debe tener al menos 10 caracteres") }
            return
        }
        _state.update { it.copy(isActing = true, actionError = null) }
        scope.launch {
            blockEmail(email, reason, durationDays)
                .onSuccess {
                    _state.update { it.copy(isActing = false, showBlockDialog = false) }
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(isActing = false, actionError = err.toUserMessage("Error bloqueando email")) }
                }
        }
    }

    override fun onUnblock(item: AdminBlockedEmailItem) {
        _state.update { it.copy(isActing = true, actionError = null) }
        scope.launch {
            unblockEmail(item.email)
                .onSuccess {
                    _state.update { st ->
                        st.copy(
                            isActing = false,
                            items = st.items.filter { it.email != item.email },
                        )
                    }
                    // Refresh for authoritative state
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(isActing = false, actionError = err.toUserMessage("Error desbloqueando email")) }
                }
        }
    }

    override fun onBack() {
        onOutput(AdminBlockedEmailsComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getBlockedEmails()
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando emails bloqueados")) }
                }
        }
    }
}
