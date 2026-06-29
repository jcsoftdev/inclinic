package com.inclinic.app.features.patient.assistant.application

import app.cash.turbine.test
import com.inclinic.app.features.patient.assistant.core.model.AssistantChatRequest
import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * RED → GREEN tests for [SendAssistantMessageUseCase].
 *
 * Uses [FakeAssistantChatDataSource] to verify:
 * 1. Blank/empty message emits [AssistantStreamEvent.Error] with code "EMPTY_MESSAGE"
 *    and does NOT call the data source.
 * 2. Valid message with no conversationId delegates to data source with (message, null).
 * 3. Valid message with conversationId delegates to data source with that id.
 */
class SendAssistantMessageUseCaseTest {

    private val fake = FakeAssistantChatDataSource()
    private val useCase = SendAssistantMessageUseCase(fake)

    @Test
    fun blank_message_emits_EMPTY_MESSAGE_error_without_calling_data_source() = runTest {
        useCase("   ", null).test {
            val event = awaitItem()
            assertIs<AssistantStreamEvent.Error>(event)
            assertEquals("EMPTY_MESSAGE", event.code)
            awaitComplete()
        }
        assertNull(fake.lastMessage, "data source must NOT be called for blank input")
    }

    @Test
    fun empty_string_message_emits_EMPTY_MESSAGE_error_without_calling_data_source() = runTest {
        useCase("", null).test {
            val event = awaitItem()
            assertIs<AssistantStreamEvent.Error>(event)
            assertEquals("EMPTY_MESSAGE", event.code)
            awaitComplete()
        }
        assertNull(fake.lastMessage)
    }

    @Test
    fun valid_message_without_conversationId_delegates_to_data_source_with_null() = runTest {
        fake.eventsToEmit = listOf(AssistantStreamEvent.Finish)
        useCase("Hola", null).test {
            awaitItem() // Finish
            awaitComplete()
        }
        assertEquals("Hola", fake.lastMessage)
        assertNull(fake.lastConversationId)
    }

    @Test
    fun valid_message_with_conversationId_delegates_correct_id() = runTest {
        val expectedConvId = "conv-123"
        fake.eventsToEmit = listOf(
            AssistantStreamEvent.TextDelta("hi"),
            AssistantStreamEvent.Finish,
        )
        useCase("Buenos días", expectedConvId).test {
            assertIs<AssistantStreamEvent.TextDelta>(awaitItem())
            assertIs<AssistantStreamEvent.Finish>(awaitItem())
            awaitComplete()
        }
        assertEquals("Buenos días", fake.lastMessage)
        assertEquals(expectedConvId, fake.lastConversationId)
    }

    @Test
    fun valid_message_forwards_all_events_from_data_source() = runTest {
        val convIdEvent = AssistantStreamEvent.ConversationIdReceived("conv-xyz")
        val textDelta = AssistantStreamEvent.TextDelta("Respuesta")
        val finish = AssistantStreamEvent.Finish
        fake.eventsToEmit = listOf(convIdEvent, textDelta, finish)

        useCase("Tengo fiebre", null).test {
            assertEquals(convIdEvent, awaitItem())
            assertEquals(textDelta, awaitItem())
            assertEquals(finish, awaitItem())
            awaitComplete()
        }
    }
}

/**
 * Fake [AssistantChatDataSource] that records the last call args and emits scripted events.
 */
class FakeAssistantChatDataSource : AssistantChatDataSource {
    var lastMessage: String? = null
    var lastConversationId: String? = null
    var eventsToEmit: List<AssistantStreamEvent> = emptyList()

    override suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent> = flow {
        lastMessage = request.message
        lastConversationId = request.conversationId
        eventsToEmit.forEach { emit(it) }
    }
}
