package com.inclinic.app.features.doctor.notifications.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.notifications.fakes.FakeDoctorNotificationsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkAllNotificationsReadUseCaseTest {

    private val repo = FakeDoctorNotificationsRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = MarkAllNotificationsReadUseCase(repo, dispatchers)

    @Test
    fun marks_all_read_successfully() = runTest {
        val result = useCase()
        assertTrue(result.isSuccess)
    }

    @Test
    fun calls_mark_all_read_on_repository_once() = runTest {
        useCase()
        assertEquals(1, repo.markAllReadCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.markAllReadResult = Result.failure(RuntimeException("500"))
        val result = useCase()
        assertTrue(result.isFailure)
    }
}
