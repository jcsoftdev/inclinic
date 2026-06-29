@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.moderation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.moderation.application.BlockUserUseCase
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import com.inclinic.app.features.patient.moderation.infrastructure.remote.ModerationRemoteDataSource
import com.inclinic.app.features.patient.moderation.presentation.component.BlockUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.DefaultBlockUserComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeBlockDs : ModerationRemoteDataSource {
    var result: Result<Unit> = Result.success(Unit)
    var lastReason: String? = null
    var callCount = 0

    override suspend fun reportUser(userId: String, reason: String, category: ReportCategory?) = Result.success(Unit)
    override suspend fun blockUser(userId: String, reason: String?): Result<Unit> {
        callCount++
        lastReason = reason
        return result
    }
    override suspend fun unblockUser(userId: String) = Result.success(Unit)
}

// ── Tests ─────────────────────────────────────────────────────────────────────

class DefaultBlockUserComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeDs = FakeBlockDs()

    private val emittedOutputs = mutableListOf<BlockUserComponent.Output>()

    private fun makeComponent() = DefaultBlockUserComponent(
        componentContext = context,
        targetUserId = "doc-5",
        targetUserName = "Dr. García",
        blockUser = BlockUserUseCase(fakeDs, dispatchers),
        dispatchers = dispatchers,
        onOutput = { emittedOutputs.add(it) },
    )

    @Test
    fun initial_state_has_correct_target_info() {
        val comp = makeComponent()
        assertEquals("doc-5", comp.state.value.targetUserId)
        assertEquals("Dr. García", comp.state.value.targetUserName)
        assertFalse(comp.state.value.isLoading)
        assertNull(comp.state.value.error)
    }

    @Test
    fun onReasonChanged_updates_reason() {
        val comp = makeComponent()
        comp.onReasonChanged("Insistent contact")
        assertEquals("Insistent contact", comp.state.value.reason)
    }

    @Test
    fun onConfirm_with_no_reason_emits_blocked_and_passes_null() = runTest {
        val comp = makeComponent()
        comp.onConfirm()

        assertEquals(1, fakeDs.callCount)
        assertNull(fakeDs.lastReason)
        assertTrue(emittedOutputs.contains(BlockUserComponent.Output.Blocked))
    }

    @Test
    fun onConfirm_with_reason_emits_blocked() = runTest {
        val comp = makeComponent()
        comp.onReasonChanged("Harassment")
        comp.onConfirm()

        assertEquals(1, fakeDs.callCount)
        assertEquals("Harassment", fakeDs.lastReason)
        assertTrue(emittedOutputs.contains(BlockUserComponent.Output.Blocked))
    }

    @Test
    fun onConfirm_datasource_failure_sets_error() = runTest {
        fakeDs.result = Result.failure(Exception("Timeout"))
        val comp = makeComponent()
        comp.onConfirm()

        assertNotNull(comp.state.value.error)
        assertFalse(emittedOutputs.contains(BlockUserComponent.Output.Blocked))
    }

    @Test
    fun onCancel_emits_back_output() {
        val comp = makeComponent()
        comp.onCancel()

        assertTrue(emittedOutputs.contains(BlockUserComponent.Output.Back))
    }
}
