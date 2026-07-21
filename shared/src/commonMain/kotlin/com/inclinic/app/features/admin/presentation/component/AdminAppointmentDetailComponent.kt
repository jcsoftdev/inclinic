package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.util.DetailLoadState
import com.inclinic.app.core.util.detailLoadState
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail

interface AdminAppointmentDetailComponent {
    val state: Value<AdminAppointmentDetailState>

    fun onBack()
    fun onNavigateToResolveDispute()

    sealed interface Output {
        data object Back : Output
        /** Navigates to dispute resolution for the current appointment.
         *  Only emitted when [AdminAppointmentDetail.hasDispute] is true — see
         *  [DefaultAdminAppointmentDetailComponent.onNavigateToResolveDispute]. */
        data class NavigateToResolveDispute(val appointmentId: String) : Output
    }
}

data class AdminAppointmentDetailState(
    val detail: AdminAppointmentDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** True when [error] came from an HTTP 404 — see [com.inclinic.app.core.error.isNotFoundError]. */
    val notFound: Boolean = false,
)

fun AdminAppointmentDetailState.toDetailLoadState(): DetailLoadState<AdminAppointmentDetail> =
    detailLoadState(isLoading = isLoading, value = detail, error = error, notFound = notFound)
