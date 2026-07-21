@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.PackageStatus
import com.inclinic.app.core.model.SessionStatus
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackageDetailUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testPackage(id: String = "pkg-1", doctorId: String = "doc-1"): TherapyPackage = TherapyPackage(
    id = id, offerId = "offer-1", doctorId = doctorId, name = "Terapia cognitiva x8",
    totalSessions = 8, completedSessions = 2, pricePerSession = 90.0, totalPrice = 648.0,
    discount = 10, status = PackageStatus.ACTIVE, createdAt = Clock.System.now(),
)

private fun testSession(number: Int = 1, status: SessionStatus = SessionStatus.SCHEDULED): PackageSession =
    PackageSession(id = "sess-$number", sessionNumber = number, status = status)

private class FakeTherapyPackageDetailDataSource(
    private val pkg: TherapyPackage = testPackage(),
    private val sessions: List<PackageSession> = listOf(testSession()),
    private val detailError: Throwable? = null,
) : TherapyPackageDataSource {
    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> =
        if (detailError != null) Result.failure(detailError) else Result.success(Pair(pkg, sessions))

    override suspend fun getPatientPackages(patientId: String, status: String?): Result<List<TherapyPackage>> =
        Result.success(emptyList())

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

class DefaultTherapyPackageDetailComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: TherapyPackageDataSource = FakeTherapyPackageDetailDataSource(),
        packageId: String = "pkg-1",
        outputs: MutableList<TherapyPackageDetailComponent.Output> = mutableListOf(),
    ): DefaultTherapyPackageDetailComponent {
        return DefaultTherapyPackageDetailComponent(
            componentContext = ctx,
            packageId = packageId,
            getPackageDetail = GetTherapyPackageDetailUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_package_and_sessions_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.therapyPackage)
        assertEquals("pkg-1", state.therapyPackage?.id)
        assertEquals(1, state.sessions.size)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val component = createComponent(
            dataSource = FakeTherapyPackageDetailDataSource(detailError = Exception("Not found")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.therapyPackage)
        assertNotNull(state.error)
    }

    @Test
    fun initial_state_has_UPCOMING_tab_selected() = runTest {
        val component = createComponent()

        assertEquals(SessionsTab.UPCOMING, component.state.value.selectedTab)
    }

    @Test
    fun onTabChange_updates_selectedTab() = runTest {
        val component = createComponent()

        component.onTabChange(SessionsTab.HISTORY)

        assertEquals(SessionsTab.HISTORY, component.state.value.selectedTab)
    }

    @Test
    fun onScheduleNextSession_emits_NavigateToScheduleSession_with_correct_ids() = runTest {
        val outputs = mutableListOf<TherapyPackageDetailComponent.Output>()
        val component = createComponent(
            dataSource = FakeTherapyPackageDetailDataSource(pkg = testPackage(id = "pkg-1", doctorId = "doc-7")),
            packageId = "pkg-1",
            outputs = outputs,
        )

        component.onScheduleNextSession()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is TherapyPackageDetailComponent.Output.NavigateToScheduleSession)
        val nav = output as TherapyPackageDetailComponent.Output.NavigateToScheduleSession
        assertEquals("pkg-1", nav.packageId)
        assertEquals("doc-7", nav.doctorId)
    }

    @Test
    fun onScheduleNextSession_does_nothing_when_package_not_loaded() = runTest {
        val outputs = mutableListOf<TherapyPackageDetailComponent.Output>()
        val component = createComponent(
            dataSource = FakeTherapyPackageDetailDataSource(detailError = Exception("Fail")),
            outputs = outputs,
        )

        component.onScheduleNextSession()

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<TherapyPackageDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is TherapyPackageDetailComponent.Output.Back)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(
            dataSource = FakeTherapyPackageDetailDataSource(detailError = Exception("Fail")),
        )
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }
}
