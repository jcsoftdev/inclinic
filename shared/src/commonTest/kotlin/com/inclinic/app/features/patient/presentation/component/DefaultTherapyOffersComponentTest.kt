@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import com.inclinic.app.features.patient.therapy.application.GetTherapyOffersUseCase
import com.inclinic.app.features.patient.therapy.application.PurchasePackageUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testOffer(id: String = "offer-1"): TherapyOffer = TherapyOffer(
    id = id, doctorId = "doc-1", name = "Pack Terapia 10 sesiones",
    sessions = 10, pricePerSession = 80.0, isNegotiable = true,
)

private class FakeTherapyOffersDataSource(
    private val offers: List<TherapyOffer> = listOf(testOffer()),
    private val offersError: Throwable? = null,
    private val purchaseResult: Result<String> = Result.success("pkg-1"),
) : TherapyPackageDataSource {
    override suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>> =
        if (offersError != null) Result.failure(offersError) else Result.success(offers)

    override suspend fun purchasePackage(offerId: String): Result<String> = purchaseResult

    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> =
        Result.success(emptyList())

    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> =
        Result.failure(UnsupportedOperationException())

    override suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation> =
        Result.failure(UnsupportedOperationException())

    override suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?> =
        Result.failure(UnsupportedOperationException())
}

class DefaultTherapyOffersComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: TherapyPackageDataSource = FakeTherapyOffersDataSource(),
        outputs: MutableList<TherapyOffersComponent.Output> = mutableListOf(),
    ): DefaultTherapyOffersComponent {
        return DefaultTherapyOffersComponent(
            componentContext = ctx,
            getTherapyOffers = GetTherapyOffersUseCase(dataSource, dispatchers),
            purchasePackage = PurchasePackageUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_offers_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.offers.size)
        assertEquals("offer-1", state.offers.first().id)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val component = createComponent(
            dataSource = FakeTherapyOffersDataSource(offersError = Exception("Service unavailable")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.offers.isEmpty())
        assertEquals("Service unavailable", state.error)
    }

    @Test
    fun onNegotiate_emits_StartNegotiation_with_correct_offerId() = runTest {
        val outputs = mutableListOf<TherapyOffersComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onNegotiate("offer-42")

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is TherapyOffersComponent.Output.StartNegotiation)
        assertEquals("offer-42", (output as TherapyOffersComponent.Output.StartNegotiation).offerId)
    }

    @Test
    fun onNegotiationTapped_emits_NavigateToNegotiation_with_correct_id() = runTest {
        val outputs = mutableListOf<TherapyOffersComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onNegotiationTapped("neg-7")

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is TherapyOffersComponent.Output.NavigateToNegotiation)
        assertEquals("neg-7", (output as TherapyOffersComponent.Output.NavigateToNegotiation).negotiationId)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<TherapyOffersComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is TherapyOffersComponent.Output.Back)
    }

    @Test
    fun onOfferTapped_does_not_emit_output() = runTest {
        val outputs = mutableListOf<TherapyOffersComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onOfferTapped("offer-1")

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(
            dataSource = FakeTherapyOffersDataSource(offersError = Exception("Fail")),
        )
        assertEquals("Fail", component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }
}
