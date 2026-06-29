package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.features.patient.chat.application.GetChatMessagesUseCase
import com.inclinic.app.features.patient.chat.application.SendChatMessageUseCase
import com.inclinic.app.features.patient.chat.application.UploadChatAttachmentUseCase
import com.inclinic.app.features.patient.chat.infrastructure.ChatPollingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultChatComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val doctorName: String,
    private val getMessages: GetChatMessagesUseCase,
    private val sendMessage: SendChatMessageUseCase,
    private val uploadAttachment: UploadChatAttachmentUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ChatComponent.Output) -> Unit = {},
) : ChatComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ChatState(doctorName = doctorName, doctorId = doctorId))
    override val state: Value<ChatState> = _state

    private val pollingService = ChatPollingService(
        doctorId,
        getMessages = { getMessages(doctorId) },
        dispatchers = dispatchers,
    )

    init {
        lifecycle.doOnStart { pollingService.start(scope) }
        lifecycle.doOnStop { pollingService.stop() }

        scope.launch {
            pollingService.messages.collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    override fun onInputChange(text: String) { _state.update { it.copy(inputText = text) } }

    override fun onSend() {
        val text = _state.value.inputText.trim()
        val attachments = _state.value.pendingAttachments
        // Permite enviar solo-adjuntos (sin texto), igual que el backend.
        if ((text.isBlank() && attachments.isEmpty()) || _state.value.isSending) return
        _state.update { it.copy(inputText = "", isSending = true, error = null) }
        scope.launch {
            sendMessage(doctorId, text, attachments)
                .onSuccess { message ->
                    _state.update {
                        it.copy(isSending = false, pendingAttachments = emptyList(), messages = it.messages + message)
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSending = false, error = err.toUserMessage("No se pudo enviar el mensaje")) }
                }
        }
    }

    override fun onAttachmentPicked(file: PickedFile) {
        if (_state.value.pendingAttachments.size >= MAX_ATTACHMENTS) {
            _state.update { it.copy(error = "Máximo $MAX_ATTACHMENTS adjuntos por mensaje") }
            return
        }
        _state.update { it.copy(isUploading = true, error = null) }
        scope.launch {
            uploadAttachment(file.bytes, file.fileName, file.mimeType)
                .onSuccess { url ->
                    _state.update { it.copy(isUploading = false, pendingAttachments = it.pendingAttachments + url) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isUploading = false, error = err.toUserMessage("No se pudo subir el adjunto")) }
                }
        }
    }

    override fun onRemovePendingAttachment(index: Int) {
        _state.update {
            if (index !in it.pendingAttachments.indices) it
            else it.copy(pendingAttachments = it.pendingAttachments.filterIndexed { i, _ -> i != index })
        }
    }

    override fun onBack() { onOutput(ChatComponent.Output.Back) }

    override fun onReportUser(userId: String, userName: String) {
        onOutput(ChatComponent.Output.NavigateToReport(userId, userName))
    }

    override fun onBlockUser(userId: String, userName: String) {
        onOutput(ChatComponent.Output.NavigateToBlock(userId, userName))
    }

    private companion object {
        const val MAX_ATTACHMENTS = 5
    }
}
