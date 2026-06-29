package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.platform.PickedFile

interface RequestSpecialtyComponent {
    val state: Value<RequestSpecialtyState>

    fun onSpecialtyNameChange(name: String)
    fun onAddDocumentUrl(url: String)
    fun onRemoveDocumentUrl(url: String)
    fun onCommentChange(comment: String)
    fun onSubmit()
    fun onBack()

    /** User tapped the SUNEDU certification slot — stores the picked file in state. */
    fun onPickCertification(file: PickedFile)

    /** User tapped the Diploma slot — stores the picked file in state. */
    fun onPickDiploma(file: PickedFile)

    sealed interface Output {
        data object Back : Output
        data object Submitted : Output
    }
}

data class RequestSpecialtyState(
    val specialtyName: String = "",
    val documentUrls: List<String> = emptyList(),
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitSuccess: Boolean = false,
    // ── Upload progress for SUNEDU certification slot ─────────────────────────
    val isCertUploading: Boolean = false,
    val certUploadError: String? = null,
    // ── Upload progress for Diploma slot ─────────────────────────────────────
    val isDiplomaUploading: Boolean = false,
    val diplomaUploadError: String? = null,
)
