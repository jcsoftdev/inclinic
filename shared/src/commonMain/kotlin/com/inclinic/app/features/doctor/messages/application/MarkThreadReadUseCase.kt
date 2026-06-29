package com.inclinic.app.features.doctor.messages.application

/**
 * No backend endpoint exists for marking a thread as read independently.
 * The backend marks threads as read when messages are fetched (getMessages).
 * This stub is retained for interface compatibility; actual read-marking
 * happens server-side when the conversation is opened (DoctorChatScreen).
 */
class MarkThreadReadUseCase {
    // No-op: read state is updated server-side when getMessages is called
}
