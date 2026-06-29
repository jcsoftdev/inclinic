package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.messages.application.GetConversationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultMessagesListComponent(
    componentContext: ComponentContext,
    private val getConversations: GetConversationsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (MessagesListComponent.Output) -> Unit,
) : MessagesListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(MessagesListState())
    override val state: Value<MessagesListState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onConversationClick(conversationId: String) {
        val conversation = _state.value.conversations.firstOrNull { it.id == conversationId } ?: return
        onOutput(MessagesListComponent.Output.NavigateToChat(conversation.doctorId, conversation.doctorName))
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getConversations()
                .onSuccess { list -> _state.update { it.copy(isLoading = false, conversations = list) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar conversaciones")) } }
        }
    }
}
