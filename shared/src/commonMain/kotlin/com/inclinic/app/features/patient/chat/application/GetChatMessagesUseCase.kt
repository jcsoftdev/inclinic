package com.inclinic.app.features.patient.chat.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.features.patient.infrastructure.remote.ChatDataSource
import kotlinx.coroutines.withContext

class GetChatMessagesUseCase(
    private val dataSource: ChatDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String): Result<List<ChatMessage>> =
        withContext(dispatchers.io) { dataSource.getMessages(doctorId) }
}
