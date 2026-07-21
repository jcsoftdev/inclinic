@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.ApiError
import com.inclinic.app.core.util.DetailLoadState
import com.inclinic.app.features.admin.disputes.application.GetDisputesUseCase
import com.inclinic.app.features.admin.disputes.application.GetNoShowsUseCase
import com.inclinic.app.features.admin.disputes.application.ResolveDisputeUseCase
import com.inclinic.app.features.admin.disputes.application.ResolveNoShowUseCase
import com.inclinic.app.features.admin.doctors.application.ApproveDoctorUseCase
import com.inclinic.app.features.admin.doctors.application.GetAdminDoctorDetailUseCase
import com.inclinic.app.features.admin.doctors.application.GetPendingDoctorByIdUseCase
import com.inclinic.app.features.admin.doctors.application.RejectDoctorUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * RED → GREEN — the 5 admin detail/resolve components must flag `notFound` distinctly from a
 * generic load failure so the screens can render [com.inclinic.app.ui.atoms.DetailErrorState]
 * with the right copy (gap 4 of the design-gap-closure pass). Covers the pure wiring: a 404
 * ([ApiError.NotFound], which [com.inclinic.app.core.error.isNotFoundError] also recognizes for
 * raw Ktor `ClientRequestException`s not exercised here) sets `notFound = true`; any other
 * failure leaves it `false`.
 */
class AdminDetailNotFoundStateTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : AppDispatchers {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private fun ctx() = DefaultComponentContext(LifecycleRegistry())

    private class FakeAdminDataSource(
        var appointmentDetailResult: Result<AdminAppointmentDetail> = Result.failure(NotImplementedError()),
        var doctorDetailResult: Result<AdminDoctorDetail> = Result.failure(NotImplementedError()),
        var pendingDoctorResult: Result<AdminPendingDoctor> = Result.failure(NotImplementedError()),
        var disputesResult: Result<List<AdminDisputeItem>> = Result.success(emptyList()),
        var noShowsResult: Result<List<AdminNoShowItem>> = Result.success(emptyList()),
    ) : AdminDataSource {
        override suspend fun getTwoFactorStatus(): Result<TwoFactorStatus> = Result.failure(NotImplementedError())
        override suspend fun setupTwoFactor(): Result<TwoFactorSetup> = Result.failure(NotImplementedError())
        override suspend fun enableTwoFactor(code: String): Result<Unit> = Result.failure(NotImplementedError())
        override suspend fun disableTwoFactor(code: String): Result<Unit> = Result.failure(NotImplementedError())
        override suspend fun getDashboard(): Result<AdminDashboard> = Result.success(AdminDashboard())
        override suspend fun getFinance(): Result<AdminFinance> = Result.success(AdminFinance())
        override suspend fun getAppointments(filters: AdminAppointmentFilters): Result<List<AdminAppointmentListItem>> = Result.success(emptyList())
        override suspend fun getAppointmentDetail(id: String): Result<AdminAppointmentDetail> = appointmentDetailResult
        override suspend fun getDoctors(status: String?, q: String?): Result<List<AdminDoctorListItem>> = Result.success(emptyList())
        override suspend fun getPendingDoctors(): Result<List<AdminPendingDoctor>> = Result.success(emptyList())
        override suspend fun getPendingDoctorById(id: String): Result<AdminPendingDoctor> = pendingDoctorResult
        override suspend fun getDoctorDetail(id: String): Result<AdminDoctorDetail> = doctorDetailResult
        override suspend fun approveDoctor(id: String): Result<Unit> = Result.success(Unit)
        override suspend fun rejectDoctor(id: String, reason: String): Result<Unit> = Result.success(Unit)
        override suspend fun getDisputes(status: String?): Result<List<AdminDisputeItem>> = disputesResult
        override suspend fun resolveDispute(id: String, resolution: String, resolutionNote: String): Result<Unit> = Result.success(Unit)
        override suspend fun getNoShows(): Result<List<AdminNoShowItem>> = noShowsResult
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

    // ── AdminAppointmentDetail ───────────────────────────────────────────────

    @Test
    fun appointmentDetail_404_sets_notFound_true() = runTest {
        val ds = FakeAdminDataSource(appointmentDetailResult = Result.failure(ApiError.NotFound))
        val component = DefaultAdminAppointmentDetailComponent(
            componentContext = ctx(),
            appointmentId = "appt-1",
            getDetail = GetAdminAppointmentDetailUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        assertTrue(component.state.value.notFound)
        assertTrue(component.state.value.toDetailLoadState() is DetailLoadState.NotFound)
    }

    @Test
    fun appointmentDetail_generic_failure_leaves_notFound_false() = runTest {
        val ds = FakeAdminDataSource(appointmentDetailResult = Result.failure(RuntimeException("boom")))
        val component = DefaultAdminAppointmentDetailComponent(
            componentContext = ctx(),
            appointmentId = "appt-1",
            getDetail = GetAdminAppointmentDetailUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        assertFalse(component.state.value.notFound)
        assertTrue(component.state.value.toDetailLoadState() is DetailLoadState.Failed)
    }

    // ── AdminDoctorDetail ─────────────────────────────────────────────────────

    @Test
    fun doctorDetail_404_sets_notFound_true() = runTest {
        val ds = FakeAdminDataSource(doctorDetailResult = Result.failure(ApiError.NotFound))
        val component = DefaultAdminDoctorDetailComponent(
            componentContext = ctx(),
            doctorId = "doc-1",
            getDetail = GetAdminDoctorDetailUseCase(ds, dispatchers),
            suspendUser = com.inclinic.app.features.admin.patients.application.SuspendUserUseCase(ds, dispatchers),
            unsuspendUser = com.inclinic.app.features.admin.patients.application.UnsuspendUserUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        assertTrue(component.state.value.notFound)
        assertTrue(component.state.value.toDetailLoadState() is DetailLoadState.NotFound)
    }

    // ── AdminPendingDoctorDetail ──────────────────────────────────────────────

    @Test
    fun pendingDoctorDetail_404_sets_notFound_true() = runTest {
        val ds = FakeAdminDataSource(pendingDoctorResult = Result.failure(ApiError.NotFound))
        val component = DefaultAdminPendingDoctorDetailComponent(
            componentContext = ctx(),
            doctorId = "doc-1",
            getPendingDoctorById = GetPendingDoctorByIdUseCase(ds, dispatchers),
            approveDoctor = ApproveDoctorUseCase(ds, dispatchers),
            rejectDoctor = RejectDoctorUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        assertTrue(component.state.value.notFound)
        assertTrue(component.state.value.toDetailLoadState() is DetailLoadState.NotFound)
    }

    // ── AdminResolveDispute ───────────────────────────────────────────────────

    @Test
    fun resolveDispute_missing_from_list_sets_notFound_true() = runTest {
        val ds = FakeAdminDataSource(disputesResult = Result.success(emptyList()))
        val component = DefaultAdminResolveDisputeComponent(
            componentContext = ctx(),
            disputeId = "dispute-1",
            getDisputes = GetDisputesUseCase(ds, dispatchers),
            resolveDispute = ResolveDisputeUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        assertTrue(component.state.value.notFound)
        assertTrue(component.state.value.toDetailLoadState() is DetailLoadState.NotFound)
    }

    @Test
    fun resolveDispute_generic_failure_leaves_notFound_false() = runTest {
        val ds = FakeAdminDataSource(disputesResult = Result.failure(RuntimeException("boom")))
        val component = DefaultAdminResolveDisputeComponent(
            componentContext = ctx(),
            disputeId = "dispute-1",
            getDisputes = GetDisputesUseCase(ds, dispatchers),
            resolveDispute = ResolveDisputeUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        assertFalse(component.state.value.notFound)
        assertTrue(component.state.value.toDetailLoadState() is DetailLoadState.Failed)
    }

    // ── AdminResolveNoShow ────────────────────────────────────────────────────

    @Test
    fun resolveNoShow_missing_from_list_sets_notFound_true() = runTest {
        val ds = FakeAdminDataSource(noShowsResult = Result.success(emptyList()))
        val component = DefaultAdminResolveNoShowComponent(
            componentContext = ctx(),
            noShowId = "noshow-1",
            getNoShows = GetNoShowsUseCase(ds, dispatchers),
            resolveNoShow = ResolveNoShowUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        assertTrue(component.state.value.notFound)
        assertTrue(component.state.value.toDetailLoadState() is DetailLoadState.NotFound)
    }
}
