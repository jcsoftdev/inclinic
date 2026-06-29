package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.application.UploadDocumentUseCase
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import com.inclinic.app.ui.molecules.DocUploadState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultStepDocumentosComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorOnboardingRepository()

    private fun uploadUseCase() = UploadDocumentUseCase(fakeRepo, dispatchers)

    private fun makeComponent(
        onContinue: (List<UploadedDoc>) -> Unit = {},
    ) = DefaultStepDocumentosComponent(context, uploadUseCase(), dispatchers, onContinue)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_all_docs_empty() {
        val c = makeComponent()
        val s = c.state.value
        assertIs<DocUploadState.Empty>(s.cmpState)
        assertIs<DocUploadState.Empty>(s.idFrontState)
        assertIs<DocUploadState.Empty>(s.idBackState)
        assertFalse(s.allUploaded)
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    @Test
    fun onPickDocument_cmp_success_marks_cmp_as_done() = runTest {
        fakeRepo.uploadResult = Result.success(
            UploadedDoc("doc-cmp", DocKind.CMP_LICENSE, "https://cdn.test/cmp")
        )
        val c = makeComponent()
        c.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1, 2, 3), "cmp.pdf")
        assertIs<DocUploadState.Done>(c.state.value.cmpState)
        assertEquals("cmp.pdf", (c.state.value.cmpState as DocUploadState.Done).fileName)
    }

    @Test
    fun onPickDocument_tracks_uploaded_doc_in_list() = runTest {
        val uploaded = UploadedDoc("doc-cmp", DocKind.CMP_LICENSE, "https://cdn.test/cmp")
        fakeRepo.uploadResult = Result.success(uploaded)
        val c = makeComponent()
        c.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1), "cmp.pdf")
        assertTrue(c.state.value.uploadedDocs.contains(uploaded))
    }

    @Test
    fun onPickDocument_failure_marks_doc_as_error() = runTest {
        fakeRepo.uploadResult = Result.failure(RuntimeException("Network error"))
        val c = makeComponent()
        c.onPickDocument(DocKind.ID_FRONT, byteArrayOf(1), "id_front.pdf")
        assertIs<DocUploadState.Error>(c.state.value.idFrontState)
    }

    @Test
    fun upload_increments_upload_call_count() = runTest {
        val c = makeComponent()
        c.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1), "cmp.pdf")
        assertEquals(1, fakeRepo.uploadCallCount)
    }

    // ── Continue validation ───────────────────────────────────────────────────

    @Test
    fun onContinueClicked_without_all_docs_sets_error() = runTest {
        val c = makeComponent()
        c.onContinueClicked()
        assertNotNull(c.state.value.error)
        assertFalse(c.state.value.allUploaded)
    }

    @Test
    fun onContinueClicked_with_all_docs_invokes_callback() = runTest {
        // Upload all 3 docs
        fakeRepo.uploadResult = Result.success(
            UploadedDoc("d1", DocKind.CMP_LICENSE, "u1")
        )
        val c = makeComponent(onContinue = { docs ->
            assertEquals(3, docs.size)
        })
        fakeRepo.uploadResult = Result.success(UploadedDoc("d1", DocKind.CMP_LICENSE, "u1"))
        c.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1), "cmp.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d2", DocKind.ID_FRONT, "u2"))
        c.onPickDocument(DocKind.ID_FRONT, byteArrayOf(2), "id_front.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d3", DocKind.ID_BACK, "u3"))
        c.onPickDocument(DocKind.ID_BACK, byteArrayOf(3), "id_back.pdf")

        assertTrue(c.state.value.allUploaded)
        var callbackInvoked = false
        val c2 = DefaultStepDocumentosComponent(
            DefaultComponentContext(LifecycleRegistry().also { it.resume() }),
            uploadUseCase(),
            dispatchers,
        ) { _ -> callbackInvoked = true }
        fakeRepo.uploadResult = Result.success(UploadedDoc("d1", DocKind.CMP_LICENSE, "u1"))
        c2.onPickDocument(DocKind.CMP_LICENSE, byteArrayOf(1), "cmp.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d2", DocKind.ID_FRONT, "u2"))
        c2.onPickDocument(DocKind.ID_FRONT, byteArrayOf(2), "id_front.pdf")
        fakeRepo.uploadResult = Result.success(UploadedDoc("d3", DocKind.ID_BACK, "u3"))
        c2.onPickDocument(DocKind.ID_BACK, byteArrayOf(3), "id_back.pdf")
        c2.onContinueClicked()
        assertTrue(callbackInvoked)
    }

    // ── Error dismissed ───────────────────────────────────────────────────────

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val c = makeComponent()
        c.onContinueClicked() // trigger error
        assertNotNull(c.state.value.error)
        c.onErrorDismissed()
        assertNull(c.state.value.error)
    }
}
