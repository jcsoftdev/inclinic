package com.inclinic.app.features.doctor.schedule.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.config.application.SaveScheduleConfigUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DaySchedule
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorScheduleDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.WeeklySchedule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeScheduleDataSource(
    private val saveResult: Result<WeeklySchedule> = Result.success(WeeklySchedule()),
) : DoctorScheduleDataSource {
    var saved: WeeklySchedule? = null
    override suspend fun getWeeklySchedule(doctorId: String): Result<WeeklySchedule> =
        Result.success(WeeklySchedule())
    override suspend fun saveWeeklySchedule(doctorId: String, schedule: WeeklySchedule): Result<WeeklySchedule> {
        saved = schedule
        return saveResult
    }
}

class SaveScheduleConfigUseCaseTest {

    private val testDispatchers = TestAppDispatchers()


    @Test
    fun rejects_day_with_end_before_start() = runTest {
        val ds = FakeScheduleDataSource()
        val useCase = SaveScheduleConfigUseCase(ds, testDispatchers)
        val schedule = WeeklySchedule(
            days = listOf(DaySchedule(dayOfWeek = "MONDAY", startTime = "13:00", endTime = "08:00")),
        )
        val result = useCase("doc1", schedule)
        assertTrue(result.isFailure)
        assertEquals(null, ds.saved)
    }

    @Test
    fun persists_valid_schedule() = runTest {
        val ds = FakeScheduleDataSource()
        val useCase = SaveScheduleConfigUseCase(ds, testDispatchers)
        val schedule = WeeklySchedule(
            days = listOf(DaySchedule(dayOfWeek = "MONDAY", startTime = "08:00", endTime = "13:00")),
        )
        val result = useCase("doc1", schedule)
        assertTrue(result.isSuccess)
        assertFalse(ds.saved == null)
        assertEquals(1, ds.saved!!.days.size)
    }
}
