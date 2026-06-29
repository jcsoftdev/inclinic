package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment

interface ConfirmRatingComponent {
    val state: Value<ConfirmRatingState>

    fun onPunctualityChanged(stars: Int)
    fun onProfessionalismChanged(stars: Int)
    fun onEmpathyChanged(stars: Int)
    fun onCommentChanged(comment: String)
    fun onConfirm()
    fun onDispute()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Confirmed : Output
        data class NavigateToDispute(val appointmentId: String) : Output
    }
}

data class ConfirmRatingState(
    val appointment: Appointment? = null,
    val punctuality: Int = 0,
    val professionalism: Int = 0,
    val empathy: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
