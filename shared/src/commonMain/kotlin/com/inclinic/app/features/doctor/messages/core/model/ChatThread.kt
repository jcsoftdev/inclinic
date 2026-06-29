package com.inclinic.app.features.doctor.messages.core.model

import kotlin.time.Instant

data class ChatThread(
    val id: String,
    /** For a DOCTOR, this is the patientId. */
    val otherPartyId: String,
    /** Display name of the other party (patient name for doctor's view). */
    val otherPartyName: String,
    val lastMessage: String?,
    val lastAt: Instant,
    val unread: Boolean,
)
