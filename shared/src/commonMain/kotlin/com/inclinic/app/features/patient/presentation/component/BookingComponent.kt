package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.VisitType
import kotlinx.serialization.Serializable

interface BookingComponent {
    val state: Value<BookingState>

    fun onVisitTypeChange(visitType: VisitType)
    fun onNotesChange(notes: String)
    fun onConfirm()
    fun onSkipPayment()
    fun onBack()

    sealed interface Output {
        data class NavigateToPayment(val appointmentId: String) : Output
        data object NavigateToAppointments : Output
        data object Back : Output
    }
}

/**
 * @Serializable so StateKeeper can survive Android configuration changes.
 * [doctor] is marked @Transient — it is re-fetched on restore.
 *
 * REQ-4-009
 */
@Serializable
data class BookingState(
    @kotlinx.serialization.Transient val doctor: Doctor? = null,
    val slotId: String = "",
    val date: String = "",
    val startTime: String = "",
    val visitType: VisitType? = null,
    val notes: String = "",
    @kotlinx.serialization.Transient val isLoading: Boolean = false,
    @kotlinx.serialization.Transient val isConfirmed: Boolean = false,
    @kotlinx.serialization.Transient val error: String? = null,
    @kotlinx.serialization.Transient val visitTypeError: String? = null,
    @kotlinx.serialization.Transient val isLoadingSkip: Boolean = false,
)
