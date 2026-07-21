@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.core

import app.cash.turbine.test
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.events.SessionExpiryReason
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
            awaitItem() // receives a SessionExpiryReason
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emitExpired_delivers_event_to_multiple_collectors() = runTest {
        val events = SessionEvents()
        val received1 = mutableListOf<SessionExpiryReason>()
        val received2 = mutableListOf<SessionExpiryReason>()

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

    // ── Session-expiry reason (design-gap-closure: distinguish 401 vs explicit logout) ──

    @Test
    fun emitExpired_with_no_argument_defaults_to_EXPIRED_reason() = runTest {
        val events = SessionEvents()

        events.expired.test {
            events.emitExpired()
            assertEquals(SessionExpiryReason.EXPIRED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emitExpired_with_USER_INITIATED_reason_is_delivered_as_is() = runTest {
        val events = SessionEvents()

        events.expired.test {
            events.emitExpired(SessionExpiryReason.USER_INITIATED)
            assertEquals(SessionExpiryReason.USER_INITIATED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
