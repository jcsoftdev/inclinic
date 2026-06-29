package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val senderRole: SenderRole,
    val text: String,
    val sentAt: Instant,
    // El backend party-based (chat por doctorId) no devuelve estos campos.
    val appointmentId: String? = null,
    val senderId: String = "",
    val readAt: Instant? = null,
    // URLs de adjuntos (imágenes / PDF) subidos vía /api/upload. Máx 5 por mensaje.
    val attachments: List<String> = emptyList(),
)

@Serializable
enum class SenderRole { PATIENT, DOCTOR }
