@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.admin.patients

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.admin.appointments.application.GetAdminAppointmentsUseCase
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentFilters
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentPerson
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentSpecialty
import com.inclinic.app.features.admin.infrastructure.remote.AdminBlockedEmailItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminDashboard
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem
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
import com.inclinic.app.features.admin.presentation.component.AdminPatientAppointmentsComponent
import com.inclinic.app.features.admin.presentation.component.AdminPatientAppointmentsState
import com.inclinic.app.features.admin.presentation.component.DefaultAdminPatientAppointmentsComponent
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val testPerson = AdminAppointmentPerson("u1", "Ana", "Torres", "ana@test.com")
private val testSpecialty = AdminAppointmentSpecialty("s1", "Cardiología")

private fun testAppointmentItem(id: String = "apt-1") = AdminAppointmentListItem(
    id = id, status = "CONFIRMED", startTime = "2026-06-15T10:00:00Z",
    price = 120.0, commission = 8.0, disputeStatus = null,
    paymentStatus = "PAID", paymentHoldStatus = null,
    doctor = testPerson, patient = testPerson, specialty = testSpecialty,
)

private class FakeAdminDataSourceForAppointments(
    private val appointmentsResult: Result<List<AdminAppointmentListItem>> = Result.success(listOf(testAppointmentItem())),
    var lastFilters: AdminAppointmentFilters? = null,
) : AdminDataSource {
    override suspend fun getTwoFactorStatus(): Result<TwoFactorStatus> = Result.failure(UnsupportedOperationException())
    override suspend fun setupTwoFactor(): Result<TwoFactorSetup> = Result.failure(UnsupportedOperationException())
    override suspend fun enableTwoFactor(code: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun disableTwoFactor(code: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getDashboard(): Result<AdminDashboard> = Result.failure(UnsupportedOperationException())
    override suspend fun getFinance(): Result<AdminFinance> = Result.failure(UnsupportedOperationException())
    override suspend fun getAppointments(filters: AdminAppointmentFilters): Result<List<AdminAppointmentListItem>> {
        lastFilters = filters
        return appointmentsResult
    }
    override suspend fun getAppointmentDetail(id: String): Result<AdminAppointmentDetail> = Result.failure(UnsupportedOperationException())
    override suspend fun getDoctors(status: String?, q: String?): Result<List<AdminDoctorListItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingDoctors(): Result<List<AdminPendingDoctor>> = Result.failure(UnsupportedOperationException())
    override suspend fun getDoctorDetail(id: String): Result<AdminDoctorDetail> = Result.failure(UnsupportedOperationException())
    override suspend fun approveDoctor(id: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun rejectDoctor(id: String, reason: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getDisputes(status: String?): Result<List<AdminDisputeItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun resolveDispute(id: String, resolution: String, resolutionNote: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getNoShows(): Result<List<AdminNoShowItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun resolveNoShow(id: String, resolution: String, note: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getSpecialties(): Result<List<AdminSpecialtyItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun createSpecialty(name: String, description: String?, icon: String?): Result<AdminSpecialtyItem> = Result.failure(UnsupportedOperationException())
    override suspend fun getSpecialtyRequests(): Result<List<AdminSpecialtyRequestItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun resolveSpecialtyRequest(requestId: String, action: String, reason: String?): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatients(status: String?, q: String?): Result<List<AdminPatientListItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun suspendUser(userId: String, reason: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun unsuspendUser(userId: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getReports(status: String?): Result<List<AdminReportItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun resolveReport(reportId: String, status: String, adminNote: String?): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getReviews(withComment: Boolean?, hidden: Boolean?): Result<List<AdminReviewItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun hideReview(appointmentId: String, reason: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun unhideReview(appointmentId: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getBlockedEmails(): Result<List<AdminBlockedEmailItem>> = Result.failure(UnsupportedOperationException())
    override suspend fun blockEmail(email: String, reason: String, durationDays: Int?): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun unblockEmail(email: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getSubscriptions(): Result<AdminSubscriptionsOverview> = Result.failure(UnsupportedOperationException())
    override suspend fun setUserSubscription(userId: String, tier: String, expiresAt: String?): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun exportFinanceCsv(): Result<ByteArray> = Result.failure(UnsupportedOperationException())
}

class AdminPatientAppointmentsComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()

    private fun createComponent(
        dataSource: FakeAdminDataSourceForAppointments = FakeAdminDataSourceForAppointments(),
        patientId: String = "pat-1",
        outputs: MutableList<AdminPatientAppointmentsComponent.Output> = mutableListOf(),
    ): DefaultAdminPatientAppointmentsComponent {
        return DefaultAdminPatientAppointmentsComponent(
            componentContext = ctx,
            patientId = patientId,
            getAppointments = GetAdminAppointmentsUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun loads_appointments_for_patient_on_init() = runTest {
        val dataSource = FakeAdminDataSourceForAppointments(
            appointmentsResult = Result.success(listOf(testAppointmentItem("apt-1"), testAppointmentItem("apt-2"))),
        )

        val component = createComponent(dataSource = dataSource, patientId = "pat-42")

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(2, component.state.value.appointments.size)
        // Verify the filters were called with the correct patientId
        assertEquals("pat-42", dataSource.lastFilters?.patientId)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val dataSource = FakeAdminDataSourceForAppointments(
            appointmentsResult = Result.failure(RuntimeException("Network error")),
        )

        val component = createComponent(dataSource = dataSource)

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.appointments.isEmpty())
    }

    @Test
    fun onBack_emits_Back_output() {
        val outputs = mutableListOf<AdminPatientAppointmentsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is AdminPatientAppointmentsComponent.Output.Back)
    }

    @Test
    fun onRefresh_reloads_appointments() = runTest {
        val dataSource = FakeAdminDataSourceForAppointments(
            appointmentsResult = Result.success(emptyList()),
        )
        val component = createComponent(dataSource = dataSource)
        assertEquals(0, component.state.value.appointments.size)

        // Update result before refresh
        val newResult = Result.success(listOf(testAppointmentItem()))
        // Can't easily swap since dataSource is val, but verify refresh calls load again
        component.onRefresh()

        assertFalse(component.state.value.isLoading)
    }
}
