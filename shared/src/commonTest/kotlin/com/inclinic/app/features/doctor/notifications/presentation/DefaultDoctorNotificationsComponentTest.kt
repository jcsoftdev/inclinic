package com.inclinic.app.features.doctor.notifications.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.notifications.application.GetDoctorNotificationsUseCase
import com.inclinic.app.features.doctor.notifications.application.MarkAllNotificationsReadUseCase
import com.inclinic.app.features.doctor.notifications.application.MarkNotificationReadUseCase
import com.inclinic.app.features.doctor.notifications.core.model.NotificationKind
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import com.inclinic.app.features.doctor.notifications.fakes.FakeDoctorNotificationsRepository
import com.inclinic.app.features.doctor.notifications.fakes.stubNotification
import com.inclinic.app.features.doctor.notifications.presentation.component.DefaultDoctorNotificationsComponent
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultDoctorNotificationsComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val repo = FakeDoctorNotificationsRepository()

    private fun createComponent(
        onOutput: (DoctorNotificationsComponent.Output) -> Unit = {},
    ) = DefaultDoctorNotificationsComponent(
        componentContext = ctx,
        getNotifications = GetDoctorNotificationsUseCase(repo, dispatchers),
        markRead = MarkNotificationReadUseCase(repo, dispatchers),
        markAllRead = MarkAllNotificationsReadUseCase(repo, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    @Test
    fun initial_filter_is_all() {
        val component = createComponent()
        assertEquals(NotificationFilter.ALL, component.state.value.activeFilter)
    }

    @Test
    fun loads_notifications_on_init() = runTest {
        repo.listResult = Result.success(listOf(stubNotification("n1"), stubNotification("n2")))
        val component = createComponent()
        assertEquals(2, component.state.value.notifications.size)
    }

    @Test
    fun filter_change_to_appointments_updates_active_filter() = runTest {
        repo.listResult = Result.success(listOf(stubNotification("n1")))
        val component = createComponent()
        component.onFilterChange(NotificationFilter.APPOINTMENTS)
        assertEquals(NotificationFilter.APPOINTMENTS, component.state.value.activeFilter)
    }

    @Test
    fun filtered_notifications_returns_only_matching_category() = runTest {
        repo.listResult = Result.success(
            listOf(
                stubNotification("a1", kind = NotificationKind.APPOINTMENT),
                stubNotification("p1", kind = NotificationKind.PAYMENT),
                stubNotification("s1", kind = NotificationKind.SHARE),
            ),
        )
        val component = createComponent()

        component.onFilterChange(NotificationFilter.APPOINTMENTS)
        assertEquals(listOf("a1"), component.state.value.filteredNotifications.map { it.id })

        component.onFilterChange(NotificationFilter.PAYMENTS)
        assertEquals(listOf("p1"), component.state.value.filteredNotifications.map { it.id })

        component.onFilterChange(NotificationFilter.SHARE)
        assertEquals(listOf("s1"), component.state.value.filteredNotifications.map { it.id })

        component.onFilterChange(NotificationFilter.ALL)
        assertEquals(listOf("a1", "p1", "s1"), component.state.value.filteredNotifications.map { it.id })
    }

    @Test
    fun on_mark_read_updates_notification_isRead() = runTest {
        repo.listResult = Result.success(listOf(stubNotification("n1", isRead = false)))
        val component = createComponent()

        component.onMarkRead("n1")

        assertTrue(component.state.value.notifications[0].isRead)
    }

    @Test
    fun on_mark_all_read_sets_all_notifications_to_read() = runTest {
        repo.listResult = Result.success(listOf(
            stubNotification("n1", isRead = false),
            stubNotification("n2", isRead = false),
        ))
        val component = createComponent()

        component.onMarkAllRead()

        assertTrue(component.state.value.notifications.all { it.isRead })
    }

    @Test
    fun on_back_emits_back_output() {
        var output: DoctorNotificationsComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onBack()
        assertEquals(DoctorNotificationsComponent.Output.Back, output)
    }

    @Test
    fun error_is_shown_when_load_fails() = runTest {
        repo.listResult = Result.failure(RuntimeException("Load error"))
        val component = createComponent()
        assertNotNull(component.state.value.error)
    }

    @Test
    fun state_is_not_loading_after_successful_load() = runTest {
        repo.listResult = Result.success(emptyList())
        val component = createComponent()
        component.state.asFlow().test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T : Any> com.arkivanov.decompose.value.Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
