package com.inclinic.app.features.doctor.notifications.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.notifications.fakes.FakeDoctorNotificationsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkNotificationReadUseCaseTest {

    private val repo = FakeDoctorNotificationsRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = MarkNotificationReadUseCase(repo, dispatchers)

    @Test
    fun marks_notification_read_successfully() = runTest {
        val result = useCase("n1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun passes_id_to_repository() = runTest {
        useCase("notif-99")
        assertEquals("notif-99", repo.lastMarkReadId)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.markReadResult = Result.failure(RuntimeException("403"))
        val result = useCase("n1")
        assertTrue(result.isFailure)
    }
}
