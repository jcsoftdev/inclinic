package com.inclinic.app.features.doctor.negotiation.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.negotiation.fakes.FakeDoctorNegotiationRepository
import com.inclinic.app.features.doctor.negotiation.fakes.stubNegotiation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPackageNegotiationUseCaseTest {

    private val repo = FakeDoctorNegotiationRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetPackageNegotiationUseCase(repo, dispatchers)

    @Test
    fun returns_negotiation_on_success() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        val result = useCase("neg-1")
        assertTrue(result.isSuccess)
        assertEquals("neg-1", result.getOrThrow().id)
    }

    @Test
    fun passes_correct_id_to_repository() = runTest {
        useCase("neg-99")
        assertEquals("neg-99", repo.lastGetId)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.getResult = Result.failure(RuntimeException("404"))
        val result = useCase("neg-1")
        assertTrue(result.isFailure)
    }
}
