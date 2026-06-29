@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.notifications.application

import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.core.model.NotificationType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.NotificationDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeNotificationDataSource : NotificationDataSource {
    var result: Result<List<AppNotification>> = Result.success(emptyList())
    var callCount = 0

    override suspend fun getNotifications(limit: Int): Result<List<AppNotification>> {
        callCount++
        return result
    }

    override suspend fun markAllRead(): Result<Unit> = Result.success(Unit)
}

class GetNotificationsUseCaseTest {

    private val fake = FakeNotificationDataSource()
    private val useCase = GetNotificationsUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_notifications_list() = runTest {
        val notifications = listOf(
            AppNotification(id = "n1", type = NotificationType.APPOINTMENT, title = "Cita confirmada", message = "Tu cita fue confirmada", read = false, createdAt = Clock.System.now()),
            AppNotification(id = "n2", type = NotificationType.PAYMENT, title = "Nuevo mensaje", message = "Dr. Torres te escribió", read = true, createdAt = Clock.System.now()),
        )
        fake.result = Result.success(notifications)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("n1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun success_empty_list() = runTest {
        fake.result = Result.success(emptyList())

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.result = Result.failure(Exception("Unauthorized"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
    }
}
