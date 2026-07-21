@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.PackageStatus
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeTherapyPackageDataSource : TherapyPackageDataSource {
    var packagesResult: Result<List<TherapyPackage>> = Result.success(emptyList())
    var lastPatientId: String? = null
    var lastStatus: String? = null
    var callCount = 0

    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> {
        callCount++
        lastPatientId = patientId
        lastStatus = status
        return packagesResult
    }

    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> = Result.failure(UnsupportedOperationException())
    override suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>> = Result.failure(UnsupportedOperationException())
    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> = Result.failure(UnsupportedOperationException())
    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> = Result.failure(UnsupportedOperationException())
    override suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation> = Result.failure(UnsupportedOperationException())
    override suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?> = Result.failure(UnsupportedOperationException())
    override suspend fun purchasePackage(offerId: String): Result<String> = Result.failure(UnsupportedOperationException())
    override suspend fun getPackageStatement(packageId: String): Result<com.inclinic.app.core.model.PackageStatement> =
        Result.failure(UnsupportedOperationException())
    override suspend fun payPackageInstallment(packageId: String, amount: Double): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

class GetTherapyPackagesUseCaseTest {

    private val fake = FakeTherapyPackageDataSource()
    private val useCase = GetTherapyPackagesUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_forwards_patientId_and_status() = runTest {
        val packages = listOf(
            TherapyPackage(
                id = "pkg-1", offerId = "off-1", doctorId = "doc-1", name = "Nutrición 8 sesiones",
                totalSessions = 8, completedSessions = 2, pricePerSession = 100.0, totalPrice = 720.0,
                discount = 10, status = PackageStatus.ACTIVE, createdAt = Clock.System.now(),
            )
        )
        fake.packagesResult = Result.success(packages)

        val result = useCase("pat-1", "ACTIVE")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("pat-1", fake.lastPatientId)
        assertEquals("ACTIVE", fake.lastStatus)
    }

    @Test
    fun success_with_null_status() = runTest {
        fake.packagesResult = Result.success(emptyList())

        val result = useCase("pat-1", null)

        assertTrue(result.isSuccess)
        assertEquals(null, fake.lastStatus)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.packagesResult = Result.failure(Exception("Server error"))

        val result = useCase("pat-1")

        assertTrue(result.isFailure)
    }
}
