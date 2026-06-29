package com.inclinic.app.features.doctor.chat.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorChatDataSource
import kotlinx.coroutines.withContext

class GetDoctorChatMessagesUseCase(
    private val dataSource: DoctorChatDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(appointmentId: String): Result<List<ChatMessage>> =
        withContext(dispatchers.io) { dataSource.getMessages(appointmentId) }
}
