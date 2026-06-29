package com.inclinic.app.features.doctor.modality.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.modality.fakes.FakeModalityRequestRepository
import com.inclinic.app.features.doctor.modality.fakes.stubModalityRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetModalityChangeRequestUseCaseTest {

    private val repo = FakeModalityRequestRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetModalityChangeRequestUseCase(repo, dispatchers)

    @Test
    fun returns_request_on_success() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-7"))
        val result = useCase("req-7")
        assertTrue(result.isSuccess)
        assertEquals("req-7", result.getOrThrow().id)
    }

    @Test
    fun passes_correct_id_to_repository() = runTest {
        useCase("req-42")
        assertEquals("req-42", repo.lastGetId)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.getRequestResult = Result.failure(RuntimeException("404"))
        val result = useCase("req-1")
        assertTrue(result.isFailure)
    }
}
