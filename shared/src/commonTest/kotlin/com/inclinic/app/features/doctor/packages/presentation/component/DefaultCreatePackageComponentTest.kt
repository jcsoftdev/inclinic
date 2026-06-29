package com.inclinic.app.features.doctor.packages.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.packages.application.CreatePackageUseCase
import com.inclinic.app.features.doctor.packages.fakes.FakeDoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.fakes.samplePackage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultCreatePackageComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorPackagesRepository()
    private var capturedOutput: CreatePackageComponent.Output? = null

    private val specialties = listOf(
        SpecialtyOption("spe-1", "Nutrición"),
        SpecialtyOption("spe-2", "Cardiología"),
    )

    private fun createComponent(): DefaultCreatePackageComponent {
        capturedOutput = null
        return DefaultCreatePackageComponent(
            componentContext = componentContext,
            patientId = "pat-1",
            patientName = "Juan Pérez",
            patientEmail = "juan.perez@correo.com",
            specialties = specialties,
            createPackage = CreatePackageUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = { capturedOutput = it },
        )
    }

    private fun fillValid(c: DefaultCreatePackageComponent) {
        c.onPackageNameChange("Plan nutricional 6 meses")
        c.onSpecialtySelected("spe-1")
        c.onTotalSessionsChange("12")
        c.onRegularPriceChange("70")
        c.onPackagePriceChange("60")
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_carries_patient_and_defaults_first_specialty() {
        val state = createComponent().state.value
        assertEquals("pat-1", state.patientId)
        assertEquals("Juan Pérez", state.patientName)
        assertEquals("spe-1", state.selectedSpecialtyId)
        assertFalse(state.isPrepaid)
        assertNull(state.error)
    }

    // ── Pricing math ─────────────────────────────────────────────────────────

    @Test
    fun total_without_prepaid_is_price_times_sessions() = runTest {
        val c = createComponent()
        c.onTotalSessionsChange("12")
        c.onPackagePriceChange("60")

        assertEquals(720.0, c.state.value.totalBeforeDiscount)
        assertEquals(720.0, c.state.value.totalWithDiscount)
    }

    @Test
    fun prepaid_applies_discount_to_total() = runTest {
        val c = createComponent()
        c.onTotalSessionsChange("12")
        c.onPackagePriceChange("60")
        c.onPrepaidToggle(true)

        assertEquals(612.0, c.state.value.totalWithDiscount)
        assertEquals(108.0, c.state.value.discountSavings)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun onSubmit_with_short_name_sets_nameError_without_network() = runTest {
        val c = createComponent()
        c.onPackageNameChange("ab")
        c.onTotalSessionsChange("12")
        c.onRegularPriceChange("70")
        c.onPackagePriceChange("60")

        c.onSubmit()

        assertNotNull(c.state.value.nameError)
        assertEquals(0, fakeRepo.createCallCount)
    }

    @Test
    fun onSubmit_with_too_few_sessions_sets_sessionsError() = runTest {
        val c = createComponent()
        c.onPackageNameChange("Plan completo")
        c.onTotalSessionsChange("1")
        c.onRegularPriceChange("70")
        c.onPackagePriceChange("60")

        c.onSubmit()

        assertNotNull(c.state.value.sessionsError)
        assertEquals(0, fakeRepo.createCallCount)
    }

    @Test
    fun onSubmit_with_invalid_package_price_sets_error() = runTest {
        val c = createComponent()
        c.onPackageNameChange("Plan completo")
        c.onTotalSessionsChange("12")
        c.onRegularPriceChange("70")
        c.onPackagePriceChange("0")

        c.onSubmit()

        assertNotNull(c.state.value.packagePriceError)
        assertEquals(0, fakeRepo.createCallCount)
    }

    // ── Success path ──────────────────────────────────────────────────────────

    @Test
    fun onSubmit_valid_form_creates_draft_and_emits_PackageCreated() = runTest {
        fakeRepo.createResult = Result.success(samplePackage(id = "new-id"))
        val c = createComponent()
        fillValid(c)
        c.onPrepaidToggle(true)

        c.onSubmit()

        assertEquals(CreatePackageComponent.Output.PackageCreated, capturedOutput)
        assertEquals(1, fakeRepo.createCallCount)
        val draft = fakeRepo.lastCreated
        assertNotNull(draft)
        assertEquals("pat-1", draft.patientId)
        assertEquals("spe-1", draft.specialtyId)
        assertEquals(12, draft.totalSessions)
        assertEquals(70.0, draft.regularPricePerSession)
        assertEquals(60.0, draft.packagePricePerSession)
        assertTrue(draft.isPrepaid)
        assertEquals(15.0, draft.prepaidDiscount)
    }

    @Test
    fun onSubmit_without_prepaid_sends_null_discount() = runTest {
        fakeRepo.createResult = Result.success(samplePackage(id = "new-id"))
        val c = createComponent()
        fillValid(c)

        c.onSubmit()

        assertEquals(false, fakeRepo.lastCreated?.isPrepaid)
        assertNull(fakeRepo.lastCreated?.prepaidDiscount)
    }

    // ── Error path ────────────────────────────────────────────────────────────

    @Test
    fun onSubmit_network_failure_sets_error_and_clears_isSubmitting() = runTest {
        fakeRepo.createResult = Result.failure(RuntimeException("Network error"))
        val c = createComponent()
        fillValid(c)

        c.onSubmit()

        assertFalse(c.state.value.isSubmitting)
        assertNotNull(c.state.value.error)
        assertNull(capturedOutput)
    }

    // ── Back ──────────────────────────────────────────────────────────────────

    @Test
    fun onBack_emits_Back_output() {
        val c = createComponent()

        c.onBack()

        assertEquals(CreatePackageComponent.Output.Back, capturedOutput)
    }
}
