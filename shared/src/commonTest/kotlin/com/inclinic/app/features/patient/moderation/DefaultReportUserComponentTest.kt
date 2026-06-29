@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.moderation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.moderation.application.ReportUserUseCase
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import com.inclinic.app.features.patient.moderation.infrastructure.remote.ModerationRemoteDataSource
import com.inclinic.app.features.patient.moderation.presentation.component.DefaultReportUserComponent
import com.inclinic.app.features.patient.moderation.presentation.component.ReportUserComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeReportDataSource : ModerationRemoteDataSource {
    var result: Result<Unit> = Result.success(Unit)
    var callCount = 0

    override suspend fun reportUser(userId: String, reason: String, category: ReportCategory?): Result<Unit> {
        callCount++
        return result
    }
    override suspend fun blockUser(userId: String, reason: String?) = Result.success(Unit)
    override suspend fun unblockUser(userId: String) = Result.success(Unit)
}

// ── Tests ─────────────────────────────────────────────────────────────────────

class DefaultReportUserComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeDs = FakeReportDataSource()

    private val emittedOutputs = mutableListOf<ReportUserComponent.Output>()

    private fun makeComponent() = DefaultReportUserComponent(
        componentContext = context,
        targetUserId = "doc-1",
        targetUserName = "Dr. Mendoza",
        reportUser = ReportUserUseCase(fakeDs, dispatchers),
        dispatchers = dispatchers,
        onOutput = { emittedOutputs.add(it) },
    )

    @Test
    fun initial_state_has_correct_target_info() {
        val comp = makeComponent()
        assertEquals("doc-1", comp.state.value.targetUserId)
        assertEquals("Dr. Mendoza", comp.state.value.targetUserName)
        assertFalse(comp.state.value.isLoading)
        assertNull(comp.state.value.error)
    }

    @Test
    fun onReasonChanged_updates_reason_in_state() {
        val comp = makeComponent()
        comp.onReasonChanged("Spam messages from doctor")
        assertEquals("Spam messages from doctor", comp.state.value.reason)
        assertNull(comp.state.value.error)
    }

    @Test
    fun onCategorySelected_updates_category_in_state() {
        val comp = makeComponent()
        comp.onCategorySelected(ReportCategory.Abuse)
        assertEquals(ReportCategory.Abuse, comp.state.value.selectedCategory)
    }

    @Test
    fun onSubmit_with_valid_reason_emits_submitted_output() = runTest {
        val comp = makeComponent()
        comp.onReasonChanged("Valid reason of 10+ chars")
        comp.onCategorySelected(ReportCategory.Spam)
        comp.onSubmit()

        assertEquals(1, fakeDs.callCount)
        assertTrue(emittedOutputs.contains(ReportUserComponent.Output.Submitted))
    }

    @Test
    fun onSubmit_with_short_reason_sets_error_without_calling_datasource() = runTest {
        val comp = makeComponent()
        comp.onReasonChanged("short")
        comp.onSubmit()

        assertEquals(0, fakeDs.callCount)
        assertTrue(comp.state.value.error?.contains("10") == true)
        assertFalse(emittedOutputs.contains(ReportUserComponent.Output.Submitted))
    }

    @Test
    fun onSubmit_datasource_failure_sets_error_on_state() = runTest {
        fakeDs.result = Result.failure(Exception("Network error"))
        val comp = makeComponent()
        comp.onReasonChanged("A reason long enough to pass")
        comp.onSubmit()

        assertNotNull(comp.state.value.error)
        assertFalse(emittedOutputs.contains(ReportUserComponent.Output.Submitted))
    }

    @Test
    fun onBack_emits_back_output() {
        val comp = makeComponent()
        comp.onBack()

        assertTrue(emittedOutputs.contains(ReportUserComponent.Output.Back))
    }
}
