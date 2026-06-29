package com.inclinic.app.features.doctor.messages.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.messages.core.model.ChatThread
import com.inclinic.app.features.doctor.messages.core.port.DoctorMessagesRepository
import com.inclinic.app.features.doctor.messages.core.port.ThreadFilter
import kotlinx.coroutines.withContext

class GetDoctorChatThreadsUseCase(
    private val repository: DoctorMessagesRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(filter: ThreadFilter = ThreadFilter.ALL): Result<List<ChatThread>> =
        withContext(dispatchers.io) {
            repository.listThreads().map { threads ->
                when (filter) {
                    ThreadFilter.UNREAD -> threads.filter { it.unread }
                    ThreadFilter.ALL -> threads
                }
            }
        }
}
