@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.PackageStatus
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackagesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testPackage(id: String = "pkg-1", status: PackageStatus = PackageStatus.ACTIVE): TherapyPackage = TherapyPackage(
    id = id, offerId = "offer-1", doctorId = "doc-1", name = "Terapia x10",
    totalSessions = 10, completedSessions = 3, pricePerSession = 80.0, totalPrice = 720.0,
    discount = 10, status = status, createdAt = Clock.System.now(),
)

private class FakeTherapyPackagesListDataSource(
    private val packages: List<TherapyPackage> = listOf(testPackage()),
    private val packagesError: Throwable? = null,
) : TherapyPackageDataSource {
    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> =
        if (packagesError != null) Result.failure(packagesError) else Result.success(packages)

    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> =
        Result.success(Pair(testPackage(packageId), emptyList()))

    override suspend fun getOffers(doctorId: String?): Result<List<TherapyOffer>> = Result.success(emptyList())

    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> =
        Result.failure(UnsupportedOperationException())

    override suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation> =
        Result.failure(UnsupportedOperationException())

    override suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?> =
        Result.failure(UnsupportedOperationException())

    override suspend fun purchasePackage(offerId: String): Result<String> =
        Result.failure(UnsupportedOperationException())
    override suspend fun getPackageStatement(packageId: String): Result<com.inclinic.app.core.model.PackageStatement> =
        Result.failure(UnsupportedOperationException())
    override suspend fun payPackageInstallment(packageId: String, amount: Double): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

class DefaultTherapyPackagesListComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: TherapyPackageDataSource = FakeTherapyPackagesListDataSource(),
        outputs: MutableList<TherapyPackagesListComponent.Output> = mutableListOf(),
    ): DefaultTherapyPackagesListComponent {
        return DefaultTherapyPackagesListComponent(
            componentContext = ctx,
            patientId = "pat-1",
            getTherapyPackages = GetTherapyPackagesUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_packages_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.packages.size)
        assertEquals("pkg-1", state.packages.first().id)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val component = createComponent(
            dataSource = FakeTherapyPackagesListDataSource(packagesError = Exception("Server error")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.packages.isEmpty())
        assertNotNull(state.error)
    }

    @Test
    fun initial_state_has_ACTIVE_tab_selected() = runTest {
        val component = createComponent()

        assertEquals(PackagesTab.ACTIVE, component.state.value.selectedTab)
    }

    @Test
    fun onTabChange_updates_selectedTab() = runTest {
        val component = createComponent()

        component.onTabChange(PackagesTab.PENDING_PAYMENT)

        assertEquals(PackagesTab.PENDING_PAYMENT, component.state.value.selectedTab)
    }

    @Test
    fun onTabChange_HISTORY_filters_completed_cancelled_expired() = runTest {
        val packages = listOf(
            testPackage("p1", PackageStatus.ACTIVE),
            testPackage("p2", PackageStatus.COMPLETED),
            testPackage("p3", PackageStatus.CANCELLED),
            testPackage("p4", PackageStatus.EXPIRED),
        )
        val component = createComponent(dataSource = FakeTherapyPackagesListDataSource(packages = packages))

        component.onTabChange(PackagesTab.HISTORY)

        val state = component.state.value
        assertEquals(PackagesTab.HISTORY, state.selectedTab)
        assertEquals(3, state.packages.size)
        assertTrue(state.packages.none { it.status == PackageStatus.ACTIVE })
    }

    @Test
    fun onPackageTapped_emits_NavigateToPackageDetail_with_correct_id() = runTest {
        val outputs = mutableListOf<TherapyPackagesListComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onPackageTapped("pkg-99")

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is TherapyPackagesListComponent.Output.NavigateToPackageDetail)
        assertEquals("pkg-99", (output as TherapyPackagesListComponent.Output.NavigateToPackageDetail).packageId)
    }

    @Test
    fun onBuyPackage_emits_NavigateToOffers() = runTest {
        val outputs = mutableListOf<TherapyPackagesListComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBuyPackage()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is TherapyPackagesListComponent.Output.NavigateToOffers)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(
            dataSource = FakeTherapyPackagesListDataSource(packagesError = Exception("Fail")),
        )
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }
}
