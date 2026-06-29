package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail

interface AdminAppointmentDetailComponent {
    val state: Value<AdminAppointmentDetailState>

    fun onBack()
    fun onNavigateToResolveDispute()

    sealed interface Output {
        data object Back : Output
        /** Navigates to dispute resolution for the current appointment.
         *  Target screen does not exist yet — wired to a no-op / TODO placeholder. */
        data class NavigateToResolveDispute(val appointmentId: String) : Output
    }
}

data class AdminAppointmentDetailState(
    val detail: AdminAppointmentDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
