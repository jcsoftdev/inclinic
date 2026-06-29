package com.inclinic.app.features.patient.chat.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.features.patient.infrastructure.remote.ChatDataSource
import kotlinx.coroutines.withContext

class SendChatMessageUseCase(
    private val dataSource: ChatDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        doctorId: String,
        content: String,
        attachments: List<String> = emptyList(),
    ): Result<ChatMessage> =
        withContext(dispatchers.io) { dataSource.sendMessage(doctorId, content, attachments) }
}
