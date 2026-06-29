@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.core.model.NotificationType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.NotificationDataSource
import com.inclinic.app.features.patient.notifications.application.GetNotificationsUseCase
import com.inclinic.app.features.patient.notifications.application.MarkAllNotificationsReadUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private fun testNotification(
    id: String = "notif-1",
    type: NotificationType = NotificationType.APPOINTMENT,
    read: Boolean = false,
    appointmentId: String = "apt-1",
): AppNotification = AppNotification(
    id = id,
    type = type,
    title = "Notification $id",
    message = "Message for $id",
    read = read,
    createdAt = Clock.System.now(),
    metadata = mapOf("appointmentId" to appointmentId),
)

private class FakeNotificationDataSource(
    private val notifications: List<AppNotification> = listOf(testNotification()),
    private val loadError: Throwable? = null,
    private var markReadResult: Result<Unit> = Result.success(Unit),
) : NotificationDataSource {
    var markAllReadCalled = false

    override suspend fun getNotifications(limit: Int): Result<List<AppNotification>> =
        if (loadError != null) Result.failure(loadError) else Result.success(notifications)

    override suspend fun markAllRead(): Result<Unit> {
        markAllReadCalled = true
        return markReadResult
    }
}

class DefaultNotificationsComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeNotificationDataSource = FakeNotificationDataSource(),
        outputs: MutableList<NotificationsComponent.Output> = mutableListOf(),
    ): DefaultNotificationsComponent {
        return DefaultNotificationsComponent(
            componentContext = ctx,
            getNotifications = GetNotificationsUseCase(dataSource, dispatchers),
            markAllRead = MarkAllNotificationsReadUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_populates_notifications() = runTest {
        val ds = FakeNotificationDataSource(
            notifications = listOf(
                testNotification("n-1"),
                testNotification("n-2"),
            ),
        )
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.notifications.size)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error() = runTest {
        val component = createComponent(
            dataSource = FakeNotificationDataSource(loadError = Exception("Timeout")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals("Timeout", state.error)
    }

    @Test
    fun onFilterChange_updates_filter_in_state() = runTest {
        val component = createComponent()

        component.onFilterChange(NotificationFilter.PAYMENTS)

        assertEquals(NotificationFilter.PAYMENTS, component.state.value.filter)
    }

    @Test
    fun filteredNotifications_returns_only_matching_type() = runTest {
        val ds = FakeNotificationDataSource(
            notifications = listOf(
                testNotification("n-1", NotificationType.APPOINTMENT),
                testNotification("n-2", NotificationType.PAYMENT),
                testNotification("n-3", NotificationType.APPOINTMENT),
            ),
        )
        val component = createComponent(dataSource = ds)

        component.onFilterChange(NotificationFilter.APPOINTMENTS)

        val filtered = component.state.value.filteredNotifications
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.type == NotificationType.APPOINTMENT })
    }

    @Test
    fun onMarkAllRead_marks_all_notifications_as_read() = runTest {
        val ds = FakeNotificationDataSource(
            notifications = listOf(
                testNotification("n-1", read = false),
                testNotification("n-2", read = false),
            ),
        )
        val component = createComponent(dataSource = ds)

        component.onMarkAllRead()

        assertTrue(component.state.value.notifications.all { it.read })
    }

    @Test
    fun onNotificationClick_appointment_type_emits_NavigateToAppointment() = runTest {
        val outputs = mutableListOf<NotificationsComponent.Output>()
        val component = createComponent(outputs = outputs)
        val notification = testNotification(type = NotificationType.APPOINTMENT, appointmentId = "apt-99")

        component.onNotificationClick(notification)

        assertEquals(1, outputs.size)
        val output = outputs.first() as NotificationsComponent.Output.NavigateToAppointment
        assertEquals("apt-99", output.appointmentId)
    }

    @Test
    fun onNotificationClick_payment_type_emits_NavigateToPayment() = runTest {
        val outputs = mutableListOf<NotificationsComponent.Output>()
        val component = createComponent(outputs = outputs)
        val notification = testNotification(type = NotificationType.PAYMENT, appointmentId = "apt-7")

        component.onNotificationClick(notification)

        assertEquals(1, outputs.size)
        val output = outputs.first() as NotificationsComponent.Output.NavigateToPayment
        assertEquals("apt-7", output.appointmentId)
    }

    @Test
    fun onNotificationClick_without_appointmentId_emits_nothing() = runTest {
        val outputs = mutableListOf<NotificationsComponent.Output>()
        val component = createComponent(outputs = outputs)
        val notification = AppNotification(
            id = "n-sys", type = NotificationType.SYSTEM,
            title = "System", message = "msg", read = false,
            createdAt = Clock.System.now(), metadata = emptyMap(),
        )

        component.onNotificationClick(notification)

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<NotificationsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is NotificationsComponent.Output.Back)
    }
}
