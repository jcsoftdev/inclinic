package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val specialty: String,
    val lastMessage: String,
    val lastMessageAt: Instant,
    val unreadCount: Int = 0,
    val doctorInitials: String = doctorName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString(""),
)
