@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.dashboard.application.GetDoctorDashboardUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class StubDashboardDataSource(
    private val dashboard: DoctorDashboard,
) : DoctorAppointmentDataSource {
    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> = Result.success(dashboard)
    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> = Result.success(emptyList())
    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> = Result.success(emptyList())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun markNoShow(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getNoShowAppointments(from: String?, to: String?) =
        Result.success(emptyList<com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem>())
    override suspend fun getPendingClosureAppointments(from: String?, to: String?): Result<List<PendingClosureItem>> = Result.failure(UnsupportedOperationException())
}

class DefaultDoctorDashboardComponentTest {

    private val dispatchers = TestAppDispatchers()

    private fun component(dashboard: DoctorDashboard): DefaultDoctorDashboardComponent {
        val lifecycle = LifecycleRegistry()
        val ctx = DefaultComponentContext(lifecycle)
        val useCase = GetDoctorDashboardUseCase(StubDashboardDataSource(dashboard), dispatchers)
        val comp = DefaultDoctorDashboardComponent(
            componentContext = ctx,
            doctorId = "doc-1",
            getDashboard = useCase,
            dispatchers = dispatchers,
            onOutput = {},
        )
        lifecycle.resume()
        return comp
    }

    @Test
    fun maps_all_design_metrics_into_state() = runTest {
        val comp = component(
            DoctorDashboard(
                todayCount = 8,
                pendingCount = 2,
                monthlyEarnings = 4820.0,
                ratingAverage = 4.9,
                ratingCount = 248,
                completedCount = 312,
                completedThisMonth = 18,
                patientsCount = 86,
                recurringPatientsCount = 40,
                completedTodayPct = 67,
            ),
        )

        val state = comp.state.value
        assertEquals(8, state.todayCount)
        assertEquals("S/4,820", state.monthlyEarnings)
        assertEquals(4.9, state.ratingAverage)
        assertEquals(248, state.ratingCount)
        assertEquals(312, state.completedCount)
        assertEquals(18, state.completedThisMonth)
        assertEquals(86, state.patientsCount)
        assertEquals(67, state.completedTodayPct)
    }
}
