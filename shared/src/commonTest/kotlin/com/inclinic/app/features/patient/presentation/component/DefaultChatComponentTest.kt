@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.model.Conversation
import com.inclinic.app.core.model.SenderRole
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.chat.application.GetChatMessagesUseCase
import com.inclinic.app.features.patient.chat.application.SendChatMessageUseCase
import com.inclinic.app.features.patient.infrastructure.remote.ChatDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun chatTestMessage(id: String = "msg-1", text: String = "Hello"): ChatMessage {
    val now = Clock.System.now()
    return ChatMessage(
        id = id, appointmentId = "apt-1", senderId = "pat-1",
        senderRole = SenderRole.PATIENT, text = text, sentAt = now, readAt = null,
    )
}

private class FakeDirectChatDataSource(
    private val messages: List<ChatMessage> = listOf(chatTestMessage()),
    private var sendResult: Result<ChatMessage> = Result.success(chatTestMessage("msg-2", "Reply")),
) : ChatDataSource {
    var sendCallCount = 0
    var lastSentContent: String? = null

    override suspend fun getMessages(appointmentId: String): Result<List<ChatMessage>> =
        Result.success(messages)

    override suspend fun sendMessage(
        appointmentId: String,
        content: String,
        attachments: List<String>,
    ): Result<ChatMessage> {
        sendCallCount++
        lastSentContent = content
        return sendResult
    }

    override suspend fun getConversations(): Result<List<Conversation>> =
        Result.success(emptyList())

    override suspend fun uploadAttachment(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto> =
        Result.success(
            com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto(
                url = "https://example.com/$fileName", path = fileName,
                bucket = "medical-attachments", size = bytes.size.toLong(), type = mimeType,
            ),
        )

    fun setSendResult(result: Result<ChatMessage>) { sendResult = result }
}

class DefaultChatComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeDirectChatDataSource = FakeDirectChatDataSource(),
    ): DefaultChatComponent {
        return DefaultChatComponent(
            componentContext = ctx,
            doctorId = "doc-1",
            doctorName = "Dr. Ana Torres",
            getMessages = GetChatMessagesUseCase(dataSource, dispatchers),
            sendMessage = SendChatMessageUseCase(dataSource, dispatchers),
            uploadAttachment = com.inclinic.app.features.patient.chat.application.UploadChatAttachmentUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
        )
    }

    @Test
    fun initial_state_has_empty_input_and_no_error() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertEquals("", state.inputText)
        assertFalse(state.isSending)
        assertNull(state.error)
    }

    @Test
    fun onInputChange_updates_inputText() = runTest {
        val component = createComponent()

        component.onInputChange("Hello doctor")

        assertEquals("Hello doctor", component.state.value.inputText)
    }

    @Test
    fun onSend_with_blank_input_does_nothing() = runTest {
        val ds = FakeDirectChatDataSource()
        val component = createComponent(dataSource = ds)
        component.onInputChange("   ")

        component.onSend()

        assertEquals(0, ds.sendCallCount)
    }

    @Test
    fun onSend_success_clears_input_and_appends_message() = runTest {
        val ds = FakeDirectChatDataSource()
        val component = createComponent(dataSource = ds)
        component.onInputChange("Need help")

        component.onSend()

        assertEquals(1, ds.sendCallCount)
        assertEquals("Need help", ds.lastSentContent)
        assertEquals("", component.state.value.inputText)
        assertFalse(component.state.value.isSending)
        assertNull(component.state.value.error)
    }

    @Test
    fun onSend_failure_sets_error_and_clears_isSending() = runTest {
        val ds = FakeDirectChatDataSource()
        ds.setSendResult(Result.failure(Exception("Send failed")))
        val component = createComponent(dataSource = ds)
        component.onInputChange("Hello")

        component.onSend()

        assertFalse(component.state.value.isSending)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onSend_with_empty_input_after_trim_is_ignored() = runTest {
        val ds = FakeDirectChatDataSource()
        val component = createComponent(dataSource = ds)
        component.onInputChange("")
        component.onSend()

        assertEquals(0, ds.sendCallCount)
    }
}
