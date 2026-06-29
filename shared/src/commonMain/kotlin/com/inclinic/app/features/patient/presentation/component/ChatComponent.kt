package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.platform.PickedFile

interface ChatComponent {
    val state: Value<ChatState>

    fun onInputChange(text: String)
    fun onSend()
    fun onBack()

    /** El usuario seleccionó un archivo en el picker: lo sube y lo encola como adjunto pendiente. */
    fun onAttachmentPicked(file: PickedFile)

    /** Elimina un adjunto pendiente (aún no enviado) por su índice. */
    fun onRemovePendingAttachment(index: Int)

    fun onReportUser(userId: String, userName: String)
    fun onBlockUser(userId: String, userName: String)

    sealed interface Output {
        data object Back : Output
        data class NavigateToReport(val userId: String, val userName: String) : Output
        data class NavigateToBlock(val userId: String, val userName: String) : Output
    }
}

data class ChatState(
    /** Nombre del doctor para el encabezado de la conversación. */
    val doctorName: String = "",
    /** ID del doctor — usado por onReportUser / onBlockUser en el dropdown del header. */
    val doctorId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val pollingFailed: Boolean = false,
    /** URLs de adjuntos ya subidos, listos para enviarse con el próximo mensaje. */
    val pendingAttachments: List<String> = emptyList(),
    val isUploading: Boolean = false,
)
