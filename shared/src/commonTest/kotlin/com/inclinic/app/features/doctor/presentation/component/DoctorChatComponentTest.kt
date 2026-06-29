@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.model.SenderRole
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.core.upload.FakeUploadDataSource
import com.inclinic.app.core.upload.UploadFileUseCase
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.chat.application.GetDoctorChatMessagesUseCase
import com.inclinic.app.features.doctor.chat.application.SendDoctorChatMessageUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorChatDataSource
import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

private fun makeMessage(
    id: String = "msg-1",
    role: SenderRole = SenderRole.DOCTOR,
    text: String = "Hello",
) = ChatMessage(
    id = id,
    senderRole = role,
    text = text,
    sentAt = Clock.System.now(),
)

private class FakeDoctorChatDataSource(
    private var messages: List<ChatMessage> = emptyList(),
    private var sendResult: Result<ChatMessage> = Result.success(makeMessage()),
) : DoctorChatDataSource {
    var getCallCount = 0
    var sendCallCount = 0
    var lastSentText: String? = null
    var lastSentAttachments: List<String> = emptyList()

    override suspend fun getMessages(appointmentId: String): Result<List<ChatMessage>> {
        getCallCount++
        return Result.success(messages)
    }

    override suspend fun sendMessage(
        appointmentId: String,
        text: String,
        attachments: List<String>,
    ): Result<ChatMessage> {
        sendCallCount++
        lastSentText = text
        lastSentAttachments = attachments
        return sendResult
    }
}

class DoctorChatComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val dispatchers = TestAppDispatchers()
    private val fakeUpload = FakeUploadDataSource()

    private fun makeComponent(
        dataSource: FakeDoctorChatDataSource = FakeDoctorChatDataSource(),
        appointmentId: String = "apt-1",
    ): DoctorChatComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DoctorChatComponent(
            componentContext = ctx,
            appointmentId = appointmentId,
            getMessages = GetDoctorChatMessagesUseCase(dataSource, dispatchers),
            sendMessage = SendDoctorChatMessageUseCase(dataSource, dispatchers),
            uploadAttachment = UploadFileUseCase(dataSource = fakeUpload, dispatchers = dispatchers),
            dispatchers = dispatchers,
        )
    }

    private fun uploadResult(url: String) = Result.success(
        UploadResultDto(url = url, path = "p", bucket = "medical-attachments", size = 1L, type = "application/pdf")
    )

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_has_empty_messages_and_empty_input() = runTest {
        val component = makeComponent()

        val state = component.state.value
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.inputText)
        assertFalse(state.isSending)
        assertNull(state.error)
    }

    // ── Input handling ────────────────────────────────────────────────────────

    @Test
    fun onInputChange_updates_inputText_in_state() = runTest {
        val component = makeComponent()

        component.onInputChange("Hola doctor")

        assertEquals("Hola doctor", component.state.value.inputText)
    }

    @Test
    fun onInputChange_then_clear_sets_empty_inputText() = runTest {
        val component = makeComponent()
        component.onInputChange("some text")

        component.onInputChange("")

        assertEquals("", component.state.value.inputText)
    }

    // ── Send message ──────────────────────────────────────────────────────────

    @Test
    fun onSend_with_blank_input_does_not_call_datasource() = runTest {
        val ds = FakeDoctorChatDataSource()
        val component = makeComponent(ds)

        component.onInputChange("   ")
        component.onSend()

        assertEquals(0, ds.sendCallCount)
    }

    @Test
    fun onSend_success_appends_message_and_clears_input() = runTest {
        val sent = makeMessage(id = "new-1", role = SenderRole.DOCTOR, text = "Toma con agua")
        val ds = FakeDoctorChatDataSource(sendResult = Result.success(sent))
        val component = makeComponent(ds)
        component.onInputChange("Toma con agua")

        component.onSend()

        assertEquals("", component.state.value.inputText)
        assertTrue(component.state.value.messages.any { it.id == "new-1" })
        assertNull(component.state.value.error)
        assertFalse(component.state.value.isSending)
    }

    @Test
    fun onSend_failure_sets_error_and_clears_isSending() = runTest {
        val ds = FakeDoctorChatDataSource(
            sendResult = Result.failure(RuntimeException("Network error")),
        )
        val component = makeComponent(ds)
        component.onInputChange("Mensaje")

        component.onSend()

        assertNotNull(component.state.value.error)
        assertFalse(component.state.value.isSending)
    }

    @Test
    fun onSend_does_not_fire_again_while_isSending() = runTest {
        val ds = FakeDoctorChatDataSource()
        val component = makeComponent(ds)
        component.onInputChange("Test")

        component.onSend()

        val sentOnce = ds.sendCallCount
        assertEquals(1, sentOnce)
    }

    // ── Doctor message ownership ──────────────────────────────────────────────

    @Test
    fun sent_messages_have_DOCTOR_role() = runTest {
        val sent = makeMessage(id = "m1", role = SenderRole.DOCTOR, text = "Respuesta del doctor")
        val ds = FakeDoctorChatDataSource(sendResult = Result.success(sent))
        val component = makeComponent(ds)
        component.onInputChange("Respuesta del doctor")

        component.onSend()

        val doctorMsg = component.state.value.messages.firstOrNull { it.id == "m1" }
        assertNotNull(doctorMsg)
        assertEquals(SenderRole.DOCTOR, doctorMsg!!.senderRole)
    }

    // ── Attachment upload (real upload — bucket: medical-attachments) ─────────

    @Test
    fun onAttachmentPicked_successful_upload_stores_url_in_pendingAttachments() = runTest {
        fakeUpload.result = uploadResult("https://cdn.inclinic.com/medical-attachments/informe.pdf")
        val component = makeComponent()
        val file = PickedFile(byteArrayOf(1, 2, 3), "informe.pdf", "application/pdf")

        component.onAttachmentPicked(file)

        assertTrue(
            component.state.value.pendingAttachments.contains("https://cdn.inclinic.com/medical-attachments/informe.pdf"),
            "Expected URL, got: ${component.state.value.pendingAttachments}"
        )
        assertFalse(component.state.value.isUploading)
    }

    @Test
    fun onAttachmentPicked_uses_medical_attachments_bucket() = runTest {
        val component = makeComponent()
        component.onAttachmentPicked(PickedFile(byteArrayOf(1), "scan.jpg", "image/jpeg"))
        assertEquals("medical-attachments", fakeUpload.lastBucket)
    }

    @Test
    fun onAttachmentPicked_upload_failure_sets_error_and_does_not_add_to_pending() = runTest {
        fakeUpload.result = Result.failure(RuntimeException("Upload failed"))
        val component = makeComponent()

        component.onAttachmentPicked(PickedFile(byteArrayOf(1), "scan.jpg", "image/jpeg"))

        assertFalse(component.state.value.isUploading)
        assertTrue(component.state.value.pendingAttachments.isEmpty())
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onRemovePendingAttachment_removes_by_index() = runTest {
        fakeUpload.result = uploadResult("https://cdn/a.pdf")
        val component = makeComponent()
        component.onAttachmentPicked(PickedFile(byteArrayOf(1), "a.pdf", "application/pdf"))

        fakeUpload.result = uploadResult("https://cdn/b.pdf")
        component.onAttachmentPicked(PickedFile(byteArrayOf(2), "b.pdf", "application/pdf"))

        component.onRemovePendingAttachment(0)

        assertEquals(1, component.state.value.pendingAttachments.size)
        assertEquals("https://cdn/b.pdf", component.state.value.pendingAttachments[0])
    }

    @Test
    fun onSend_passes_pending_attachment_urls_to_datasource() = runTest {
        fakeUpload.result = uploadResult("https://cdn/report.pdf")
        val ds = FakeDoctorChatDataSource(sendResult = Result.success(makeMessage()))
        val component = makeComponent(ds)
        component.onAttachmentPicked(PickedFile(byteArrayOf(1), "report.pdf", "application/pdf"))
        component.onInputChange("See attached")

        component.onSend()

        assertTrue(ds.lastSentAttachments.contains("https://cdn/report.pdf"),
            "sendMessage should have received the upload URL, got: ${ds.lastSentAttachments}")
    }

    @Test
    fun onSend_clears_pendingAttachments_on_success() = runTest {
        fakeUpload.result = uploadResult("https://cdn/doc.pdf")
        val sent = makeMessage(id = "m2", role = SenderRole.DOCTOR, text = "ok")
        val ds = FakeDoctorChatDataSource(sendResult = Result.success(sent))
        val component = makeComponent(ds)
        component.onAttachmentPicked(PickedFile(byteArrayOf(1), "doc.pdf", "application/pdf"))
        component.onInputChange("ok")

        component.onSend()

        assertTrue(component.state.value.pendingAttachments.isEmpty())
    }
}
