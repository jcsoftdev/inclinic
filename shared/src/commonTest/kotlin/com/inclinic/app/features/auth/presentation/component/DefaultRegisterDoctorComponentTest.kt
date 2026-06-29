package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Specialty
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.core.upload.FakeUploadDataSource
import com.inclinic.app.core.upload.UploadFileUseCase
import com.inclinic.app.features.auth.application.GetSpecialtiesUseCase
import com.inclinic.app.features.auth.application.RegisterFreelanceDoctorUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto
import com.inclinic.app.features.auth.infrastructure.local.SpecialtyCacheDataSource
import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [DefaultRegisterDoctorComponent].
 *
 * Covers:
 * - Initial state
 * - Step 1 → 2 navigation (valid + invalid)
 * - Step 2 → 3 (specialty + price validation)
 * - Step 3 → 4 (documents validation)
 * - Step 4 → 5 (schedules)
 * - Back navigation at each step
 * - Submit happy path (success → Output.Success)
 * - Submit remote error propagation
 */
class DefaultRegisterDoctorComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRemote = FakeAuthRemoteDataSource()
    private val fakeUpload = FakeUploadDataSource()

    private fun makeComponent(
        localDispatchers: com.inclinic.app.core.concurrency.AppDispatchers = dispatchers,
        onOutput: (RegisterDoctorComponent.Output) -> Unit = {},
    ): DefaultRegisterDoctorComponent {
        val useCase = RegisterFreelanceDoctorUseCase(fakeRemote, localDispatchers)
        val specialtiesUseCase = GetSpecialtiesUseCase(
            cache = SpecialtyCacheDataSource(remote = fakeRemote),
            dispatchers = localDispatchers,
        )
        val uploadUseCase = UploadFileUseCase(dataSource = fakeUpload, dispatchers = localDispatchers)
        return DefaultRegisterDoctorComponent(
            componentContext = context,
            registerFreelanceUseCase = useCase,
            getSpecialtiesUseCase = specialtiesUseCase,
            uploadFileUseCase = uploadUseCase,
            dispatchers = localDispatchers,
            onOutput = onOutput,
        )
    }

    private fun fillStep1(c: DefaultRegisterDoctorComponent) {
        c.onFirstNameChanged("Patricia")
        c.onLastNameChanged("Huaman")
        c.onEmailChanged("patricia@test.com")
        c.onPhoneChanged("987654321")
    }

    private fun fillStep2(c: DefaultRegisterDoctorComponent) {
        c.onToggleSpecialty("sp-1")
        c.onPrimarySpecialtySelected("sp-1")
        c.onConsultationPriceChanged("80")
    }

    private val sampleSchedule = FreelanceScheduleDto(
        dayOfWeek = "MONDAY",
        startTime = "09:00",
        endTime = "17:00",
    )

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_step_is_PersonalData() {
        val c = makeComponent()
        assertEquals(RegisterDoctorState.Step.PersonalData, c.state.value.step)
    }

    @Test
    fun initial_state_all_fields_empty_and_no_errors() {
        val c = makeComponent()
        val s = c.state.value
        assertEquals("", s.firstName)
        assertEquals("", s.email)
        assertNull(s.firstNameError)
        assertNull(s.serverError)
        assertTrue(s.documentUrls.isEmpty())
        assertTrue(s.schedules.isEmpty())
    }

    // ── Step 1 navigation ─────────────────────────────────────────────────────

    @Test
    fun onNextStep_from_PersonalData_with_valid_data_advances_to_SpecialtyAndPrice() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        assertEquals(RegisterDoctorState.Step.SpecialtyAndPrice, c.state.value.step)
    }

    @Test
    fun onNextStep_with_blank_firstName_sets_firstNameError_stays_on_step1() = runTest {
        val c = makeComponent()
        c.onFirstNameChanged("P") // too short
        c.onLastNameChanged("Huaman")
        c.onEmailChanged("p@test.com")
        c.onPhoneChanged("987654321")
        c.onNextStep()
        assertEquals(RegisterDoctorState.Step.PersonalData, c.state.value.step)
        assertNotNull(c.state.value.firstNameError)
    }

    @Test
    fun onNextStep_with_invalid_email_sets_emailError() = runTest {
        val c = makeComponent()
        c.onFirstNameChanged("Patricia")
        c.onLastNameChanged("Huaman")
        c.onEmailChanged("not-an-email")
        c.onPhoneChanged("987654321")
        c.onNextStep()
        assertNotNull(c.state.value.emailError)
        assertEquals(RegisterDoctorState.Step.PersonalData, c.state.value.step)
    }

    @Test
    fun onNextStep_with_short_phone_sets_phoneError() = runTest {
        val c = makeComponent()
        c.onFirstNameChanged("Patricia")
        c.onLastNameChanged("Huaman")
        c.onEmailChanged("p@test.com")
        c.onPhoneChanged("123") // too short
        c.onNextStep()
        assertNotNull(c.state.value.phoneError)
    }

    @Test
    fun onFirstNameChanged_clears_firstNameError() = runTest {
        val c = makeComponent()
        c.onNextStep() // trigger errors
        c.onFirstNameChanged("Patricia")
        assertNull(c.state.value.firstNameError)
    }

    // ── Step 2 navigation ─────────────────────────────────────────────────────

    @Test
    fun onNextStep_from_SpecialtyAndPrice_with_no_specialty_sets_error() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep() // → step 2
        c.onConsultationPriceChanged("80")
        c.onNextStep() // should fail — no specialty
        assertEquals(RegisterDoctorState.Step.SpecialtyAndPrice, c.state.value.step)
        assertNotNull(c.state.value.specialtyError)
    }

    @Test
    fun onNextStep_from_SpecialtyAndPrice_with_price_below_50_sets_priceError() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        c.onToggleSpecialty("sp-1")
        c.onConsultationPriceChanged("30")
        c.onNextStep()
        assertNotNull(c.state.value.priceError)
        assertEquals(RegisterDoctorState.Step.SpecialtyAndPrice, c.state.value.step)
    }

    @Test
    fun onNextStep_from_SpecialtyAndPrice_with_valid_data_advances_to_Documents() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        fillStep2(c)
        c.onNextStep()
        assertEquals(RegisterDoctorState.Step.Documents, c.state.value.step)
    }

    // ── Step 3 navigation ─────────────────────────────────────────────────────

    @Test
    fun onNextStep_from_Documents_with_no_docs_sets_documentError() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        fillStep2(c)
        c.onNextStep()
        c.onNextStep() // no docs — should fail
        assertNotNull(c.state.value.documentError)
        assertEquals(RegisterDoctorState.Step.Documents, c.state.value.step)
    }

    @Test
    fun onDocumentUploaded_adds_url_and_clears_documentError() = runTest {
        val c = makeComponent()
        c.onDocumentUploaded("https://cdn/doc.pdf")
        assertTrue(c.state.value.documentUrls.contains("https://cdn/doc.pdf"))
        assertNull(c.state.value.documentError)
    }

    @Test
    fun onNextStep_from_Documents_with_doc_advances_to_Schedules() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        fillStep2(c)
        c.onNextStep()
        c.onDocumentUploaded("https://cdn/doc.pdf")
        c.onNextStep()
        assertEquals(RegisterDoctorState.Step.Schedules, c.state.value.step)
    }

    // ── Step 4 navigation ─────────────────────────────────────────────────────

    @Test
    fun onNextStep_from_Schedules_advances_to_Review() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        fillStep2(c)
        c.onNextStep()
        c.onDocumentUploaded("https://cdn/doc.pdf")
        c.onNextStep()
        c.onScheduleAdded(sampleSchedule)
        c.onNextStep()
        assertEquals(RegisterDoctorState.Step.Review, c.state.value.step)
    }

    @Test
    fun onScheduleAdded_adds_schedule() {
        val c = makeComponent()
        c.onScheduleAdded(sampleSchedule)
        assertEquals(1, c.state.value.schedules.size)
        assertEquals("MONDAY", c.state.value.schedules.first().dayOfWeek)
    }

    @Test
    fun onScheduleRemoved_removes_schedule_by_index() {
        val c = makeComponent()
        c.onScheduleAdded(sampleSchedule)
        c.onScheduleAdded(sampleSchedule.copy(dayOfWeek = "TUESDAY"))
        c.onScheduleRemoved(0)
        assertEquals(1, c.state.value.schedules.size)
        assertEquals("TUESDAY", c.state.value.schedules.first().dayOfWeek)
    }

    // ── Back navigation ───────────────────────────────────────────────────────

    @Test
    fun onBack_from_PersonalData_emits_Back_output() = runTest {
        var output: RegisterDoctorComponent.Output? = null
        val c = makeComponent(onOutput = { output = it })
        c.onBack()
        assertIs<RegisterDoctorComponent.Output.Back>(output)
    }

    @Test
    fun onBack_from_SpecialtyAndPrice_returns_to_PersonalData() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        assertEquals(RegisterDoctorState.Step.SpecialtyAndPrice, c.state.value.step)
        c.onBack()
        assertEquals(RegisterDoctorState.Step.PersonalData, c.state.value.step)
    }

    @Test
    fun onBack_from_Documents_returns_to_SpecialtyAndPrice() = runTest {
        val c = makeComponent()
        fillStep1(c)
        c.onNextStep()
        fillStep2(c)
        c.onNextStep()
        c.onBack()
        assertEquals(RegisterDoctorState.Step.SpecialtyAndPrice, c.state.value.step)
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    @Test
    fun onSubmit_success_emits_Success_output_with_email() = runTest {
        fakeRemote.registerFreelanceDoctorResult = Result.success(Unit)
        var output: RegisterDoctorComponent.Output? = null
        val c = makeComponent(onOutput = { output = it })

        fillStep1(c)
        c.onNextStep()
        fillStep2(c)
        c.onNextStep()
        c.onDocumentUploaded("https://cdn/doc.pdf")
        c.onNextStep()
        c.onScheduleAdded(sampleSchedule)
        c.onNextStep() // → Review
        c.onSubmit()

        assertIs<RegisterDoctorComponent.Output.Success>(output)
        assertEquals("patricia@test.com", (output as RegisterDoctorComponent.Output.Success).email)
    }

    @Test
    fun onSubmit_remote_failure_sets_serverError_does_not_emit_output() = runTest {
        fakeRemote.registerFreelanceDoctorResult = Result.failure(AuthError.NetworkError)
        var output: RegisterDoctorComponent.Output? = null
        val c = makeComponent(onOutput = { output = it })

        fillStep1(c)
        c.onNextStep()
        fillStep2(c)
        c.onNextStep()
        c.onDocumentUploaded("https://cdn/doc.pdf")
        c.onNextStep()
        c.onScheduleAdded(sampleSchedule)
        c.onNextStep()
        c.onSubmit()

        assertIs<AuthError.NetworkError>(c.state.value.serverError)
        assertNull(output)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun onSubmit_while_loading_is_idempotent() = runTest {
        // Use StandardTestDispatcher so the first submit's coroutine doesn't complete
        // eagerly before the second submit is called — preserving the isLoading guard.
        val stdDispatchers = TestAppDispatchers(scheduler = testScheduler, useStandard = true)
        fakeRemote.registerFreelanceDoctorResult = Result.success(Unit)
        val c = makeComponent(localDispatchers = stdDispatchers)
        fillStep1(c); c.onNextStep()
        fillStep2(c); c.onNextStep()
        c.onDocumentUploaded("https://cdn/doc.pdf"); c.onNextStep()
        c.onScheduleAdded(sampleSchedule); c.onNextStep()
        c.onSubmit()           // sets isLoading = true, queues coroutine
        c.onSubmit()           // blocked by isLoading = true guard
        testScheduler.advanceUntilIdle() // run the first submit's coroutine
        assertEquals(1, fakeRemote.registerFreelanceDoctorCallCount)
    }

    // ── Toggle specialty ──────────────────────────────────────────────────────

    @Test
    fun onToggleSpecialty_adds_and_removes() {
        val c = makeComponent()
        c.onToggleSpecialty("sp-1")
        assertTrue("sp-1" in c.state.value.selectedSpecialtyIds)
        c.onToggleSpecialty("sp-1")
        assertTrue("sp-1" !in c.state.value.selectedSpecialtyIds)
    }

    @Test
    fun removing_primary_specialty_clears_primarySpecialtyId() {
        val c = makeComponent()
        c.onPrimarySpecialtySelected("sp-1")
        assertEquals("sp-1", c.state.value.primarySpecialtyId)
        c.onToggleSpecialty("sp-1") // remove it
        assertNull(c.state.value.primarySpecialtyId)
    }

    // ── File picker — real upload (bucket: documents) ─────────────────────────

    @Test
    fun onDocumentFilePicked_successful_upload_adds_url_not_filename() = runTest {
        fakeUpload.result = Result.success(
            UploadResultDto(
                url = "https://cdn.inclinic.com/documents/cert.pdf",
                path = "documents/cert.pdf",
                bucket = "documents",
                size = 1024L,
                type = "application/pdf",
            )
        )
        val c = makeComponent()
        val file = PickedFile(byteArrayOf(1, 2, 3), "colegiatura.pdf", "application/pdf")

        c.onDocumentFilePicked(file)

        val urls = c.state.value.documentUrls
        assertTrue(urls.contains("https://cdn.inclinic.com/documents/cert.pdf"),
            "Expected the server URL, got: $urls")
        assertFalse(urls.contains("colegiatura.pdf"),
            "Should NOT contain the local filename placeholder")
    }

    @Test
    fun onDocumentFilePicked_shows_uploading_then_done_state() = runTest {
        fakeUpload.result = Result.success(
            UploadResultDto(
                url = "https://cdn.inclinic.com/documents/id.jpg",
                path = "documents/id.jpg",
                bucket = "documents",
                size = 512L,
                type = "image/jpeg",
            )
        )
        val c = makeComponent()
        val file = PickedFile(byteArrayOf(1), "id.jpg", "image/jpeg")

        c.onDocumentFilePicked(file)

        // After upload completes the URL must be present and upload must not be in progress
        assertFalse(c.state.value.isDocumentUploading)
        assertEquals(1, c.state.value.documentUrls.size)
    }

    @Test
    fun onDocumentFilePicked_upload_failure_sets_documentUploadError() = runTest {
        fakeUpload.result = Result.failure(RuntimeException("Network error"))
        val c = makeComponent()
        val file = PickedFile(byteArrayOf(1), "id.jpg", "image/jpeg")

        c.onDocumentFilePicked(file)

        assertFalse(c.state.value.isDocumentUploading)
        assertTrue(c.state.value.documentUrls.isEmpty())
        assertNotNull(c.state.value.documentUploadError)
    }

    @Test
    fun onDocumentFilePicked_uses_correct_bucket() = runTest {
        val c = makeComponent()
        val file = PickedFile(byteArrayOf(1), "doc.pdf", "application/pdf")

        c.onDocumentFilePicked(file)

        assertEquals("documents", fakeUpload.lastBucket)
    }

    @Test
    fun onDocumentFilePicked_multiple_files_all_uploaded_as_urls() = runTest {
        var urlIndex = 0
        val urls = listOf("https://cdn/doc1.pdf", "https://cdn/doc2.pdf")
        fakeUpload.result = Result.success(
            UploadResultDto(url = urls[0], path = "p", bucket = "documents", size = 1L, type = "application/pdf")
        )
        val c = makeComponent()

        c.onDocumentFilePicked(PickedFile(byteArrayOf(1), "doc1.pdf", "application/pdf"))
        fakeUpload.result = Result.success(
            UploadResultDto(url = urls[1], path = "p", bucket = "documents", size = 1L, type = "application/pdf")
        )
        c.onDocumentFilePicked(PickedFile(byteArrayOf(2), "doc2.pdf", "application/pdf"))

        assertEquals(2, c.state.value.documentUrls.size)
        assertTrue(c.state.value.documentUrls.containsAll(urls))
    }
}
