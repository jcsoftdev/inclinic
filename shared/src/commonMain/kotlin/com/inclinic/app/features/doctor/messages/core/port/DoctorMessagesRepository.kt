package com.inclinic.app.features.doctor.messages.core.port

import com.inclinic.app.features.doctor.messages.core.model.ChatThread

interface DoctorMessagesRepository {
    /** Lists all chat threads for the current doctor. */
    suspend fun listThreads(): Result<List<ChatThread>>
}

enum class ThreadFilter { ALL, UNREAD }
