package com.inclinic.app.features.doctor.therapy_offers.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.therapy_offers.fakes.FakeDoctorTherapyOffersRepository
import com.inclinic.app.features.doctor.therapy_offers.fakes.offerFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMyTherapyOffersUseCaseTest {

    private val repo = FakeDoctorTherapyOffersRepository()
    private val useCase = GetMyTherapyOffersUseCase(repo, TestAppDispatchers())

    @Test
    fun returns_offers_from_repository() = runTest {
        repo.offersResult = Result.success(listOf(offerFixture("o1"), offerFixture("o2")))
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertEquals("o1", result.getOrThrow()[0].id)
    }

    @Test
    fun returns_empty_list_when_no_offers() = runTest {
        repo.offersResult = Result.success(emptyList())
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().size)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.offersResult = Result.failure(RuntimeException("network error"))
        val result = useCase()
        assertTrue(result.isFailure)
        assertEquals("network error", result.exceptionOrNull()?.message)
    }
}
