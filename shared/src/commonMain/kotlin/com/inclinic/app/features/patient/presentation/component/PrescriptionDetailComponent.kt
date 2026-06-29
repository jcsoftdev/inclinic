package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Prescription

interface PrescriptionDetailComponent {
    val state: Value<PrescriptionDetailState>
    fun onBack()
    fun onShare()

    /** Absolute URL of the backend-generated PDF, ready for [androidx.compose.ui.platform.UriHandler]. */
    fun pdfUrl(): String

    /** Descarga autenticada del PDF; el resultado llega vía [PrescriptionDetailState.pdfDownload]. */
    fun onDownloadPdf()

    /** Limpia el PDF descargado tras abrirlo (evento one-shot). */
    fun onPdfConsumed()

    sealed interface Output { data object Back : Output }
}

data class PrescriptionDetailState(
    val prescription: Prescription? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDownloading: Boolean = false,
    val pdfDownload: PdfDownload? = null,
    val downloadError: String? = null,
)

/** Bytes del PDF descargado, listos para abrir con el visor del sistema. */
class PdfDownload(val bytes: ByteArray, val fileName: String)
