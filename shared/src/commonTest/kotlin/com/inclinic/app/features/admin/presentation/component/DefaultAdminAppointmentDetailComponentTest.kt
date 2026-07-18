@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.appointments.application.GetAdminAppointmentDetailUseCase
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentFilters
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentPerson
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentSpecialty
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeAdminDataSource(
    var detailResult: Result<AdminAppointmentDetail> = Result.failure(NotImplementedError()),
) : AdminDataSource {
    override suspend fun getTwoFactorStatus(): Result<TwoFactorStatus> = Result.failure(NotImplementedError())
    override suspend fun setupTwoFactor(): Result<TwoFactorSetup> = Result.failure(NotImplementedError())
    override suspend fun enableTwoFactor(code: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun disableTwoFactor(code: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun getDashboard(): Result<AdminDashboard> = Result.success(AdminDashboard())
    override suspend fun getFinance(): Result<AdminFinance> = Result.success(AdminFinance())
    override suspend fun getAppointments(filters: AdminAppointmentFilters): Result<List<AdminAppointmentListItem>> = Result.success(emptyList())
    override suspend fun getAppointmentDetail(id: String): Result<AdminAppointmentDetail> = detailResult
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
    override suspend fun resolveReport(reportId: String, status: String, adminNote: String?): Result<Unit> = Result.success(Unit)
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

private fun fakeDetail(hasDispute: Boolean): AdminAppointmentDetail = AdminAppointmentDetail(
    id = "appt-1",
    status = "COMPLETED",
    startTime = "2026-07-17T10:00:00Z",
    price = 100.0,
    commission = 10.0,
    disputeStatus = if (hasDispute) "OPEN" else null,
    disputeReason = if (hasDispute) "No-show" else null,
    paymentStatus = "PAID",
    paymentHoldStatus = null,
    notes = null,
    rescheduleCount = 0,
    doctor = AdminAppointmentPerson("doc-1", "Ana", "Perez", "ana@test.com"),
    patient = AdminAppointmentPerson("pat-1", "Luis", "Gomez", "luis@test.com"),
    specialty = AdminAppointmentSpecialty("spec-1", "Cardiología"),
)

// ── Tests ─────────────────────────────────────────────────────────────────────

/**
 * RED → GREEN — [DefaultAdminAppointmentDetailComponent.onNavigateToResolveDispute] must only
 * emit [AdminAppointmentDetailComponent.Output.NavigateToResolveDispute] when
 * [AdminAppointmentDetail.hasDispute] is true. This is a defense-in-depth guard: the screen
 * already hides the "Resolver disputa" button when there's no dispute, but the component
 * re-checks so a stale/mismatched UI state can never fire the navigation.
 */
class DefaultAdminAppointmentDetailComponentTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : AppDispatchers {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle)

    private fun createComponent(
        dataSource: FakeAdminDataSource,
        outputs: MutableList<AdminAppointmentDetailComponent.Output> = mutableListOf(),
    ): DefaultAdminAppointmentDetailComponent = DefaultAdminAppointmentDetailComponent(
        componentContext = ctx,
        appointmentId = "appt-1",
        getDetail = GetAdminAppointmentDetailUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun onNavigateToResolveDispute_emits_output_when_hasDispute_true() = runTest {
        val outputs = mutableListOf<AdminAppointmentDetailComponent.Output>()
        val component = createComponent(
            dataSource = FakeAdminDataSource(detailResult = Result.success(fakeDetail(hasDispute = true))),
            outputs = outputs,
        )

        component.onNavigateToResolveDispute()

        assertEquals(1, outputs.size)
        assertTrue(outputs.single() is AdminAppointmentDetailComponent.Output.NavigateToResolveDispute)
    }

    @Test
    fun onNavigateToResolveDispute_is_noop_when_hasDispute_false() = runTest {
        val outputs = mutableListOf<AdminAppointmentDetailComponent.Output>()
        val component = createComponent(
            dataSource = FakeAdminDataSource(detailResult = Result.success(fakeDetail(hasDispute = false))),
            outputs = outputs,
        )

        component.onNavigateToResolveDispute()

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onNavigateToResolveDispute_is_noop_when_detail_not_loaded() = runTest {
        val outputs = mutableListOf<AdminAppointmentDetailComponent.Output>()
        val component = createComponent(
            dataSource = FakeAdminDataSource(detailResult = Result.failure(RuntimeException("boom"))),
            outputs = outputs,
        )

        component.onNavigateToResolveDispute()

        assertTrue(outputs.isEmpty())
    }
}
