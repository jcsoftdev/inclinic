@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeOffersDataSource : TherapyPackageDataSource {
    var offersResult: Result<List<TherapyOffer>> = Result.success(emptyList())
    var lastDoctorId: String? = null
    var callCount = 0

    override suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>> {
        callCount++
        lastDoctorId = doctorId
        return offersResult
    }

    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> = Result.failure(UnsupportedOperationException())
    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> = Result.failure(UnsupportedOperationException())
    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> = Result.failure(UnsupportedOperationException())
    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> = Result.failure(UnsupportedOperationException())
    override suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation> = Result.failure(UnsupportedOperationException())
    override suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?> = Result.failure(UnsupportedOperationException())
    override suspend fun purchasePackage(offerId: String): Result<String> = Result.failure(UnsupportedOperationException())
}

class GetTherapyOffersUseCaseTest {

    private val fake = FakeOffersDataSource()
    private val useCase = GetTherapyOffersUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_with_doctorId_filter() = runTest {
        val offers = listOf(
            TherapyOffer(id = "off-1", doctorId = "doc-1", name = "Pack 8", sessions = 8, pricePerSession = 90.0),
        )
        fake.offersResult = Result.success(offers)

        val result = useCase("doc-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("doc-1", fake.lastDoctorId)
    }

    @Test
    fun success_with_null_doctorId() = runTest {
        fake.offersResult = Result.success(emptyList())

        val result = useCase(null)

        assertTrue(result.isSuccess)
        assertNull(fake.lastDoctorId)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.offersResult = Result.failure(Exception("Timeout"))

        val result = useCase("doc-1")

        assertTrue(result.isFailure)
    }
}
