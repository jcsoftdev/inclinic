@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.auth.fakes

import com.inclinic.app.core.concurrency.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Test implementation of [AppDispatchers].
 *
 * By default uses [UnconfinedTestDispatcher] so that:
 * - Tests using `runTest { }` do NOT need to forward the scheduler manually.
 * - `withContext(dispatchers.io)` in use cases completes eagerly inside `runTest`.
 *
 * Pass [useStandard = true] (or supply a [scheduler] shared from the enclosing `runTest`)
 * when the test needs to advance virtual time explicitly — e.g. cooldown timer tests that
 * call [kotlinx.coroutines.test.TestScope.advanceTimeBy].
 *
 * If a test needs fine-grained coroutine scheduling, pass a shared [TestCoroutineScheduler]
 * from the surrounding `runTest` scope explicitly.
 */
class TestAppDispatchers(
    scheduler: TestCoroutineScheduler? = null,
    useStandard: Boolean = false,
) : AppDispatchers {
    private val testDispatcher = when {
        useStandard && scheduler != null -> StandardTestDispatcher(scheduler)
        useStandard -> StandardTestDispatcher()
        scheduler != null -> UnconfinedTestDispatcher(scheduler)
        else -> UnconfinedTestDispatcher()
    }
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}
