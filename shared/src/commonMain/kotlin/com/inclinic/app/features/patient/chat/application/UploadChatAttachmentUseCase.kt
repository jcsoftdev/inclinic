package com.inclinic.app.features.patient.chat.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.ChatDataSource
import kotlinx.coroutines.withContext

/**
 * Sube un adjunto (imagen / PDF) y devuelve su URL para enviarlo luego en un mensaje de chat.
 */
class UploadChatAttachmentUseCase(
    private val dataSource: ChatDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<String> = withContext(dispatchers.io) {
        dataSource.uploadAttachment(bytes, fileName, mimeType).map { it.url }
    }
}
