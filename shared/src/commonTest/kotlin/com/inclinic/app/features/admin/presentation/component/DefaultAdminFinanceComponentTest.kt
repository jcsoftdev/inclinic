@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.finance.application.ExportFinanceCsvUseCase
import com.inclinic.app.features.admin.finance.application.GetFinanceUseCase
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private open class StubAdminDataSource : AdminDataSource {
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

// ── Tests ─────────────────────────────────────────────────────────────────────

class DefaultAdminFinanceComponentTest {

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : AppDispatchers {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val lifecycle = LifecycleRegistry()
    private val ctx = DefaultComponentContext(lifecycle)

    private fun createComponent(
        dataSource: AdminDataSource = StubAdminDataSource(),
        outputs: MutableList<AdminFinanceComponent.Output> = mutableListOf(),
    ): DefaultAdminFinanceComponent = DefaultAdminFinanceComponent(
        componentContext = ctx,
        getFinance = GetFinanceUseCase(dataSource, dispatchers),
        exportFinanceCsv = ExportFinanceCsvUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    // ── REQ: onExport sets exportBytes on success ─────────────────────────────

    @Test
    fun onExport_success_sets_exportBytes_in_state() = runTest {
        val csvBytes = byteArrayOf(99, 115, 118)
        val ds = object : StubAdminDataSource() {
            override suspend fun exportFinanceCsv(): Result<ByteArray> = Result.success(csvBytes)
        }
        val component = createComponent(dataSource = ds)

        component.onExport()

        assertNotNull(component.state.value.exportBytes)
        assertFalse(component.state.value.isExporting)
        assertNull(component.state.value.exportMessage)
    }

    // ── REQ: onExportHandled clears bytes and sets success message ────────────

    @Test
    fun onExportHandled_clears_exportBytes_and_sets_success_message() = runTest {
        val csvBytes = byteArrayOf(99, 115, 118)
        val ds = object : StubAdminDataSource() {
            override suspend fun exportFinanceCsv(): Result<ByteArray> = Result.success(csvBytes)
        }
        val component = createComponent(dataSource = ds)

        component.onExport()
        assertNotNull(component.state.value.exportBytes) // precondition

        component.onExportHandled()

        assertNull(component.state.value.exportBytes)
        assertNotNull(component.state.value.exportMessage)
    }

    // ── REQ: onExport failure sets exportMessage, no exportBytes ─────────────

    @Test
    fun onExport_failure_sets_exportMessage_without_exportBytes() = runTest {
        val ds = object : StubAdminDataSource() {
            override suspend fun exportFinanceCsv(): Result<ByteArray> = Result.failure(Exception("Network error"))
        }
        val component = createComponent(dataSource = ds)

        component.onExport()

        assertNull(component.state.value.exportBytes)
        assertNotNull(component.state.value.exportMessage)
        assertFalse(component.state.value.isExporting)
    }
}
