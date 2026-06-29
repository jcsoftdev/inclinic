package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.medical_history.application.DownloadPrescriptionPdfUseCase
import com.inclinic.app.features.patient.medical_history.application.GetPrescriptionDetailUseCase
import com.inclinic.app.features.patient.medical_history.application.GetPrescriptionPdfUrlUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPrescriptionDetailComponent(
    componentContext: ComponentContext,
    private val prescriptionId: String,
    private val getPrescriptionDetail: GetPrescriptionDetailUseCase,
    private val getPrescriptionPdfUrl: GetPrescriptionPdfUrlUseCase,
    private val downloadPrescriptionPdf: DownloadPrescriptionPdfUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PrescriptionDetailComponent.Output) -> Unit,
) : PrescriptionDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PrescriptionDetailState())
    override val state: Value<PrescriptionDetailState> = _state

    init { load() }

    override fun onBack() { onOutput(PrescriptionDetailComponent.Output.Back) }
    override fun onShare() {}

    override fun pdfUrl(): String = getPrescriptionPdfUrl(prescriptionId)

    override fun onDownloadPdf() {
        if (_state.value.isDownloading) return
        _state.update { it.copy(isDownloading = true, downloadError = null) }
        scope.launch {
            downloadPrescriptionPdf(prescriptionId)
                .onSuccess { bytes ->
                    val code = _state.value.prescription?.code ?: prescriptionId
                    _state.update {
                        it.copy(isDownloading = false, pdfDownload = PdfDownload(bytes, "Receta-$code.pdf"))
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isDownloading = false, downloadError = err.toUserMessage("No se pudo descargar el PDF")) }
                }
        }
    }

    override fun onPdfConsumed() {
        _state.update { it.copy(pdfDownload = null) }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPrescriptionDetail(prescriptionId)
                .onSuccess { rx -> _state.update { it.copy(isLoading = false, prescription = rx) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error")) } }
        }
    }
}
