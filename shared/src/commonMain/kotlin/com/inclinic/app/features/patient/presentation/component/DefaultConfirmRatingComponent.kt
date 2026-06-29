package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.appointments.application.ConfirmRatingUseCase
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultConfirmRatingComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetAppointmentDetailUseCase,
    private val confirmRating: ConfirmRatingUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ConfirmRatingComponent.Output) -> Unit,
) : ConfirmRatingComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ConfirmRatingState())
    override val state: Value<ConfirmRatingState> = _state

    init { load() }

    override fun onPunctualityChanged(stars: Int) {
        _state.update { it.copy(punctuality = stars.coerceIn(1, 5)) }
    }

    override fun onProfessionalismChanged(stars: Int) {
        _state.update { it.copy(professionalism = stars.coerceIn(1, 5)) }
    }

    override fun onEmpathyChanged(stars: Int) {
        _state.update { it.copy(empathy = stars.coerceIn(1, 5)) }
    }

    override fun onCommentChanged(comment: String) {
        _state.update { it.copy(comment = comment) }
    }

    override fun onConfirm() {
        val s = _state.value
        if (s.punctuality == 0 || s.professionalism == 0 || s.empathy == 0) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            confirmRating(appointmentId, s.punctuality, s.professionalism, s.empathy, s.comment.ifBlank { null })
                .onSuccess { onOutput(ConfirmRatingComponent.Output.Confirmed) }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage()) }
                }
        }
    }

    override fun onDispute() {
        onOutput(ConfirmRatingComponent.Output.NavigateToDispute(appointmentId))
    }

    override fun onBack() { onOutput(ConfirmRatingComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appt ->
                    _state.update { it.copy(isLoading = false, appointment = appt) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }
}
