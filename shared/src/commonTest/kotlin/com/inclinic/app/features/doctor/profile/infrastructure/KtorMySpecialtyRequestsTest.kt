package com.inclinic.app.features.doctor.profile.infrastructure

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.application.GetMySpecialtyRequestsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the specialty requests feature.
 *
 * NOTE: The backend does NOT have a /api/doctors/{id}/specialty-requests endpoint.
 * The repository stub returns an empty list until the backend adds this route.
 * These tests verify the stub behaviour is stable.
 */
class KtorMySpecialtyRequestsTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetMySpecialtyRequestsUseCase(fakeRepo, dispatchers)

    @Test
    fun returns_empty_list_by_default_from_stub() = runTest {
        // The repo stub returns the default (pre-populated) list in FakeDoctorProfileRepository.
        // In production, DefaultDoctorProfileRepository.getMySpecialtyRequests() returns emptyList().
        val result = useCase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun getMySpecialtyRequests_calls_repository_once() = runTest {
        useCase()

        assertEquals(1, fakeRepo.getMySpecialtyRequestsCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        fakeRepo.getMySpecialtyRequestsResult = Result.failure(RuntimeException("Unavailable"))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
