@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Conversation
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.features.patient.infrastructure.remote.ChatDataSource
import com.inclinic.app.features.patient.messages.application.GetConversationsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

private fun testConversation(id: String = "conv-1") = Conversation(
    id = id,
    doctorId = "doc-1",
    doctorName = "Dr. Ana Torres",
    specialty = "Cardiología",
    lastMessage = "Hola, ¿cómo estás?",
    lastMessageAt = Clock.System.now(),
    unreadCount = 2,
)

private class FakeMessagesListChatDataSource(
    private val conversations: List<Conversation> = listOf(testConversation()),
    private val loadError: Throwable? = null,
) : ChatDataSource {
    override suspend fun getConversations(): Result<List<Conversation>> =
        if (loadError != null) Result.failure(loadError) else Result.success(conversations)

    override suspend fun getMessages(appointmentId: String): Result<List<ChatMessage>> =
        Result.success(emptyList())

    override suspend fun sendMessage(
        appointmentId: String,
        content: String,
        attachments: List<String>,
    ): Result<ChatMessage> =
        Result.failure(UnsupportedOperationException())

    override suspend fun uploadAttachment(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto> =
        Result.failure(UnsupportedOperationException())
}

class DefaultMessagesListComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: ChatDataSource = FakeMessagesListChatDataSource(),
        outputs: MutableList<MessagesListComponent.Output> = mutableListOf(),
    ): DefaultMessagesListComponent = DefaultMessagesListComponent(
        componentContext = ctx,
        getConversations = GetConversationsUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun load_success_sets_conversations_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.conversations.size)
        assertEquals("conv-1", state.conversations.first().id)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val component = createComponent(
            dataSource = FakeMessagesListChatDataSource(conversations = emptyList(), loadError = Exception("Network error")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.conversations.isEmpty())
        assertNotNull(state.error)
    }

    @Test
    fun onRefresh_reloads_conversations() = runTest {
        val component = createComponent()
        assertEquals(1, component.state.value.conversations.size)

        component.onRefresh()

        assertFalse(component.state.value.isLoading)
        assertEquals(1, component.state.value.conversations.size)
    }

    @Test
    fun onConversationClick_emits_NavigateToChat_output() = runTest {
        val outputs = mutableListOf<MessagesListComponent.Output>()
        // El fake carga "conv-1" con doctorId "doc-1" / "Dr. Ana Torres".
        val component = createComponent(outputs = outputs)

        component.onConversationClick("conv-1")

        assertEquals(1, outputs.size)
        val nav = outputs.first() as MessagesListComponent.Output.NavigateToChat
        assertEquals("doc-1", nav.doctorId)
        assertEquals("Dr. Ana Torres", nav.doctorName)
    }

    @Test
    fun multiple_conversations_all_loaded() = runTest {
        val conversations = listOf(
            testConversation("conv-1"),
            testConversation("conv-2"),
            testConversation("conv-3"),
        )
        val component = createComponent(dataSource = FakeMessagesListChatDataSource(conversations))

        assertEquals(3, component.state.value.conversations.size)
    }
}
