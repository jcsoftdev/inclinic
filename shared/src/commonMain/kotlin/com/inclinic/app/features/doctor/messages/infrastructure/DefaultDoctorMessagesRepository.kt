package com.inclinic.app.features.doctor.messages.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.messages.core.model.ChatThread
import com.inclinic.app.features.doctor.messages.core.port.DoctorMessagesRepository
import com.inclinic.app.features.doctor.messages.infrastructure.remote.ChatThreadDto
import com.inclinic.app.features.doctor.messages.infrastructure.remote.DoctorMessagesDataSource
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class DefaultDoctorMessagesRepository(
    private val remote: DoctorMessagesDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorMessagesRepository {

    override suspend fun listThreads(): Result<List<ChatThread>> =
        withContext(dispatchers.io) {
            remote.listThreads()
                .map { list -> list.map { it.toDomain() } }
        }

    private fun ChatThreadDto.toDomain() = ChatThread(
        id = id,
        otherPartyId = otherPartyId,
        otherPartyName = otherPartyName,
        lastMessage = lastMessage,
        lastAt = runCatching { Instant.parse(lastAt) }.getOrElse { Instant.fromEpochMilliseconds(0) },
        unread = unread,
    )
}
