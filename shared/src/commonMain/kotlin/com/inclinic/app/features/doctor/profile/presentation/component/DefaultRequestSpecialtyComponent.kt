package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.application.RequestSpecialtyUseCase
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRequestSpecialtyComponent(
    componentContext: ComponentContext,
    private val requestSpecialty: RequestSpecialtyUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RequestSpecialtyComponent.Output) -> Unit,
) : RequestSpecialtyComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RequestSpecialtyState())
    override val state: Value<RequestSpecialtyState> = _state

    override fun onSpecialtyNameChange(name: String) {
        _state.update { it.copy(specialtyName = name, error = null) }
    }

    override fun onAddDocumentUrl(url: String) {
        _state.update { it.copy(documentUrls = it.documentUrls + url) }
    }

    override fun onRemoveDocumentUrl(url: String) {
        _state.update { it.copy(documentUrls = it.documentUrls - url) }
    }

    override fun onCommentChange(comment: String) {
        _state.update { it.copy(comment = comment) }
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.isSubmitting) return
        if (s.specialtyName.isBlank()) {
            _state.update { it.copy(error = "Specialty name is required") }
            return
        }
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            requestSpecialty(
                SpecialtyRequest(
                    specialtyName = s.specialtyName.trim(),
                    documentUrls = s.documentUrls,
                    comment = s.comment.trim(),
                )
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, submitSuccess = true) }
                onOutput(RequestSpecialtyComponent.Output.Submitted)
            }.onFailure { err ->
                _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Submit failed")) }
            }
        }
    }

    override fun onBack() = onOutput(RequestSpecialtyComponent.Output.Back)
}
