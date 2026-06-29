package com.inclinic.app.features.doctor.profile.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.core.upload.FakeUploadDataSource
import com.inclinic.app.core.upload.UploadFileUseCase
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.application.RequestSpecialtyUseCase
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultRequestSpecialtyComponent
import com.inclinic.app.features.doctor.profile.presentation.component.RequestSpecialtyComponent
import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultRequestSpecialtyComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val fakeUpload = FakeUploadDataSource()

    private fun makeComponent(
        onOutput: (RequestSpecialtyComponent.Output) -> Unit = {},
    ): DefaultRequestSpecialtyComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultRequestSpecialtyComponent(
            componentContext = ctx,
            requestSpecialty = RequestSpecialtyUseCase(fakeRepo, dispatchers),
            uploadFileUseCase = UploadFileUseCase(dataSource = fakeUpload, dispatchers = dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    private fun certResult(url: String) = Result.success(
        UploadResultDto(url = url, path = "p", bucket = "specialty-request-docs", size = 1L, type = "application/pdf")
    )

    @Test
    fun initial_state_is_blank() {
        val component = makeComponent()
        val s = component.state.value
        assertEquals("", s.specialtyName)
        assertEquals("", s.comment)
        assertTrue(s.documentUrls.isEmpty())
        assertNull(s.error)
    }

    @Test
    fun onSpecialtyNameChange_updates_state() {
        val component = makeComponent()
        component.onSpecialtyNameChange("Neurología")
        assertEquals("Neurología", component.state.value.specialtyName)
    }

    @Test
    fun onCommentChange_updates_state() {
        val component = makeComponent()
        component.onCommentChange("Tengo experiencia.")
        assertEquals("Tengo experiencia.", component.state.value.comment)
    }

    @Test
    fun onAddDocumentUrl_appends_url() {
        val component = makeComponent()
        component.onAddDocumentUrl("https://cdn.inclinic.com/doc-a")
        assertEquals(1, component.state.value.documentUrls.size)
    }

    @Test
    fun onRemoveDocumentUrl_removes_url() {
        val component = makeComponent()
        component.onAddDocumentUrl("https://cdn.inclinic.com/doc-a")
        component.onRemoveDocumentUrl("https://cdn.inclinic.com/doc-a")
        assertEquals(0, component.state.value.documentUrls.size)
    }

    @Test
    fun onSubmit_sets_error_when_specialtyName_is_blank() = runTest {
        val component = makeComponent()

        component.onSubmit()

        assertNotNull(component.state.value.error)
        assertEquals(0, fakeRepo.requestSpecialtyCallCount)
    }

    @Test
    fun onSubmit_calls_repository_with_trimmed_values() = runTest {
        val component = makeComponent()
        component.onSpecialtyNameChange("  Neurología  ")
        component.onCommentChange("  Experiencia.  ")

        component.onSubmit()

        assertEquals(1, fakeRepo.requestSpecialtyCallCount)
        assertEquals("Neurología", fakeRepo.lastSpecialtyRequest?.specialtyName)
        assertEquals("Experiencia.", fakeRepo.lastSpecialtyRequest?.comment)
    }

    @Test
    fun onSubmit_emits_Submitted_output_on_success() = runTest {
        var output: RequestSpecialtyComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })
        component.onSpecialtyNameChange("Neurología")

        component.onSubmit()

        assertTrue(output is RequestSpecialtyComponent.Output.Submitted)
    }

    @Test
    fun onSubmit_sets_error_on_failure() = runTest {
        fakeRepo.requestSpecialtyResult = Result.failure(RuntimeException("Server error"))
        val component = makeComponent()
        component.onSpecialtyNameChange("Neurología")

        component.onSubmit()

        assertNotNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() {
        var output: RequestSpecialtyComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onBack()

        assertTrue(output is RequestSpecialtyComponent.Output.Back)
    }

    // ── File picker — real upload (bucket: specialty-request-docs) ───────────

    @Test
    fun onPickCertification_successful_upload_adds_url_to_documentUrls() = runTest {
        fakeUpload.result = certResult("https://cdn.inclinic.com/specialty-request-docs/cert.pdf")
        val component = makeComponent()
        val file = PickedFile(byteArrayOf(1, 2, 3), "sunedu.pdf", "application/pdf")

        component.onPickCertification(file)

        assertTrue(
            component.state.value.documentUrls.contains("https://cdn.inclinic.com/specialty-request-docs/cert.pdf"),
            "Expected server URL in documentUrls: ${component.state.value.documentUrls}"
        )
        assertFalse(component.state.value.isCertUploading)
    }

    @Test
    fun onPickDiploma_successful_upload_adds_url_to_documentUrls() = runTest {
        fakeUpload.result = certResult("https://cdn.inclinic.com/specialty-request-docs/diploma.pdf")
        val component = makeComponent()
        val file = PickedFile(byteArrayOf(4, 5, 6), "diploma.pdf", "application/pdf")

        component.onPickDiploma(file)

        assertTrue(
            component.state.value.documentUrls.contains("https://cdn.inclinic.com/specialty-request-docs/diploma.pdf"),
        )
        assertFalse(component.state.value.isDiplomaUploading)
    }

    @Test
    fun onPickCertification_uses_correct_bucket() = runTest {
        val component = makeComponent()
        component.onPickCertification(PickedFile(byteArrayOf(1), "cert.pdf", "application/pdf"))
        assertEquals("specialty-request-docs", fakeUpload.lastBucket)
    }

    @Test
    fun onPickDiploma_uses_correct_bucket() = runTest {
        val component = makeComponent()
        component.onPickDiploma(PickedFile(byteArrayOf(1), "diploma.pdf", "application/pdf"))
        assertEquals("specialty-request-docs", fakeUpload.lastBucket)
    }

    @Test
    fun onPickCertification_upload_failure_sets_certUploadError() = runTest {
        fakeUpload.result = Result.failure(RuntimeException("Upload rejected"))
        val component = makeComponent()

        component.onPickCertification(PickedFile(byteArrayOf(1), "cert.pdf", "application/pdf"))

        assertFalse(component.state.value.isCertUploading)
        assertNotNull(component.state.value.certUploadError)
        assertTrue(component.state.value.documentUrls.isEmpty())
    }

    @Test
    fun onPickDiploma_upload_failure_sets_diplomaUploadError() = runTest {
        fakeUpload.result = Result.failure(RuntimeException("File too large"))
        val component = makeComponent()

        component.onPickDiploma(PickedFile(byteArrayOf(1), "diploma.pdf", "application/pdf"))

        assertFalse(component.state.value.isDiplomaUploading)
        assertNotNull(component.state.value.diplomaUploadError)
        assertTrue(component.state.value.documentUrls.isEmpty())
    }

    @Test
    fun both_cert_and_diploma_can_be_uploaded_independently() = runTest {
        val component = makeComponent()

        fakeUpload.result = certResult("https://cdn/cert.pdf")
        component.onPickCertification(PickedFile(byteArrayOf(1), "cert.pdf", "application/pdf"))

        fakeUpload.result = certResult("https://cdn/diploma.pdf")
        component.onPickDiploma(PickedFile(byteArrayOf(2), "diploma.pdf", "application/pdf"))

        assertEquals(2, component.state.value.documentUrls.size)
    }
}
