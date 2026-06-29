package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.application.GetOnboardingStatusUseCase
import com.inclinic.app.features.doctor.onboarding.application.ResubmitOnboardingUseCase
import com.inclinic.app.features.doctor.onboarding.application.SubmitOnboardingUseCase
import com.inclinic.app.features.doctor.onboarding.application.UploadDocumentUseCase
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class DefaultDoctorOnboardingComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorOnboardingRepository()

    private fun makeComponent(
        onOutput: (DoctorOnboardingComponent.Output) -> Unit = {},
    ) = DefaultDoctorOnboardingComponent(
        componentContext = context,
        dispatchers = dispatchers,
        submitOnboardingUseCase = SubmitOnboardingUseCase(fakeRepo, dispatchers),
        uploadDocumentUseCase = UploadDocumentUseCase(fakeRepo, dispatchers),
        getOnboardingStatusUseCase = GetOnboardingStatusUseCase(fakeRepo, dispatchers),
        resubmitOnboardingUseCase = ResubmitOnboardingUseCase(fakeRepo, dispatchers),
        availableSpecialties = listOf("cardiology", "dermatology"),
        onOutput = onOutput,
    )

    // ── Initial navigation ────────────────────────────────────────────────────

    @Test
    fun initial_child_is_StepDatos() {
        val c = makeComponent()
        val active = c.stack.value.active.instance
        assertIs<DoctorOnboardingComponent.Child.StepDatos>(active)
    }

    // ── Navigation forward ────────────────────────────────────────────────────

    @Test
    fun after_datos_continue_child_is_StepDocumentos() = runTest {
        val c = makeComponent()
        val stepDatos = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepDatos).component
        stepDatos.onFirstNameChanged("Juan")
        stepDatos.onLastNameChanged("Perez")
        stepDatos.onCmpLicenseChanged("CMP-12345")
        stepDatos.onPhoneChanged("987654321")
        stepDatos.onContinueClicked()
        assertIs<DoctorOnboardingComponent.Child.StepDocumentos>(c.stack.value.active.instance)
    }

    @Test
    fun after_documentos_continue_child_is_StepEspecialidades() = runTest {
        fakeRepo.uploadResult = Result.success(UploadedDoc("d1", DocKind.CMP_LICENSE, "u1"))
        val c = makeComponent()

        // Step 1 → 2
        val datos = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepDatos).component
        datos.onFirstNameChanged("Juan"); datos.onLastNameChanged("Perez")
        datos.onCmpLicenseChanged("CMP-12345"); datos.onPhoneChanged("987654321")
        datos.onContinueClicked()

        // Step 2 → 3
        val docs = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepDocumentos).component
        fakeRepo.uploadResult = Result.success(UploadedDoc("d1", DocKind.CMP_LICENSE, "u1"))
        docs.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1), "cmp.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d2", DocKind.ID_FRONT, "u2"))
        docs.onPickDocument(DocKind.ID_FRONT, byteArrayOf(2), "id_front.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d3", DocKind.ID_BACK, "u3"))
        docs.onPickDocument(DocKind.ID_BACK, byteArrayOf(3), "id_back.pdf")
        docs.onContinueClicked()

        assertIs<DoctorOnboardingComponent.Child.StepEspecialidades>(c.stack.value.active.instance)
    }

    @Test
    fun after_precios_continue_on_submit_success_child_is_Enviado() = runTest {
        fakeRepo.submitResult = Result.success(Unit)
        val c = makeComponent()

        // Navigate through all 5 steps
        val datos = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepDatos).component
        datos.onFirstNameChanged("Juan"); datos.onLastNameChanged("Perez")
        datos.onCmpLicenseChanged("CMP-12345"); datos.onPhoneChanged("987654321")
        datos.onContinueClicked()

        val docs = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepDocumentos).component
        fakeRepo.uploadResult = Result.success(UploadedDoc("d1", DocKind.CMP_LICENSE, "u1"))
        docs.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1), "cmp.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d2", DocKind.ID_FRONT, "u2"))
        docs.onPickDocument(DocKind.ID_FRONT, byteArrayOf(2), "id_front.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d3", DocKind.ID_BACK, "u3"))
        docs.onPickDocument(DocKind.ID_BACK, byteArrayOf(3), "id_back.pdf")
        docs.onContinueClicked()

        val esp = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepEspecialidades).component
        esp.onToggleSpecialty("cardiology")
        esp.onContinueClicked()

        val hor = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepHorarios).component
        hor.onToggleDay("MONDAY")
        hor.onToggleSlot("MONDAY", 9)
        hor.onContinueClicked()

        val precios = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepPrecios).component
        precios.onConsultationFeeChanged("150")
        precios.onContinueClicked()

        assertIs<DoctorOnboardingComponent.Child.Enviado>(c.stack.value.active.instance)
    }

    // ── Output ────────────────────────────────────────────────────────────────

    @Test
    fun enviado_logout_emits_NavigateOutToLogin_output() = runTest {
        fakeRepo.submitResult = Result.success(Unit)
        var output: DoctorOnboardingComponent.Output? = null
        val c = makeComponent(onOutput = { output = it })

        // Navigate to Enviado first
        val datos = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepDatos).component
        datos.onFirstNameChanged("Juan"); datos.onLastNameChanged("Perez")
        datos.onCmpLicenseChanged("CMP-12345"); datos.onPhoneChanged("987654321")
        datos.onContinueClicked()

        val docs = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepDocumentos).component
        fakeRepo.uploadResult = Result.success(UploadedDoc("d1", DocKind.CMP_LICENSE, "u1"))
        docs.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1), "cmp.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d2", DocKind.ID_FRONT, "u2"))
        docs.onPickDocument(DocKind.ID_FRONT, byteArrayOf(2), "id_front.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d3", DocKind.ID_BACK, "u3"))
        docs.onPickDocument(DocKind.ID_BACK, byteArrayOf(3), "id_back.pdf")
        docs.onContinueClicked()

        val esp = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepEspecialidades).component
        esp.onToggleSpecialty("cardiology"); esp.onContinueClicked()

        val hor = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepHorarios).component
        hor.onToggleDay("MONDAY"); hor.onToggleSlot("MONDAY", 9); hor.onContinueClicked()

        val precios = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.StepPrecios).component
        precios.onConsultationFeeChanged("150"); precios.onContinueClicked()

        val enviado = (c.stack.value.active.instance as DoctorOnboardingComponent.Child.Enviado).component
        enviado.onLogOutClicked()

        assertNotNull(output)
        assertIs<DoctorOnboardingComponent.Output.NavigateOutToLogin>(output)
    }
}
