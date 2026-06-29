package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.ChatMessage

interface DoctorChatDataSource {
    suspend fun getMessages(appointmentId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(appointmentId: String, text: String): Result<ChatMessage>
}
