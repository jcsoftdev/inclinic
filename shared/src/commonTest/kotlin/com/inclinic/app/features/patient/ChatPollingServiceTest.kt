@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.features.patient.chat.infrastructure.ChatPollingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ChatPollingService] backoff and error-reset behavior.
 *
 * We use [StandardTestDispatcher] (not Unconfined) so that [advanceTimeBy]
 * gives us fine-grained control over the virtual clock inside the polling loop.
 */
class ChatPollingServiceTest {

    /**
     * Test dispatcher that wires all three dispatcher slots to the same
     * StandardTestDispatcher so that advanceTimeBy controls the polling loop.
     */
    private fun testDispatchers(scope: TestScope): AppDispatchers {
        val d = StandardTestDispatcher(scope.testScheduler)
        return object : AppDispatchers {
            override val main: CoroutineDispatcher = d
            override val io: CoroutineDispatcher = d
            override val default: CoroutineDispatcher = d
        }
    }

    @Test
    fun three_consecutive_errors_produce_exponential_backoff_intervals() = runTest {
        val dispatchers = testDispatchers(this)
        var callCount = 0

        val service = ChatPollingService(
            "doc-1",
            getMessages = {
                callCount++
                Result.failure(Exception("Error $callCount"))
            },
            dispatchers = dispatchers,
        )

        service.start(this)

        // After 1st error: backs off for 5_000 ms
        advanceTimeBy(1L)          // triggers 1st getMessages() call → error → delay(5_000)
        assertEquals(1, callCount)

        advanceTimeBy(5_000L)      // completes 5_000 ms delay → 2nd call → error → delay(10_000)
        assertEquals(2, callCount)

        advanceTimeBy(10_000L)     // completes 10_000 ms delay → 3rd call → error → delay(20_000)
        assertEquals(3, callCount)

        service.stop()
    }

    @Test
    fun success_resets_consecutive_errors_to_zero_and_uses_normal_interval() = runTest {
        val dispatchers = testDispatchers(this)
        var callCount = 0
        val successMessages = listOf(
            ChatMessage(
                id = "m1",
                appointmentId = "apt-1",
                senderId = "s1",
                senderRole = com.inclinic.app.core.model.SenderRole.DOCTOR,
                text = "Hello",
                sentAt = kotlin.time.Clock.System.now(),
                readAt = null,
            )
        )

        // First call fails, second succeeds
        val service = ChatPollingService(
            "doc-1",
            getMessages = {
                callCount++
                when (callCount) {
                    1 -> Result.failure(Exception("First error"))
                    else -> Result.success(successMessages)
                }
            },
            dispatchers = dispatchers,
        )

        service.start(this)

        advanceTimeBy(1L)       // 1st call → failure → backoff 5_000 ms
        assertEquals(1, callCount)

        advanceTimeBy(5_000L)   // 2nd call → success → normal 5_000 ms poll
        assertEquals(2, callCount)

        // After success the messages state should be updated
        assertEquals(successMessages, service.messages.value)

        service.stop()
    }

    @Test
    fun fourth_consecutive_error_uses_60_second_backoff() = runTest {
        val dispatchers = testDispatchers(this)
        var callCount = 0

        val service = ChatPollingService(
            "doc-1",
            getMessages = {
                callCount++
                Result.failure(Exception("Persistent error"))
            },
            dispatchers = dispatchers,
        )

        service.start(this)

        advanceTimeBy(1L)         // 1st call → error → 5_000
        advanceTimeBy(5_000L)     // 2nd call → error → 10_000
        advanceTimeBy(10_000L)    // 3rd call → error → 20_000
        advanceTimeBy(20_000L)    // 4th call → error → 60_000

        assertEquals(4, callCount)

        // Should NOT trigger a 5th call before 60_000 ms
        advanceTimeBy(59_999L)
        assertEquals(4, callCount)

        advanceTimeBy(1L)         // completes 60_000 delay → 5th call
        assertEquals(5, callCount)

        service.stop()
    }
}
