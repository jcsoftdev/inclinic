package com.inclinic.app.features.doctor.therapy_offers.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.therapy_offers.application.CreateTherapyOfferUseCase
import com.inclinic.app.features.doctor.therapy_offers.core.model.NewOfferDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultCreateTherapyOfferComponent(
    componentContext: ComponentContext,
    specialties: List<SpecialtyOption>,
    private val createOffer: CreateTherapyOfferUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (CreateTherapyOfferComponent.Output) -> Unit,
) : CreateTherapyOfferComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    private val _state = MutableValue(CreateTherapyOfferState(specialties = specialties))
    override val state: Value<CreateTherapyOfferState> = _state

    init { lifecycle.doOnDestroy { scope.cancel() } }

    override fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v, titleError = null) }
    override fun onSpecialtySelected(id: String) { _state.value = _state.value.copy(selectedSpecialtyId = id, specialtyError = null) }
    override fun onTotalSessionsChange(v: String) { _state.value = _state.value.copy(totalSessions = v, sessionsError = null) }
    override fun onPricePerSessionChange(v: String) { _state.value = _state.value.copy(pricePerSession = v, priceError = null) }
    override fun onMinPriceChange(v: String) { _state.value = _state.value.copy(minPricePerSession = v) }
    override fun onDescriptionChange(v: String) { _state.value = _state.value.copy(description = v) }
    override fun onActiveToggle(v: Boolean) { _state.value = _state.value.copy(isActive = v) }
    override fun onBack() = onOutput(CreateTherapyOfferComponent.Output.Back)

    override fun onSubmit() {
        val s = _state.value
        val titleErr = if (s.title.isBlank()) "El título es obligatorio" else null
        val specErr = if (s.selectedSpecialtyId.isBlank()) "Selecciona una especialidad" else null
        val sessions = s.totalSessions.toIntOrNull()
        val sessErr = if (sessions == null || sessions < 2) "Mínimo 2 sesiones" else null
        val price = s.pricePerSession.toDoubleOrNull()
        val priceErr = if (price == null || price < 10.0) "Precio mínimo S/. 10" else null

        if (titleErr != null || specErr != null || sessErr != null || priceErr != null) {
            _state.value = s.copy(titleError = titleErr, specialtyError = specErr, sessionsError = sessErr, priceError = priceErr)
            return
        }

        _state.value = s.copy(isSubmitting = true, error = null)
        scope.launch {
            val draft = NewOfferDraft(
                title = s.title.trim(),
                specialtyId = s.selectedSpecialtyId,
                totalSessions = sessions!!,
                pricePerSession = price!!,
                minPricePerSession = s.minPricePerSession.toDoubleOrNull(),
                sessionDurationMin = null,
                description = s.description.trim().ifBlank { null },
                isActive = s.isActive,
            )
            createOffer(draft).fold(
                onSuccess = { onOutput(CreateTherapyOfferComponent.Output.OfferCreated) },
                onFailure = { err ->
                    _state.value = _state.value.copy(isSubmitting = false, error = err.toUserMessage("Error al crear oferta"))
                },
            )
        }
    }
}
