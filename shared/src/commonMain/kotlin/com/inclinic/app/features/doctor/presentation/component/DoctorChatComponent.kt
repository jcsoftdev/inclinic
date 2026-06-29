package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.core.upload.UploadFileUseCase
import com.inclinic.app.features.doctor.chat.application.GetDoctorChatMessagesUseCase
import com.inclinic.app.features.doctor.chat.application.SendDoctorChatMessageUseCase
import com.inclinic.app.features.patient.chat.infrastructure.ChatPollingService
import com.inclinic.app.features.patient.presentation.component.ChatState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Doctor chat component — reuses the shared ChatPollingService from the patient module.
 * Messages from the same appointment are shown for both roles; the UI uses senderRole
 * to differentiate bubbles (doctor messages appear on the right from the patient's view).
 */
class DoctorChatComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getMessages: GetDoctorChatMessagesUseCase,
    private val sendMessage: SendDoctorChatMessageUseCase,
    private val uploadAttachment: UploadFileUseCase,
    private val dispatchers: AppDispatchers,
) : ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ChatState())
    val state: Value<ChatState> = _state

    private val pollingService = ChatPollingService(
        doctorId = appointmentId,
        getMessages = { getMessages(appointmentId) },
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

    fun onInputChange(text: String) { _state.update { it.copy(inputText = text) } }

    fun onSend() {
        val text = _state.value.inputText.trim()
        val attachments = _state.value.pendingAttachments
        if ((text.isBlank() && attachments.isEmpty()) || _state.value.isSending) return
        _state.update { it.copy(inputText = "", isSending = true, error = null) }
        scope.launch {
            sendMessage(appointmentId, text, attachments)
                .onSuccess { message ->
                    _state.update {
                        it.copy(
                            isSending = false,
                            pendingAttachments = emptyList(),
                            messages = it.messages + message,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSending = false, error = err.toUserMessage("Failed to send message")) }
                }
        }
    }

    /** Uploads the picked file to the medical-attachments bucket and enqueues the returned URL. */
    fun onAttachmentPicked(file: PickedFile) {
        if (_state.value.isUploading) return
        _state.update { it.copy(isUploading = true, error = null) }
        scope.launch {
            uploadAttachment(
                bucket = ATTACHMENTS_BUCKET,
                bytes = file.bytes,
                fileName = file.fileName,
                mimeType = file.mimeType,
            ).onSuccess { url ->
                _state.update { it.copy(isUploading = false, pendingAttachments = it.pendingAttachments + url) }
            }.onFailure { err ->
                _state.update { it.copy(isUploading = false, error = err.toUserMessage("Error al subir adjunto")) }
            }
        }
    }

    fun onRemovePendingAttachment(index: Int) {
        _state.update {
            if (index !in it.pendingAttachments.indices) it
            else it.copy(pendingAttachments = it.pendingAttachments.filterIndexed { i, _ -> i != index })
        }
    }

    fun onBack() {}

    private companion object {
        const val ATTACHMENTS_BUCKET = "medical-attachments"
    }
}
