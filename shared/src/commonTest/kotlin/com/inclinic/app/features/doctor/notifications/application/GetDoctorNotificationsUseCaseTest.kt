package com.inclinic.app.features.doctor.notifications.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import com.inclinic.app.features.doctor.notifications.fakes.FakeDoctorNotificationsRepository
import com.inclinic.app.features.doctor.notifications.fakes.stubNotification
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDoctorNotificationsUseCaseTest {

    private val repo = FakeDoctorNotificationsRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetDoctorNotificationsUseCase(repo, dispatchers)

    @Test
    fun returns_all_notifications_with_default_filter() = runTest {
        repo.listResult = Result.success(listOf(stubNotification("n1"), stubNotification("n2")))
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertEquals(NotificationFilter.ALL, repo.lastFilter)
    }

    @Test
    fun passes_filter_to_repository() = runTest {
        repo.listResult = Result.success(listOf(stubNotification("n1")))
        useCase(NotificationFilter.APPOINTMENTS)
        assertEquals(NotificationFilter.APPOINTMENTS, repo.lastFilter)
    }

    @Test
    fun returns_empty_list_when_no_notifications() = runTest {
        repo.listResult = Result.success(emptyList())
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun propagates_failure() = runTest {
        repo.listResult = Result.failure(RuntimeException("Network error"))
        val result = useCase()
        assertTrue(result.isFailure)
    }
}
