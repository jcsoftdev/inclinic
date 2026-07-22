package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.core.upload.UploadFileUseCase
import com.inclinic.app.features.auth.application.GetSpecialtiesUseCase
import com.inclinic.app.features.doctor.profile.application.RequestSpecialtyUseCase
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRequestSpecialtyComponent(
    componentContext: ComponentContext,
    private val requestSpecialty: RequestSpecialtyUseCase,
    private val getSpecialties: GetSpecialtiesUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RequestSpecialtyComponent.Output) -> Unit,
) : RequestSpecialtyComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RequestSpecialtyState())
    override val state: Value<RequestSpecialtyState> = _state

    init { loadCatalog() }

    private fun loadCatalog() {
        _state.update { it.copy(isLoadingCatalog = true, catalogError = null) }
        scope.launch {
            getSpecialties()
                .onSuccess { list -> _state.update { it.copy(isLoadingCatalog = false, catalog = list) } }
                .onFailure { err -> _state.update { it.copy(isLoadingCatalog = false, catalogError = err.toUserMessage("No se pudo cargar el catálogo")) } }
        }
    }

    override fun onRetryCatalog() = loadCatalog()

    override fun onSpecialtySelected(specialtyId: String) {
        _state.update { it.copy(selectedSpecialtyId = specialtyId, error = null) }
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
        val specialtyId = s.selectedSpecialtyId
        if (specialtyId.isNullOrBlank()) {
            _state.update { it.copy(error = "Selecciona una especialidad") }
            return
        }
        if (s.documentUrls.isEmpty()) {
            _state.update { it.copy(error = "Sube al menos un documento de respaldo") }
            return
        }
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            requestSpecialty(
                SpecialtyRequest(
                    specialtyId = specialtyId,
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

    override fun onPickCertification(file: PickedFile) {
        if (_state.value.isCertUploading) return
        _state.update { it.copy(isCertUploading = true, certUploadError = null) }
        scope.launch {
            uploadFileUseCase(
                bucket = SPECIALTY_DOCS_BUCKET,
                bytes = file.bytes,
                fileName = file.fileName,
                mimeType = file.mimeType,
            ).onSuccess { url ->
                _state.update { it.copy(isCertUploading = false, documentUrls = it.documentUrls + url) }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        isCertUploading = false,
                        certUploadError = err.toUserMessage("Error al subir certificación"),
                    )
                }
            }
        }
    }

    override fun onPickDiploma(file: PickedFile) {
        if (_state.value.isDiplomaUploading) return
        _state.update { it.copy(isDiplomaUploading = true, diplomaUploadError = null) }
        scope.launch {
            uploadFileUseCase(
                bucket = SPECIALTY_DOCS_BUCKET,
                bytes = file.bytes,
                fileName = file.fileName,
                mimeType = file.mimeType,
            ).onSuccess { url ->
                _state.update { it.copy(isDiplomaUploading = false, documentUrls = it.documentUrls + url) }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        isDiplomaUploading = false,
                        diplomaUploadError = err.toUserMessage("Error al subir diploma"),
                    )
                }
            }
        }
    }

    override fun onBack() = onOutput(RequestSpecialtyComponent.Output.Back)

    private companion object {
        const val SPECIALTY_DOCS_BUCKET = "specialty-request-docs"
    }
}
