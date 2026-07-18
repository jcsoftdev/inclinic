@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.navigation.AdminConfig
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentFilters
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminBlockedEmailItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminDashboard
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminFinance
import com.inclinic.app.features.admin.infrastructure.remote.AdminNoShowItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminPendingDoctor
import com.inclinic.app.features.admin.infrastructure.remote.AdminReportItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminReviewItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminSpecialtyItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminSpecialtyRequestItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminSubscriptionsOverview
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorSetup
import com.inclinic.app.features.admin.infrastructure.remote.TwoFactorStatus
import com.inclinic.app.features.admin.reports.application.ResolveReportUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeResolveReportAdminDataSource : AdminDataSource {
    val resolveCalls = mutableListOf<Triple<String, String, String?>>()
    var resolveResult: Result<Unit> = Result.success(Unit)

    override suspend fun getTwoFactorStatus(): Result<TwoFactorStatus> = Result.failure(NotImplementedError())
    override suspend fun setupTwoFactor(): Result<TwoFactorSetup> = Result.failure(NotImplementedError())
    override suspend fun enableTwoFactor(code: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun disableTwoFactor(code: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun getDashboard(): Result<AdminDashboard> = Result.success(AdminDashboard())
    override suspend fun getFinance(): Result<AdminFinance> = Result.success(AdminFinance())
    override suspend fun getAppointments(filters: AdminAppointmentFilters): Result<List<AdminAppointmentListItem>> = Result.success(emptyList())
    override suspend fun getAppointmentDetail(id: String): Result<AdminAppointmentDetail> = Result.failure(NotImplementedError())
    override suspend fun getDoctors(status: String?, q: String?): Result<List<AdminDoctorListItem>> = Result.success(emptyList())
    override suspend fun getPendingDoctors(): Result<List<AdminPendingDoctor>> = Result.success(emptyList())
    override suspend fun getPendingDoctorById(id: String): Result<AdminPendingDoctor> = Result.failure(NotImplementedError())
    override suspend fun getDoctorDetail(id: String): Result<AdminDoctorDetail> = Result.failure(NotImplementedError())
    override suspend fun approveDoctor(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun rejectDoctor(id: String, reason: String): Result<Unit> = Result.success(Unit)
    override suspend fun getDisputes(status: String?): Result<List<AdminDisputeItem>> = Result.success(emptyList())
    override suspend fun resolveDispute(id: String, resolution: String, resolutionNote: String): Result<Unit> = Result.success(Unit)
    override suspend fun getNoShows(): Result<List<AdminNoShowItem>> = Result.success(emptyList())
    override suspend fun resolveNoShow(id: String, resolution: String, note: String): Result<Unit> = Result.success(Unit)
    override suspend fun getSpecialties(): Result<List<AdminSpecialtyItem>> = Result.success(emptyList())
    override suspend fun createSpecialty(name: String, description: String?, icon: String?): Result<AdminSpecialtyItem> = Result.failure(NotImplementedError())
    override suspend fun getSpecialtyRequests(): Result<List<AdminSpecialtyRequestItem>> = Result.success(emptyList())
    override suspend fun resolveSpecialtyRequest(requestId: String, action: String, reason: String?): Result<Unit> = Result.success(Unit)
    override suspend fun getPatients(status: String?, q: String?): Result<List<AdminPatientListItem>> = Result.success(emptyList())
    override suspend fun suspendUser(userId: String, reason: String): Result<Unit> = Result.success(Unit)
    override suspend fun unsuspendUser(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getReports(status: String?): Result<List<AdminReportItem>> = Result.success(emptyList())
    override suspend fun resolveReport(reportId: String, status: String, adminNote: String?): Result<Unit> {
        resolveCalls.add(Triple(reportId, status, adminNote))
        return resolveResult
    }
    override suspend fun getReviews(withComment: Boolean?, hidden: Boolean?): Result<List<AdminReviewItem>> = Result.success(emptyList())
    override suspend fun hideReview(appointmentId: String, reason: String): Result<Unit> = Result.success(Unit)
    override suspend fun unhideReview(appointmentId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getBlockedEmails(): Result<List<AdminBlockedEmailItem>> = Result.success(emptyList())
    override suspend fun blockEmail(email: String, reason: String, durationDays: Int?): Result<Unit> = Result.success(Unit)
    override suspend fun unblockEmail(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun getSubscriptions(): Result<AdminSubscriptionsOverview> = Result.success(AdminSubscriptionsOverview())
    override suspend fun setUserSubscription(userId: String, tier: String, expiresAt: String?): Result<Unit> = Result.success(Unit)
    override suspend fun exportFinanceCsv(): Result<ByteArray> = Result.success(byteArrayOf())
}

private fun fakeConfig() = AdminConfig.MasResolveReport(
    reportId = "report-1",
    reportStatus = "PENDING",
    category = "abuse",
    reason = "Comportamiento inapropiado",
    reportedUserFirstName = "Ana",
    reportedUserLastName = "Perez",
    reportedUserRole = "DOCTOR",
    createdAt = null,
)

// ── Tests ─────────────────────────────────────────────────────────────────────

/**
 * RED → GREEN — the top-bar overflow menu on [com.inclinic.app.features.admin.presentation.ui.AdminResolveReportScreen]
 * wires "Descartar" to [DefaultAdminResolveReportComponent.onQuickDismiss] (same status as the
 * Dismissed decision card) and "Escalar" to [DefaultAdminResolveReportComponent.onEscalate]
 * (status = "ESCALATED", best-guess pending backend confirmation).
 */
class DefaultAdminResolveReportComponentTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : AppDispatchers {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle)

    private fun createComponent(
        dataSource: FakeResolveReportAdminDataSource,
        outputs: MutableList<AdminResolveReportComponent.Output> = mutableListOf(),
    ): DefaultAdminResolveReportComponent = DefaultAdminResolveReportComponent(
        componentContext = ctx,
        config = fakeConfig(),
        resolveReport = ResolveReportUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun onQuickDismiss_resolves_with_dismissed_status_and_selects_the_card() = runTest {
        val dataSource = FakeResolveReportAdminDataSource()
        val outputs = mutableListOf<AdminResolveReportComponent.Output>()
        val component = createComponent(dataSource, outputs)

        component.onQuickDismiss()

        assertEquals(1, dataSource.resolveCalls.size)
        assertEquals("report-1", dataSource.resolveCalls.single().first)
        assertEquals(ReportDecision.Dismissed.apiStatus, dataSource.resolveCalls.single().second)
        assertEquals(ReportDecision.Dismissed, component.state.value.selectedDecision)
        assertTrue(outputs.contains(AdminResolveReportComponent.Output.ResolvedSuccess))
    }

    @Test
    fun onEscalate_resolves_with_ESCALATED_status() = runTest {
        val dataSource = FakeResolveReportAdminDataSource()
        val outputs = mutableListOf<AdminResolveReportComponent.Output>()
        val component = createComponent(dataSource, outputs)

        component.onEscalate()

        assertEquals(1, dataSource.resolveCalls.size)
        assertEquals("ESCALATED", dataSource.resolveCalls.single().second)
        assertTrue(outputs.contains(AdminResolveReportComponent.Output.ResolvedSuccess))
    }

    @Test
    fun onEscalate_failure_sets_submitError_and_does_not_emit_success() = runTest {
        val dataSource = FakeResolveReportAdminDataSource().apply {
            resolveResult = Result.failure(RuntimeException("boom"))
        }
        val outputs = mutableListOf<AdminResolveReportComponent.Output>()
        val component = createComponent(dataSource, outputs)

        component.onEscalate()

        assertFalse(outputs.contains(AdminResolveReportComponent.Output.ResolvedSuccess))
        assertTrue(component.state.value.submitError != null)
        assertFalse(component.state.value.isSubmitting)
    }
}
