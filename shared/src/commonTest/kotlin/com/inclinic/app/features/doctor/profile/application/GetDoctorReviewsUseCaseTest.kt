package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDoctorReviewsUseCaseTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetDoctorReviewsUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_reviews_page_on_success() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
        val page = result.getOrThrow()
        assertEquals(4.8, page.averageRating)
        assertEquals(2, page.reviews.size)
    }

    @Test
    fun passes_limit_to_repository() = runTest {
        useCase(limit = 10)

        assertEquals(10, fakeRepo.lastReviewsLimit)
    }

    @Test
    fun default_limit_is_20() = runTest {
        useCase()

        assertEquals(20, fakeRepo.lastReviewsLimit)
    }

    @Test
    fun propagates_failure() = runTest {
        fakeRepo.getReviewsResult = Result.failure(RuntimeException("Network error"))

        val result = useCase()

        assertTrue(result.isFailure)
    }

    @Test
    fun calls_repository_exactly_once() = runTest {
        useCase()

        assertEquals(1, fakeRepo.getReviewsCallCount)
    }
}
