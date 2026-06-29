package com.inclinic.app.features.doctor.negotiation.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.negotiation.core.model.NegotiationAction
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiationStatus
import com.inclinic.app.features.doctor.negotiation.fakes.FakeDoctorNegotiationRepository
import com.inclinic.app.features.doctor.negotiation.fakes.stubNegotiation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RespondPackageNegotiationUseCaseTest {

    private val repo = FakeDoctorNegotiationRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RespondPackageNegotiationUseCase(repo, dispatchers)

    @Test
    fun returns_accepted_negotiation_on_accept() = runTest {
        repo.respondResult = Result.success(stubNegotiation("neg-1", PackageNegotiationStatus.ACCEPTED))
        val result = useCase("neg-1", NegotiationAction.ACCEPT, null)
        assertTrue(result.isSuccess)
        assertEquals(PackageNegotiationStatus.ACCEPTED, result.getOrThrow().status)
    }

    @Test
    fun accept_passes_null_counter_price() = runTest {
        useCase("neg-1", NegotiationAction.ACCEPT, null)
        assertEquals(NegotiationAction.ACCEPT, repo.lastRespondAction)
        assertNull(repo.lastRespondCounterPriceCents)
    }

    @Test
    fun reject_passes_reject_action() = runTest {
        useCase("neg-1", NegotiationAction.REJECT, null)
        assertEquals(NegotiationAction.REJECT, repo.lastRespondAction)
    }

    @Test
    fun counter_passes_counter_action_and_price() = runTest {
        useCase("neg-7", NegotiationAction.COUNTER, 11000)
        assertEquals("neg-7", repo.lastRespondId)
        assertEquals(NegotiationAction.COUNTER, repo.lastRespondAction)
        assertEquals(11000, repo.lastRespondCounterPriceCents)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.respondResult = Result.failure(RuntimeException("500"))
        val result = useCase("neg-1", NegotiationAction.ACCEPT, null)
        assertTrue(result.isFailure)
    }
}
