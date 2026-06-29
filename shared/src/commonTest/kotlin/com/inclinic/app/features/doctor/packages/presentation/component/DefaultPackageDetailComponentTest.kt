package com.inclinic.app.features.doctor.packages.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.packages.application.CancelPackageUseCase
import com.inclinic.app.features.doctor.packages.application.GetDoctorPackagesUseCase
import com.inclinic.app.features.doctor.packages.fakes.FakeDoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.fakes.samplePackage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultPackageDetailComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorPackagesRepository()
    private var capturedOutput: PackageDetailComponent.Output? = null

    private fun createComponent(packageId: String = "pkg-1"): DefaultPackageDetailComponent {
        capturedOutput = null
        return DefaultPackageDetailComponent(
            componentContext = componentContext,
            packageId = packageId,
            getPackages = GetDoctorPackagesUseCase(fakeRepo, dispatchers),
            cancelPackage = CancelPackageUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = { capturedOutput = it },
        )
    }

    // ── Initial load ──────────────────────────────────────────────────────────

    @Test
    fun initial_state_loads_matching_package_on_success() = runTest {
        val target = samplePackage("pkg-1")
        fakeRepo.listResult = Result.success(listOf(samplePackage("pkg-0"), target))

        val component = createComponent("pkg-1")

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(target, component.state.value.pkg)
    }

    @Test
    fun initial_state_sets_error_when_package_not_found() = runTest {
        fakeRepo.listResult = Result.success(listOf(samplePackage("pkg-0")))

        val component = createComponent("missing")

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.pkg)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun initial_state_sets_error_on_failure() = runTest {
        fakeRepo.listResult = Result.failure(RuntimeException("Error"))

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.pkg)
        assertNotNull(component.state.value.error)
    }

    // ── Retry ─────────────────────────────────────────────────────────────────

    @Test
    fun onRetry_reloads_package() = runTest {
        fakeRepo.listResult = Result.failure(RuntimeException("Error"))
        val component = createComponent("pkg-1")
        assertNotNull(component.state.value.error)

        fakeRepo.listResult = Result.success(listOf(samplePackage("pkg-1")))
        component.onRetry()

        assertNull(component.state.value.error)
        assertNotNull(component.state.value.pkg)
        assertEquals(2, fakeRepo.listCallCount)
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Test
    fun onCancel_calls_cancel_use_case_and_emits_Cancelled() = runTest {
        fakeRepo.listResult = Result.success(listOf(samplePackage("pkg-1")))
        val component = createComponent("pkg-1")
        fakeRepo.cancelResult = Result.success(Unit)

        component.onCancel()

        assertEquals("pkg-1", fakeRepo.lastCancelledId)
        assertEquals(1, fakeRepo.cancelCallCount)
        assertEquals(PackageDetailComponent.Output.Cancelled, capturedOutput)
    }

    @Test
    fun onCancel_failure_sets_error_and_clears_isCancelling() = runTest {
        fakeRepo.listResult = Result.success(listOf(samplePackage("pkg-1")))
        fakeRepo.cancelResult = Result.failure(RuntimeException("Cancel failed"))
        val component = createComponent("pkg-1")

        component.onCancel()

        assertFalse(component.state.value.isCancelling)
        assertNotNull(component.state.value.error)
        assertNull(capturedOutput)
    }

    @Test
    fun onCancel_does_nothing_when_package_not_loaded() = runTest {
        fakeRepo.listResult = Result.failure(RuntimeException("Error"))
        val component = createComponent("pkg-1")

        component.onCancel()

        assertEquals(0, fakeRepo.cancelCallCount)
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun onBack_emits_Back_output() {
        fakeRepo.listResult = Result.success(listOf(samplePackage("pkg-1")))
        val component = createComponent("pkg-1")

        component.onBack()

        assertEquals(PackageDetailComponent.Output.Back, capturedOutput)
    }

    @Test
    fun loaded_package_exposes_model_fields() = runTest {
        val target = samplePackage("pkg-1", packageName = "Rehab")
        fakeRepo.listResult = Result.success(listOf(target))

        val component = createComponent("pkg-1")
        val loaded = component.state.value.pkg

        assertNotNull(loaded)
        assertEquals("Rehab", loaded.packageName)
        assertEquals(10, loaded.totalSessions)
        assertEquals(120.0, loaded.packagePricePerSession)
        assertEquals(150.0, loaded.regularPricePerSession)
        assertTrue(loaded.isPrepaid)
    }
}
