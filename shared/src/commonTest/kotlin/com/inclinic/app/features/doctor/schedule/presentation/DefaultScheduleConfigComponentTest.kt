package com.inclinic.app.features.doctor.schedule.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.config.application.GetScheduleConfigUseCase
import com.inclinic.app.features.doctor.config.application.SaveScheduleConfigUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DaySchedule
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorScheduleDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.WeeklySchedule
import com.inclinic.app.features.doctor.presentation.component.DefaultScheduleConfigComponent
import com.inclinic.app.features.doctor.presentation.component.ScheduleConfigComponent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeScheduleDataSource(
    private val initial: WeeklySchedule = WeeklySchedule(),
) : DoctorScheduleDataSource {
    var lastSaved: WeeklySchedule? = null
    override suspend fun getWeeklySchedule(doctorId: String): Result<WeeklySchedule> =
        Result.success(initial)
    override suspend fun saveWeeklySchedule(doctorId: String, schedule: WeeklySchedule): Result<WeeklySchedule> {
        lastSaved = schedule
        return Result.success(schedule)
    }
}

class DefaultScheduleConfigComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        ds: DoctorScheduleDataSource,
        onOutput: (ScheduleConfigComponent.Output) -> Unit = {},
    ): DefaultScheduleConfigComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultScheduleConfigComponent(
            componentContext = ctx,
            doctorId = "doc1",
            getScheduleConfig = GetScheduleConfigUseCase(ds, dispatchers),
            saveScheduleConfig = SaveScheduleConfigUseCase(ds, dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    @Test
    fun load_merges_backend_blocks_into_all_seven_days() = runTest {
        val ds = FakeScheduleDataSource(
            WeeklySchedule(
                days = listOf(
                    DaySchedule(
                        dayOfWeek = "MONDAY", startTime = "08:00", endTime = "13:00",
                        maxPatients = 10, slotDuration = 30, price = 120.0,
                        allowVisitTypeNegotiation = true,
                    ),
                ),
            ),
        )
        val component = makeComponent(ds)

        val s = component.state.value
        assertEquals(7, s.days.size)
        val monday = s.days.first { it.day == DayOfWeek.MONDAY }
        assertTrue(monday.enabled)
        assertEquals("08:00", monday.startTime)
        assertEquals("10", monday.maxPatients)
        assertEquals("120", monday.price)
        assertTrue(monday.allowNegotiation)
        assertFalse(s.days.first { it.day == DayOfWeek.SUNDAY }.enabled)
    }

    @Test
    fun toggle_day_enables_and_expands() = runTest {
        val component = makeComponent(FakeScheduleDataSource())
        component.onToggleDay(DayOfWeek.TUESDAY)
        val tue = component.state.value.days.first { it.day == DayOfWeek.TUESDAY }
        assertTrue(tue.enabled)
        assertTrue(tue.expanded)
    }

    @Test
    fun save_only_sends_enabled_days() = runTest {
        val ds = FakeScheduleDataSource()
        val component = makeComponent(ds)
        component.onToggleDay(DayOfWeek.MONDAY)
        component.onStartTimeChange(DayOfWeek.MONDAY, "09:00")
        component.onEndTimeChange(DayOfWeek.MONDAY, "17:00")
        component.onPriceChange(DayOfWeek.MONDAY, "150")

        component.onSave()

        assertEquals(1, ds.lastSaved!!.days.size)
        val saved = ds.lastSaved!!.days[0]
        assertEquals("MONDAY", saved.dayOfWeek)
        assertEquals("09:00", saved.startTime)
        assertEquals(150.0, saved.price)
        assertTrue(component.state.value.saveSuccess)
    }
}
