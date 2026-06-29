package com.inclinic.app.features.doctor.messages.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.messages.core.model.ChatThread
import com.inclinic.app.features.doctor.messages.core.port.ThreadFilter

interface DoctorChatListComponent {
    val state: Value<DoctorChatListState>

    fun onRefresh()
    fun onFilterChange(filter: ThreadFilter)
    fun onThreadClick(threadId: String)
    fun onBack()

    sealed interface Output {
        data class NavigateToConversation(val threadId: String) : Output
        data object Back : Output
    }
}

data class DoctorChatListState(
    val isLoading: Boolean = false,
    val threads: List<ChatThread> = emptyList(),
    val activeFilter: ThreadFilter = ThreadFilter.ALL,
    val error: String? = null,
)
