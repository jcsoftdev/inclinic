package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.ui.molecules.DocUploadState

data class StepDocumentosState(
    val cmpState: DocUploadState = DocUploadState.Empty,
    val idFrontState: DocUploadState = DocUploadState.Empty,
    val idBackState: DocUploadState = DocUploadState.Empty,
    val uploadedDocs: List<UploadedDoc> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val allUploaded: Boolean
        get() = cmpState is DocUploadState.Done &&
                idFrontState is DocUploadState.Done &&
                idBackState is DocUploadState.Done
}

interface StepDocumentosComponent {
    val state: Value<StepDocumentosState>

    fun onPickDocument(kind: DocKind, file: ByteArray, fileName: String)
    fun onContinueClicked()
    fun onErrorDismissed()
}
