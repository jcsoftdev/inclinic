package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.application.GetOnboardingStatusUseCase
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultEnviadoComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorOnboardingRepository()

    private fun statusUseCase() = GetOnboardingStatusUseCase(fakeRepo, dispatchers)

    private fun makeComponent(
        initialStatus: OnboardingStatus = OnboardingStatus.PENDING,
        onOutput: (EnviadoComponent.Output) -> Unit = {},
    ) = DefaultEnviadoComponent(
        componentContext = context,
        dispatchers = dispatchers,
        getOnboardingStatusUseCase = statusUseCase(),
        initialStatus = initialStatus,
        onOutput = onOutput,
    )

    // ── Initial state handling ────────────────────────────────────────────────

    @Test
    fun initial_status_PENDING_is_reflected_in_state() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.PENDING)
        val c = makeComponent(initialStatus = OnboardingStatus.PENDING)
        assertEquals(OnboardingStatus.PENDING, c.state.value.status)
    }

    @Test
    fun initial_status_APPROVED_is_reflected_in_state() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.APPROVED)
        val c = makeComponent(initialStatus = OnboardingStatus.APPROVED)
        // After init refreshStatus() runs — fake returns APPROVED
        assertEquals(OnboardingStatus.APPROVED, c.state.value.status)
    }

    @Test
    fun initial_status_REJECTED_is_reflected_in_state() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.REJECTED)
        val c = makeComponent(initialStatus = OnboardingStatus.REJECTED)
        assertEquals(OnboardingStatus.REJECTED, c.state.value.status)
    }

    // ── Status refresh ────────────────────────────────────────────────────────

    @Test
    fun status_is_refreshed_from_use_case_on_init() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.APPROVED)
        val c = makeComponent(initialStatus = OnboardingStatus.PENDING)
        // refreshStatus() runs in init with UnconfinedTestDispatcher → completes synchronously
        assertEquals(OnboardingStatus.APPROVED, c.state.value.status)
        assertEquals(1, fakeRepo.getStatusCallCount)
    }

    @Test
    fun status_refresh_failure_sets_error() = runTest {
        fakeRepo.statusResult = Result.failure(RuntimeException("Network fail"))
        val c = makeComponent()
        assertTrue(c.state.value.error != null)
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    fun onLogOutClicked_emits_LogOut_output() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.PENDING)
        var outputReceived: EnviadoComponent.Output? = null
        val c = makeComponent(onOutput = { outputReceived = it })
        c.onLogOutClicked()
        assertEquals(EnviadoComponent.Output.LogOut, outputReceived)
    }

    // ── Error dismissed ───────────────────────────────────────────────────────

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        fakeRepo.statusResult = Result.failure(RuntimeException("fail"))
        val c = makeComponent()
        assertTrue(c.state.value.error != null)
        c.onErrorDismissed()
        assertNull(c.state.value.error)
    }
}
