package com.inclinic.app.features.doctor.notifications.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.notifications.application.GetDoctorNotificationsUseCase
import com.inclinic.app.features.doctor.notifications.application.MarkAllNotificationsReadUseCase
import com.inclinic.app.features.doctor.notifications.application.MarkNotificationReadUseCase
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDoctorNotificationsComponent(
    componentContext: ComponentContext,
    private val getNotifications: GetDoctorNotificationsUseCase,
    private val markRead: MarkNotificationReadUseCase,
    private val markAllRead: MarkAllNotificationsReadUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DoctorNotificationsComponent.Output) -> Unit,
) : DoctorNotificationsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DoctorNotificationsState())
    override val state: Value<DoctorNotificationsState> = _state

    init { load(NotificationFilter.ALL) }

    override fun onRefresh() { load(NotificationFilter.ALL) }

    override fun onFilterChange(filter: NotificationFilter) {
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
        scope.launch {
            markAllRead().onSuccess {
                _state.update { state ->
                    state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
                }
            }
        }
    }

    override fun onBack() {
        onOutput(DoctorNotificationsComponent.Output.Back)
    }

    private fun load(filter: NotificationFilter) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getNotifications(filter)
                .onSuccess { list ->
                    _state.update { it.copy(isLoading = false, notifications = list) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading notifications")) }
                }
        }
    }
}
