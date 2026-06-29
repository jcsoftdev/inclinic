package com.inclinic.app.features.admin.notifications.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.notifications.application.DeleteAdminNotificationUseCase
import com.inclinic.app.features.admin.notifications.application.GetAdminNotificationsUseCase
import com.inclinic.app.features.admin.notifications.application.MarkAdminNotificationReadUseCase
import com.inclinic.app.features.admin.notifications.application.MarkAllAdminNotificationsReadUseCase
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminNotificationsComponent(
    componentContext: ComponentContext,
    private val getNotifications: GetAdminNotificationsUseCase,
    private val markRead: MarkAdminNotificationReadUseCase,
    private val markAllRead: MarkAllAdminNotificationsReadUseCase,
    private val deleteNotification: DeleteAdminNotificationUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminNotificationsComponent.Output) -> Unit,
) : AdminNotificationsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminNotificationsState())
    override val state: Value<AdminNotificationsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onFilterChange(filter: AdminNotificationFilter) {
        _state.update { it.copy(activeFilter = filter) }
    }

    override fun onMarkRead(id: String) {
        scope.launch {
            markRead(id).onSuccess {
                _state.update { state ->
                    state.copy(
                        notifications = state.notifications.map { n ->
                            if (n.id == id) n.copy(isRead = true) else n
                        }
                    )
                }
            }
        }
    }

    override fun onMarkAllRead() {
        _state.update { it.copy(isActing = true) }
        scope.launch {
            markAllRead()
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            isActing = false,
                            notifications = state.notifications.map { it.copy(isRead = true) },
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isActing = false) }
                }
        }
    }

    override fun onDelete(id: String) {
        _state.update { it.copy(isActing = true) }
        scope.launch {
            deleteNotification(id)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            isActing = false,
                            notifications = state.notifications.filter { it.id != id },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isActing = false, error = err.toUserMessage("Error al eliminar")) }
                }
        }
    }

    override fun onBack() {
        onOutput(AdminNotificationsComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getNotifications()
                .onSuccess { list ->
                    _state.update { it.copy(isLoading = false, notifications = list) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.toUserMessage("Error cargando notificaciones"))
                    }
                }
        }
    }
}
