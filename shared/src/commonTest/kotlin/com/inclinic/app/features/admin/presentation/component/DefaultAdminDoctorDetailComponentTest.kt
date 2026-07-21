@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.doctors.application.GetAdminDoctorDetailUseCase
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentFilters
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminBlockedEmailItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminDashboard
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorSpecialty
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorUser
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
import com.inclinic.app.features.admin.patients.application.SuspendUserUseCase
import com.inclinic.app.features.admin.patients.application.UnsuspendUserUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeDoctorDetailAdminDataSource(
    private val initialDetail: AdminDoctorDetail,
) : AdminDataSource {
    var suspendCalls = mutableListOf<Pair<String, String>>()
    var unsuspendCalls = mutableListOf<String>()
    var suspendResult: Result<Unit> = Result.success(Unit)
    var unsuspendResult: Result<Unit> = Result.success(Unit)
    private var currentDetail = initialDetail

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
    override suspend fun getDoctorDetail(id: String): Result<AdminDoctorDetail> = Result.success(currentDetail)
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
    override suspend fun suspendUser(userId: String, reason: String): Result<Unit> {
        suspendCalls.add(userId to reason)
        if (suspendResult.isSuccess) {
            currentDetail = currentDetail.copy(user = currentDetail.user.copy(isSuspended = true, suspensionReason = reason))
        }
        return suspendResult
    }
    override suspend fun unsuspendUser(userId: String): Result<Unit> {
        unsuspendCalls.add(userId)
        if (unsuspendResult.isSuccess) {
            currentDetail = currentDetail.copy(user = currentDetail.user.copy(isSuspended = false, suspensionReason = null))
        }
        return unsuspendResult
    }
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

private fun fakeDoctorDetail(isSuspended: Boolean): AdminDoctorDetail = AdminDoctorDetail(
    id = "doc-1",
    isActive = true,
    isFreelance = false,
    cmpNumber = "12345",
    bio = null,
    rating = null,
    reviewCount = null,
    appointmentCount = null,
    createdAt = null,
    user = AdminDoctorUser(
        id = "user-doc-1",
        firstName = "Ana",
        lastName = "Perez",
        email = "ana@test.com",
        phone = null,
        isSuspended = isSuspended,
        suspendedAt = null,
        suspensionReason = null,
        lastLogin = null,
        createdAt = null,
    ),
    specialties = listOf(AdminDoctorSpecialty("Cardiología")),
)

// ── Tests ─────────────────────────────────────────────────────────────────────

/**
 * RED → GREEN — [DefaultAdminDoctorDetailComponent.onSuspend] / [onUnsuspend] must call the
 * shared [SuspendUserUseCase] / [UnsuspendUserUseCase] with `detail.user.id` (NOT `detail.id`,
 * which is the doctor profile id, not the user id) and refresh [AdminDoctorDetailState.detail]
 * afterwards so the suspended/active UI reflects the new state.
 */
class DefaultAdminDoctorDetailComponentTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : AppDispatchers {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle)

    private fun createComponent(dataSource: FakeDoctorDetailAdminDataSource): DefaultAdminDoctorDetailComponent =
        DefaultAdminDoctorDetailComponent(
            componentContext = ctx,
            doctorId = "doc-1",
            getDetail = GetAdminDoctorDetailUseCase(dataSource, dispatchers),
            suspendUser = SuspendUserUseCase(dataSource, dispatchers),
            unsuspendUser = UnsuspendUserUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

    @Test
    fun onSuspend_calls_useCase_with_userId_and_reloads_detail() = runTest {
        val dataSource = FakeDoctorDetailAdminDataSource(fakeDoctorDetail(isSuspended = false))
        val component = createComponent(dataSource)

        component.onSuspend("Abuso de plataforma — uso indebido")

        assertEquals(listOf("user-doc-1" to "Abuso de plataforma — uso indebido"), dataSource.suspendCalls)
        assertTrue(component.state.value.detail!!.user.isSuspended)
        assertFalse(component.state.value.isSuspending)
        assertNull(component.state.value.suspendError)
    }

    @Test
    fun onUnsuspend_calls_useCase_with_userId_and_reloads_detail() = runTest {
        val dataSource = FakeDoctorDetailAdminDataSource(fakeDoctorDetail(isSuspended = true))
        val component = createComponent(dataSource)

        component.onUnsuspend()

        assertEquals(listOf("user-doc-1"), dataSource.unsuspendCalls)
        assertFalse(component.state.value.detail!!.user.isSuspended)
        assertFalse(component.state.value.isSuspending)
    }

    @Test
    fun onSuspend_failure_sets_suspendError_and_clears_isSuspending() = runTest {
        val dataSource = FakeDoctorDetailAdminDataSource(fakeDoctorDetail(isSuspended = false)).apply {
            suspendResult = Result.failure(RuntimeException("network down"))
        }
        val component = createComponent(dataSource)

        component.onSuspend("Riesgo de pago — deuda")

        assertFalse(component.state.value.isSuspending)
        assertTrue(component.state.value.suspendError != null)
        // Detail should NOT have flipped to suspended on failure.
        assertFalse(component.state.value.detail!!.user.isSuspended)
    }
}
