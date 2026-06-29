package com.inclinic.app.features.patient.messages.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Conversation
import com.inclinic.app.features.patient.infrastructure.remote.ChatDataSource
import kotlinx.coroutines.withContext

class GetConversationsUseCase(
    private val dataSource: ChatDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<Conversation>> =
        withContext(dispatchers.io) { dataSource.getConversations() }
}
