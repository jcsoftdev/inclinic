package com.inclinic.app.features.doctor.chat.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorChatDataSource
import kotlinx.coroutines.withContext

class SendDoctorChatMessageUseCase(
    private val dataSource: DoctorChatDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointmentId: String,
        text: String,
        attachments: List<String> = emptyList(),
    ): Result<ChatMessage> =
        withContext(dispatchers.io) { dataSource.sendMessage(appointmentId, text, attachments) }
}
