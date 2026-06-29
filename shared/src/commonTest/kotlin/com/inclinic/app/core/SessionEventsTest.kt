@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.core

import app.cash.turbine.test
import com.inclinic.app.core.events.SessionEvents
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionEventsTest {

    @Test
    fun emitExpired_delivers_event_to_collector() = runTest {
        val events = SessionEvents()

        events.expired.test {
            events.emitExpired()
            awaitItem() // receives Unit
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emitExpired_delivers_event_to_multiple_collectors() = runTest {
        val events = SessionEvents()
        val received1 = mutableListOf<Unit>()
        val received2 = mutableListOf<Unit>()

        val job1 = launch { events.expired.collect { received1.add(it) } }
        val job2 = launch { events.expired.collect { received2.add(it) } }

        testScheduler.runCurrent()

        events.emitExpired()

        testScheduler.advanceUntilIdle()

        job1.cancel()
        job2.cancel()

        assertEquals(1, received1.size, "Collector 1 should have received exactly 1 event")
        assertEquals(1, received2.size, "Collector 2 should have received exactly 1 event")
    }
}
