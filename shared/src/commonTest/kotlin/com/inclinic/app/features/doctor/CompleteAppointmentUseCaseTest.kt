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
    var lastCheckInLat: Double? = null
    var lastCheckInLng: Double? = null

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>, checkInLat: Double?, checkInLng: Double?, checkInAccuracyM: Double?): Result<Appointment> {
        completeCallCount++
        lastPhotoUrls = photoUrls
        lastCheckInLat = checkInLat
        lastCheckInLng = checkInLng
        return completeResult
    }

    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> =
        Result.success(DoctorDashboard(0, 0, 0.0, 0.0))

    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> =
        Result.success(emptyList())

    override suspend fun getAvailability(doctorId: String, date: String): Result<List<com.inclinic.app.core.model.AvailabilitySlot>> =
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

private fun makeAppointment(
    status: AppointmentStatus,
    visitType: VisitType = VisitType.VIRTUAL,
): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1",
        doctorId = "doc-1",
        patientId = "pat-1",
        specialtyId = "sp-1",
        visitType = visitType,
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
    fun one_photo_url_calls_api_and_returns_success() = runTest {
        val appointment = makeAppointment(AppointmentStatus.CONFIRMED)

        val result = useCase(appointment, listOf("https://cdn/visit-proofs/a.jpg"))

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.completeCallCount)
        assertEquals(listOf("https://cdn/visit-proofs/a.jpg"), fakeDataSource.lastPhotoUrls)
    }

    @Test
    fun photo_urls_are_forwarded_unchanged() = runTest {
        val appointment = makeAppointment(AppointmentStatus.IN_PROGRESS)
        val urls = listOf("u0", "u1", "u2")

        val result = useCase(appointment, urls)

        assertTrue(result.isSuccess)
        assertEquals(urls, fakeDataSource.lastPhotoUrls)
    }

    @Test
    fun wrong_status_returns_failure_without_api_call() = runTest {
        val appointment = makeAppointment(AppointmentStatus.COMPLETED)

        val result = useCase(appointment, listOf("u0"))

        assertTrue(result.isFailure)
        assertEquals(0, fakeDataSource.completeCallCount)
    }

    @Test
    fun home_visit_without_checkin_returns_failure_without_api_call() = runTest {
        val appointment = makeAppointment(AppointmentStatus.CONFIRMED, VisitType.HOME)

        val result = useCase(appointment, listOf("u0"), checkIn = null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals(0, fakeDataSource.completeCallCount)
    }

    @Test
    fun home_visit_with_checkin_forwards_coordinates() = runTest {
        val appointment = makeAppointment(AppointmentStatus.CONFIRMED, VisitType.HOME)
        val fix = com.inclinic.app.core.platform.GpsFix(lat = -12.05, lng = -77.04, accuracyMeters = 8.0)

        val result = useCase(appointment, listOf("u0"), checkIn = fix)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.completeCallCount)
        assertEquals(-12.05, fakeDataSource.lastCheckInLat)
        assertEquals(-77.04, fakeDataSource.lastCheckInLng)
    }
}
