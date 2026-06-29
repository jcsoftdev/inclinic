package com.inclinic.app.features.doctor.packages.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.packages.application.GetDoctorPackagesUseCase
import com.inclinic.app.features.doctor.packages.core.model.PackageStatus
import com.inclinic.app.features.doctor.packages.fakes.FakeDoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.fakes.samplePackage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultPackagesListComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorPackagesRepository()
    private var capturedOutput: PackagesListComponent.Output? = null

    private fun createComponent(): DefaultPackagesListComponent {
        capturedOutput = null
        return DefaultPackagesListComponent(
            componentContext = componentContext,
            getPackages = GetDoctorPackagesUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = { capturedOutput = it },
        )
    }

    // ── Initial load ──────────────────────────────────────────────────────────

    @Test
    fun initial_state_loads_packages_on_success() = runTest {
        val packages = listOf(samplePackage())
        fakeRepo.listResult = Result.success(packages)

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(packages, component.state.value.packages)
    }

    @Test
    fun initial_state_sets_error_on_failure() = runTest {
        fakeRepo.listResult = Result.failure(RuntimeException("Error"))

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.packages.isEmpty())
    }

    // ── Tab filtering ───────────────────────────────────────────────────────────

    @Test
    fun default_tab_shows_only_active_packages() = runTest {
        fakeRepo.listResult = Result.success(
            listOf(
                samplePackage(id = "a", status = PackageStatus.ACTIVE),
                samplePackage(id = "b", status = PackageStatus.PENDING_PAYMENT),
                samplePackage(id = "c", status = PackageStatus.CANCELLED),
            ),
        )
        val component = createComponent()

        assertEquals(PackageListTab.ACTIVE, component.state.value.selectedTab)
        val visible = component.state.value.visiblePackages
        assertEquals(1, visible.size)
        assertEquals("a", visible[0].id)
    }

    @Test
    fun selecting_pending_tab_shows_pending_payment_packages() = runTest {
        fakeRepo.listResult = Result.success(
            listOf(
                samplePackage(id = "a", status = PackageStatus.ACTIVE),
                samplePackage(id = "b", status = PackageStatus.PENDING_PAYMENT),
            ),
        )
        val component = createComponent()

        component.onTabSelected(PackageListTab.PENDING)

        val visible = component.state.value.visiblePackages
        assertEquals(1, visible.size)
        assertEquals("b", visible[0].id)
    }

    @Test
    fun archived_tab_shows_cancelled_and_completed() = runTest {
        fakeRepo.listResult = Result.success(
            listOf(
                samplePackage(id = "a", status = PackageStatus.CANCELLED),
                samplePackage(id = "b", status = PackageStatus.COMPLETED),
                samplePackage(id = "c", status = PackageStatus.ACTIVE),
            ),
        )
        val component = createComponent()

        component.onTabSelected(PackageListTab.ARCHIVED)

        assertEquals(2, component.state.value.visiblePackages.size)
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun onCreateClicked_emits_NavigateToCreate_output() {
        fakeRepo.listResult = Result.success(emptyList())
        val component = createComponent()

        component.onCreateClicked()

        assertEquals(PackagesListComponent.Output.NavigateToCreate, capturedOutput)
    }

    @Test
    fun onPackageClicked_emits_NavigateToDetail() {
        fakeRepo.listResult = Result.success(emptyList())
        val component = createComponent()

        component.onPackageClicked("pkg-7")

        assertEquals(PackagesListComponent.Output.NavigateToDetail("pkg-7"), capturedOutput)
    }

    @Test
    fun onBack_emits_Back_output() {
        fakeRepo.listResult = Result.success(emptyList())
        val component = createComponent()

        component.onBack()

        assertEquals(PackagesListComponent.Output.Back, capturedOutput)
    }
}
