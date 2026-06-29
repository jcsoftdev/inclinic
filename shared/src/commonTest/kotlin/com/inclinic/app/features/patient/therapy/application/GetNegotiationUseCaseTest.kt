@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.model.NegotiationStatus
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeGetNegotiationDataSource : TherapyPackageDataSource {
    var negotiationResult: Result<PackageNegotiation> = Result.success(
        PackageNegotiation(id = "neg-1", offerId = "off-1", offerName = "Pack Yoga", status = NegotiationStatus.PENDING_DOCTOR)
    )
    var lastId: String? = null
    var callCount = 0

    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> {
        callCount++
        lastId = negotiationId
        return negotiationResult
    }

    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> = Result.failure(UnsupportedOperationException())
    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> = Result.failure(UnsupportedOperationException())
    override suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>> = Result.failure(UnsupportedOperationException())
    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> = Result.failure(UnsupportedOperationException())
    override suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation> = Result.failure(UnsupportedOperationException())
    override suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?> = Result.failure(UnsupportedOperationException())
    override suspend fun purchasePackage(offerId: String): Result<String> = Result.failure(UnsupportedOperationException())
}

class GetNegotiationUseCaseTest {

    private val fake = FakeGetNegotiationDataSource()
    private val useCase = GetNegotiationUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_negotiation() = runTest {
        val result = useCase("neg-1")

        assertTrue(result.isSuccess)
        assertEquals("neg-1", result.getOrNull()?.id)
        assertEquals("Pack Yoga", result.getOrNull()?.offerName)
        assertEquals("neg-1", fake.lastId)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.negotiationResult = Result.failure(Exception("Not found"))

        val result = useCase("neg-999")

        assertTrue(result.isFailure)
    }
}
