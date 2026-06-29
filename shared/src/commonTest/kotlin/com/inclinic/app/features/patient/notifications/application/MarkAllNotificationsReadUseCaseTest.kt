@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.notifications.application

import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.NotificationDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeMarkReadDataSource : NotificationDataSource {
    var markResult: Result<Unit> = Result.success(Unit)
    var callCount = 0

    override suspend fun getNotifications(limit: Int): Result<List<AppNotification>> = Result.success(emptyList())

    override suspend fun markAllRead(): Result<Unit> {
        callCount++
        return markResult
    }
}

class MarkAllNotificationsReadUseCaseTest {

    private val fake = FakeMarkReadDataSource()
    private val useCase = MarkAllNotificationsReadUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_calls_datasource() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, fake.callCount)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.markResult = Result.failure(Exception("Server error"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }
}
