package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value

interface RequestSpecialtyComponent {
    val state: Value<RequestSpecialtyState>

    fun onSpecialtyNameChange(name: String)
    fun onAddDocumentUrl(url: String)
    fun onRemoveDocumentUrl(url: String)
    fun onCommentChange(comment: String)
    fun onSubmit()
    fun onBack()

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
)
