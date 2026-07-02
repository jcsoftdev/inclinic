@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.appointments.application.CompleteAppointmentUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private class FakeCompleteDataSource : DoctorAppointmentDataSource {
    var completeResult: Result<Appointment> = Result.success(
        makeAppointment(AppointmentStatus.COMPLETED)
    )
    var completeCallCount = 0
    var lastPhotoUrls: List<String>? = null

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment> {
        completeCallCount++
        lastPhotoUrls = photoUrls
        return completeResult
    }

    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> =
        Result.success(DoctorDashboard(0, 0, 0.0, 0.0))

    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> =
        Result.success(emptyList())

    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> =
        Result.success(emptyList())

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun markNoShow(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getNoShowAppointments(from: String?, to: String?) =
        Result.success(emptyList<com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem>())

    override suspend fun getPendingClosureAppointments(from: String?, to: String?): Result<List<PendingClosureItem>> =
        Result.failure(UnsupportedOperationException())
}

private fun makeAppointment(status: AppointmentStatus): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1",
        doctorId = "doc-1",
        patientId = "pat-1",
        specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL,
        status = status,
        consultationFee = 100.0,
        commissionAmount = 15.0,
        startsAt = now - 1.hours,
        endsAt = now,
        rescheduleCount = 0,
        paymentDeadline = null,
        notes = null,
        createdAt = now - 2.hours,
    )
}

class CompleteAppointmentUseCaseTest {

    private val fakeDataSource = FakeCompleteDataSource()
    private val dispatchers = TestAppDispatchers()

    private val useCase = CompleteAppointmentUseCase(
        dataSource = fakeDataSource,
        dispatchers = dispatchers,
    )

    @Test
    fun zero_photos_returns_failure_without_api_call() = runTest {
        val appointment = makeAppointment(AppointmentStatus.CONFIRMED)

        val result = useCase(appointment, emptyList())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals(0, fakeDataSource.completeCallCount)
    }

    @Test
    fun one_photo_calls_api_with_stub_url_and_returns_success() = runTest {
        val appointment = makeAppointment(AppointmentStatus.CONFIRMED)
        val photo = ByteArray(10) { it.toByte() }

        val result = useCase(appointment, listOf(photo))

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.completeCallCount)
        // Stub URLs follow pattern "stub-evidence-{index}"
        assertEquals(listOf("stub-evidence-0"), fakeDataSource.lastPhotoUrls)
    }

    @Test
    fun three_photos_produces_three_stub_urls() = runTest {
        val appointment = makeAppointment(AppointmentStatus.IN_PROGRESS)
        val photos = List(3) { ByteArray(5) }

        val result = useCase(appointment, photos)

        assertTrue(result.isSuccess)
        assertEquals(listOf("stub-evidence-0", "stub-evidence-1", "stub-evidence-2"), fakeDataSource.lastPhotoUrls)
    }

    @Test
    fun wrong_status_returns_failure_without_api_call() = runTest {
        val appointment = makeAppointment(AppointmentStatus.COMPLETED)
        val photo = ByteArray(5)

        val result = useCase(appointment, listOf(photo))

        assertTrue(result.isFailure)
        assertEquals(0, fakeDataSource.completeCallCount)
    }
}
