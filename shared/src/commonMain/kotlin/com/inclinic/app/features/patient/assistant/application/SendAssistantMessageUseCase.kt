package com.inclinic.app.features.patient.assistant.application

import com.inclinic.app.features.patient.assistant.core.model.AssistantChatRequest
import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Validates and delegates an assistant message to [AssistantChatDataSource].
 *
 * Emits [AssistantStreamEvent.Error] with code `"EMPTY_MESSAGE"` immediately
 * without calling the data source when [message] is blank.
 *
 * All downstream events from the data source are forwarded unchanged.
 */
class SendAssistantMessageUseCase(
    private val dataSource: AssistantChatDataSource,
) {
    operator fun invoke(message: String, conversationId: String?): Flow<AssistantStreamEvent> = flow {
        if (message.isBlank()) {
            emit(AssistantStreamEvent.Error("EMPTY_MESSAGE"))
            return@flow
        }
        emitAll(dataSource.sendMessage(AssistantChatRequest(message, conversationId)))
    }
}
