@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.admin.notifications.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.notifications.application.DeleteAdminNotificationUseCase
import com.inclinic.app.features.admin.notifications.application.GetAdminNotificationsUseCase
import com.inclinic.app.features.admin.notifications.application.MarkAdminNotificationReadUseCase
import com.inclinic.app.features.admin.notifications.application.MarkAllAdminNotificationsReadUseCase
import com.inclinic.app.features.admin.notifications.core.model.AdminNotification
import com.inclinic.app.features.admin.notifications.core.model.AdminNotificationKind
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationsRepository
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeAdminNotificationsRepository(
    private val notifications: List<AdminNotification>,
) : AdminNotificationsRepository {
    val markReadCalls = mutableListOf<String>()
    override suspend fun list(limit: Int): Result<List<AdminNotification>> = Result.success(notifications)
    override suspend fun markRead(id: String): Result<Unit> {
        markReadCalls.add(id)
        return Result.success(Unit)
    }
    override suspend fun markAllRead(): Result<Unit> = Result.success(Unit)
    override suspend fun delete(id: String): Result<Unit> = Result.success(Unit)
}

private fun notification(
    id: String = "n1",
    kind: AdminNotificationKind,
    link: String?,
    isRead: Boolean = false,
): AdminNotification = AdminNotification(
    id = id,
    kind = kind,
    title = "title",
    body = "body",
    createdAt = Instant.fromEpochMilliseconds(0),
    isRead = isRead,
    link = link,
)

// ── Tests ─────────────────────────────────────────────────────────────────────

/**
 * RED → GREEN — [DefaultAdminNotificationsComponent.onNotificationClick] must mark the
 * notification read AND emit the right navigate [AdminNotificationsComponent.Output] per
 * [AdminNotificationKind], parsing the id from the trailing path segment of `link`. Unmapped
 * kinds (MESSAGE, SYSTEM) or a missing link must never emit a navigate output — mark-read only.
 */
class DefaultAdminNotificationsComponentTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : AppDispatchers {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle)

    private fun createComponent(
        repository: FakeAdminNotificationsRepository,
        outputs: MutableList<AdminNotificationsComponent.Output> = mutableListOf(),
    ): DefaultAdminNotificationsComponent = DefaultAdminNotificationsComponent(
        componentContext = ctx,
        getNotifications = GetAdminNotificationsUseCase(repository, dispatchers),
        markRead = MarkAdminNotificationReadUseCase(repository, dispatchers),
        markAllRead = MarkAllAdminNotificationsReadUseCase(repository, dispatchers),
        deleteNotification = DeleteAdminNotificationUseCase(repository, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun onNotificationClick_appointment_kind_parses_id_and_marks_read() = runTest {
        val n = notification(kind = AdminNotificationKind.APPOINTMENT, link = "/admin/appointments/appt-42")
        val repository = FakeAdminNotificationsRepository(listOf(n))
        val outputs = mutableListOf<AdminNotificationsComponent.Output>()
        val component = createComponent(repository, outputs)

        component.onNotificationClick(n)

        assertEquals(listOf("n1"), repository.markReadCalls)
        val output = assertIs<AdminNotificationsComponent.Output.NavigateToAppointment>(outputs.single())
        assertEquals("appt-42", output.appointmentId)
    }

    @Test
    fun onNotificationClick_doctor_kind_parses_id() = runTest {
        val n = notification(kind = AdminNotificationKind.DOCTOR, link = "/admin/doctors/doc-7")
        val repository = FakeAdminNotificationsRepository(listOf(n))
        val outputs = mutableListOf<AdminNotificationsComponent.Output>()
        val component = createComponent(repository, outputs)

        component.onNotificationClick(n)

        val output = assertIs<AdminNotificationsComponent.Output.NavigateToDoctor>(outputs.single())
        assertEquals("doc-7", output.doctorId)
    }

    @Test
    fun onNotificationClick_specialty_kind_navigates_without_needing_a_link() = runTest {
        val n = notification(kind = AdminNotificationKind.SPECIALTY, link = null)
        val repository = FakeAdminNotificationsRepository(listOf(n))
        val outputs = mutableListOf<AdminNotificationsComponent.Output>()
        val component = createComponent(repository, outputs)

        component.onNotificationClick(n)

        assertEquals(AdminNotificationsComponent.Output.NavigateToSpecialtyRequests, outputs.single())
    }

    @Test
    fun onNotificationClick_payment_kind_navigates_to_finance() = runTest {
        val n = notification(kind = AdminNotificationKind.PAYMENT, link = null)
        val repository = FakeAdminNotificationsRepository(listOf(n))
        val outputs = mutableListOf<AdminNotificationsComponent.Output>()
        val component = createComponent(repository, outputs)

        component.onNotificationClick(n)

        assertEquals(AdminNotificationsComponent.Output.NavigateToFinance, outputs.single())
    }

    @Test
    fun onNotificationClick_unmapped_kind_marks_read_and_emits_no_output() = runTest {
        val n = notification(kind = AdminNotificationKind.SYSTEM, link = "/whatever/123")
        val repository = FakeAdminNotificationsRepository(listOf(n))
        val outputs = mutableListOf<AdminNotificationsComponent.Output>()
        val component = createComponent(repository, outputs)

        component.onNotificationClick(n)

        assertEquals(listOf("n1"), repository.markReadCalls)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onNotificationClick_appointment_kind_with_unparseable_link_does_not_crash_or_navigate() = runTest {
        val n = notification(kind = AdminNotificationKind.APPOINTMENT, link = "")
        val repository = FakeAdminNotificationsRepository(listOf(n))
        val outputs = mutableListOf<AdminNotificationsComponent.Output>()
        val component = createComponent(repository, outputs)

        component.onNotificationClick(n)

        assertEquals(listOf("n1"), repository.markReadCalls)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onNotificationClick_already_read_notification_does_not_call_markRead_again() = runTest {
        val n = notification(kind = AdminNotificationKind.SYSTEM, link = null, isRead = true)
        val repository = FakeAdminNotificationsRepository(listOf(n))
        val component = createComponent(repository)

        component.onNotificationClick(n)

        assertTrue(repository.markReadCalls.isEmpty())
    }
}
