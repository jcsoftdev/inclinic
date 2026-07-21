@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.NegotiationProposal
import com.inclinic.app.core.model.NegotiationStatus
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import com.inclinic.app.features.patient.therapy.application.CreateNegotiationUseCase
import com.inclinic.app.features.patient.therapy.application.GetNegotiationUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyOfferDetailUseCase
import com.inclinic.app.features.patient.therapy.application.RespondNegotiationUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testNegotiation(
    id: String = "neg-1",
    status: NegotiationStatus = NegotiationStatus.PENDING_DOCTOR,
    acceptedTherapyPackageId: String? = null,
    finalPricePerSession: Double? = null,
    finalSessions: Int? = null,
): PackageNegotiation =
    PackageNegotiation(
        id = id, offerId = "offer-1", offerName = "Pack 10 sesiones",
        doctorName = "Dr. Paredes", status = status,
        proposals = listOf(
            NegotiationProposal(
                id = "prop-1", proposedBy = "PATIENT", pricePerSession = 80.0,
                sessions = 10, message = null, createdAt = Clock.System.now(),
            ),
        ),
        finalPricePerSession = finalPricePerSession,
        finalSessions = finalSessions,
        acceptedTherapyPackageId = acceptedTherapyPackageId,
    )

private fun testOffer(): TherapyOffer = TherapyOffer(
    id = "offer-1", doctorId = "doc-1", name = "Pack 10 sesiones",
    sessions = 10, pricePerSession = 80.0, isNegotiable = true,
)

private class FakeNegotiationDataSource(
    private var negotiation: PackageNegotiation = testNegotiation(),
    private val negotiationError: Throwable? = null,
    private val offerResult: Result<TherapyOffer> = Result.success(testOffer()),
    private var createResult: Result<PackageNegotiation> = Result.success(testNegotiation()),
    private var respondResult: Result<String?> = Result.success(null),
) : TherapyPackageDataSource {
    var lastAction: String? = null

    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> =
        if (negotiationError != null) Result.failure(negotiationError) else Result.success(negotiation)

    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> = offerResult

    override suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation> =
        createResult

    override suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?> {
        lastAction = action
        return respondResult
    }

    fun setNegotiation(value: PackageNegotiation) { negotiation = value }
    fun setCreateResult(result: Result<PackageNegotiation>) { createResult = result }
    fun setRespondResult(result: Result<String?>) { respondResult = result }

    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> =
        Result.success(emptyList())

    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>> = Result.success(emptyList())

    override suspend fun purchasePackage(offerId: String): Result<String> =
        Result.failure(UnsupportedOperationException())
    override suspend fun getPackageStatement(packageId: String): Result<com.inclinic.app.core.model.PackageStatement> =
        Result.failure(UnsupportedOperationException())
    override suspend fun payPackageInstallment(packageId: String, amount: Double): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

class DefaultNegotiationComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeNegotiationDataSource = FakeNegotiationDataSource(),
        negotiationId: String? = "neg-1",
        offerId: String? = null,
        outputs: MutableList<NegotiationComponent.Output> = mutableListOf(),
    ): DefaultNegotiationComponent {
        return DefaultNegotiationComponent(
            componentContext = ctx,
            negotiationId = negotiationId,
            offerId = offerId,
            getNegotiation = GetNegotiationUseCase(dataSource, dispatchers),
            getOfferDetail = GetTherapyOfferDetailUseCase(dataSource, dispatchers),
            createNegotiation = CreateNegotiationUseCase(dataSource, dispatchers),
            respondNegotiation = RespondNegotiationUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_existing_success_sets_negotiation_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.negotiation)
        assertEquals("neg-1", state.negotiation?.id)
        assertEquals("Dr. Paredes", state.negotiation?.doctorName)
        assertNull(state.error)
    }

    @Test
    fun load_existing_failure_sets_error_in_state() = runTest {
        val component = createComponent(
            dataSource = FakeNegotiationDataSource(negotiationError = Exception("Not found")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.negotiation)
        assertNotNull(state.error)
    }

    @Test
    fun start_mode_prefills_price_and_sessions_from_offer() = runTest {
        val component = createComponent(negotiationId = null, offerId = "offer-1")

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.negotiation)
        assertEquals("80.0", state.proposedPrice)
        assertEquals("10", state.proposedSessions)
    }

    @Test
    fun onSubmitProposal_start_mode_creates_negotiation() = runTest {
        val created = testNegotiation(id = "neg-new")
        val ds = FakeNegotiationDataSource()
        ds.setCreateResult(Result.success(created))
        val component = createComponent(dataSource = ds, negotiationId = null, offerId = "offer-1")
        component.onProposedPriceChange("70")
        component.onProposedSessionsChange("8")

        component.onSubmitProposal()

        val state = component.state.value
        assertFalse(state.isSending)
        assertEquals("neg-new", state.negotiation?.id)
        assertEquals("", state.messageText)
    }

    @Test
    fun onSubmitProposal_rejects_sessions_below_two() = runTest {
        val component = createComponent(negotiationId = null, offerId = "offer-1")
        component.onProposedPriceChange("70")
        component.onProposedSessionsChange("1")

        component.onSubmitProposal()

        assertNotNull(component.state.value.error)
        assertNull(component.state.value.negotiation)
    }

    @Test
    fun onSubmitProposal_view_mode_sends_counter_and_refreshes() = runTest {
        val ds = FakeNegotiationDataSource(negotiation = testNegotiation(status = NegotiationStatus.PENDING_PATIENT))
        ds.setRespondResult(Result.success(null))
        val component = createComponent(dataSource = ds, negotiationId = "neg-1")
        component.onProposedPriceChange("65")
        component.onProposedSessionsChange("12")

        component.onSubmitProposal()

        assertEquals("COUNTER", ds.lastAction)
        assertFalse(component.state.value.isSending)
    }

    @Test
    fun onAccept_sends_ACCEPT_and_refreshes() = runTest {
        val accepted = testNegotiation(
            status = NegotiationStatus.ACCEPTED,
            acceptedTherapyPackageId = "pkg-9",
            finalPricePerSession = 80.0, finalSessions = 10,
        )
        val ds = FakeNegotiationDataSource(negotiation = testNegotiation(status = NegotiationStatus.PENDING_PATIENT))
        ds.setRespondResult(Result.success("pkg-9"))
        val component = createComponent(dataSource = ds, negotiationId = "neg-1")
        ds.setNegotiation(accepted)

        component.onAccept()

        assertEquals("ACCEPT", ds.lastAction)
        assertEquals(NegotiationStatus.ACCEPTED, component.state.value.negotiation?.status)
        assertEquals("pkg-9", component.state.value.negotiation?.acceptedTherapyPackageId)
    }

    @Test
    fun onReject_sends_REJECT() = runTest {
        val ds = FakeNegotiationDataSource(negotiation = testNegotiation(status = NegotiationStatus.PENDING_PATIENT))
        val component = createComponent(dataSource = ds, negotiationId = "neg-1")

        component.onReject()

        assertEquals("REJECT", ds.lastAction)
    }

    @Test
    fun onPay_with_accepted_package_emits_NavigateToPayment() = runTest {
        val ds = FakeNegotiationDataSource(
            negotiation = testNegotiation(status = NegotiationStatus.ACCEPTED, acceptedTherapyPackageId = "pkg-42"),
        )
        val outputs = mutableListOf<NegotiationComponent.Output>()
        val component = createComponent(dataSource = ds, negotiationId = "neg-1", outputs = outputs)

        component.onPay()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is NegotiationComponent.Output.NavigateToPayment)
        assertEquals("pkg-42", (output as NegotiationComponent.Output.NavigateToPayment).therapyPackageId)
    }

    @Test
    fun onPay_without_accepted_package_sets_error() = runTest {
        val outputs = mutableListOf<NegotiationComponent.Output>()
        val component = createComponent(negotiationId = "neg-1", outputs = outputs)

        component.onPay()

        assertTrue(outputs.isEmpty())
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<NegotiationComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is NegotiationComponent.Output.Back)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(
            dataSource = FakeNegotiationDataSource(negotiationError = Exception("Fail")),
        )
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }
}
