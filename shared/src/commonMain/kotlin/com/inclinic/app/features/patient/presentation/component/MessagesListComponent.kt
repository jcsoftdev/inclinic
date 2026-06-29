package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Conversation

interface MessagesListComponent {
    val state: Value<MessagesListState>

    fun onRefresh()
    fun onConversationClick(conversationId: String)

    sealed interface Output {
        data class NavigateToChat(val doctorId: String, val doctorName: String) : Output
    }
}

data class MessagesListState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
