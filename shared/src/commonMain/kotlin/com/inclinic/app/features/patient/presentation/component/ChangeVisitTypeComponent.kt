package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.VisitType

interface ChangeVisitTypeComponent {
    val state: Value<ChangeVisitTypeState>

    fun onNewVisitTypeSelected(type: VisitType)
    fun onAddressChanged(address: String)
    fun onReasonChanged(reason: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Requested : Output
    }
}

data class ChangeVisitTypeState(
    val appointment: Appointment? = null,
    val newVisitType: VisitType? = null,
    val address: String = "",
    val reason: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
