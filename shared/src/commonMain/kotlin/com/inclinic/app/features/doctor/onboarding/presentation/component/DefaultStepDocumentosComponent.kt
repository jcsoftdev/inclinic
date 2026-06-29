package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.application.UploadDocumentUseCase
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.ui.molecules.DocUploadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultStepDocumentosComponent(
    componentContext: ComponentContext,
    private val uploadDocumentUseCase: UploadDocumentUseCase,
    private val dispatchers: AppDispatchers,
    private val onContinue: (List<UploadedDoc>) -> Unit,
) : StepDocumentosComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(StepDocumentosState())
    override val state: Value<StepDocumentosState> = _state

    override fun onPickDocument(kind: DocKind, file: ByteArray, fileName: String) {
        // Set uploading state immediately
        _state.update {
            it.copy(
                cmpState = if (kind == DocKind.CMP_LICENSE) DocUploadState.Uploading(0f) else it.cmpState,
                idFrontState = if (kind == DocKind.ID_FRONT) DocUploadState.Uploading(0f) else it.idFrontState,
                idBackState = if (kind == DocKind.ID_BACK) DocUploadState.Uploading(0f) else it.idBackState,
                error = null,
            )
        }

        scope.launch {
            uploadDocumentUseCase(file, fileName, kind)
                .onSuccess { uploaded ->
                    val newDocs = _state.value.uploadedDocs
                        .filter { it.kind != kind } + uploaded
                    _state.update {
                        it.copy(
                            cmpState = if (kind == DocKind.CMP_LICENSE) DocUploadState.Done(fileName) else it.cmpState,
                            idFrontState = if (kind == DocKind.ID_FRONT) DocUploadState.Done(fileName) else it.idFrontState,
                            idBackState = if (kind == DocKind.ID_BACK) DocUploadState.Done(fileName) else it.idBackState,
                            uploadedDocs = newDocs,
                        )
                    }
                }
                .onFailure { err ->
                    val msg = err.toUserMessage("Error al subir documento")
                    _state.update {
                        it.copy(
                            cmpState = if (kind == DocKind.CMP_LICENSE) DocUploadState.Error(msg) else it.cmpState,
                            idFrontState = if (kind == DocKind.ID_FRONT) DocUploadState.Error(msg) else it.idFrontState,
                            idBackState = if (kind == DocKind.ID_BACK) DocUploadState.Error(msg) else it.idBackState,
                            error = msg,
                        )
                    }
                }
        }
    }

    override fun onContinueClicked() {
        if (!_state.value.allUploaded) {
            _state.update { it.copy(error = "Sube los 3 documentos para continuar") }
            return
        }
        onContinue(_state.value.uploadedDocs)
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
