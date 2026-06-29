package com.inclinic.app.features.doctor.messages.fakes

import com.inclinic.app.features.doctor.messages.core.model.ChatThread
import com.inclinic.app.features.doctor.messages.core.port.DoctorMessagesRepository
import kotlin.time.Instant

class FakeDoctorMessagesRepository : DoctorMessagesRepository {

    var listThreadsResult: Result<List<ChatThread>> = Result.success(emptyList())

    var listCallCount = 0

    override suspend fun listThreads(): Result<List<ChatThread>> {
        listCallCount++
        return listThreadsResult
    }
}

fun stubThread(id: String, unread: Boolean = false) = ChatThread(
    id = id,
    otherPartyId = "p-$id",
    otherPartyName = "Paciente $id",
    lastMessage = "Hola doctor",
    lastAt = Instant.fromEpochMilliseconds(0),
    unread = unread,
)
