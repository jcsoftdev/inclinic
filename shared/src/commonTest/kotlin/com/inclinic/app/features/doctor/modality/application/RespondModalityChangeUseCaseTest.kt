package com.inclinic.app.features.doctor.modality.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.modality.core.model.ModalityRequestStatus
import com.inclinic.app.features.doctor.modality.core.model.ModalityResponseAction
import com.inclinic.app.features.doctor.modality.fakes.FakeModalityRequestRepository
import com.inclinic.app.features.doctor.modality.fakes.stubModalityRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RespondModalityChangeUseCaseTest {

    private val repo = FakeModalityRequestRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RespondModalityChangeUseCase(repo, dispatchers)

    @Test
    fun returns_updated_request_on_approve() = runTest {
        repo.respondResult = Result.success(stubModalityRequest("req-1", ModalityRequestStatus.APPROVED))
        val result = useCase("req-1", ModalityResponseAction.APPROVE)
        assertTrue(result.isSuccess)
        assertEquals(ModalityRequestStatus.APPROVED, result.getOrThrow().status)
    }

    @Test
    fun returns_updated_request_on_reject() = runTest {
        repo.respondResult = Result.success(stubModalityRequest("req-1", ModalityRequestStatus.REJECTED))
        val result = useCase("req-1", ModalityResponseAction.REJECT)
        assertTrue(result.isSuccess)
        assertEquals(ModalityRequestStatus.REJECTED, result.getOrThrow().status)
    }

    @Test
    fun passes_correct_id_and_action_to_repository() = runTest {
        useCase("req-99", ModalityResponseAction.REJECT)
        assertEquals("req-99", repo.lastRespondId)
        assertEquals(ModalityResponseAction.REJECT, repo.lastRespondAction)
    }

    @Test
    fun passes_adjusted_price_to_repository_on_approve() = runTest {
        useCase("req-1", ModalityResponseAction.APPROVE, adjustedPrice = 180)
        assertEquals(180, repo.lastRespondPrice)
    }

    @Test
    fun adjusted_price_defaults_to_null() = runTest {
        useCase("req-1", ModalityResponseAction.APPROVE)
        assertEquals(null, repo.lastRespondPrice)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.respondResult = Result.failure(RuntimeException("500"))
        val result = useCase("req-1", ModalityResponseAction.APPROVE)
        assertTrue(result.isFailure)
    }
}
