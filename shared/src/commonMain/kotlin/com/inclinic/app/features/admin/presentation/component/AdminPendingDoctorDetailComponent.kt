package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.util.DetailLoadState
import com.inclinic.app.core.util.detailLoadState
import com.inclinic.app.features.admin.infrastructure.remote.AdminPendingDoctor

interface AdminPendingDoctorDetailComponent {
    val state: Value<AdminPendingDoctorDetailState>

    fun onBack()
    fun onApprove()
    fun onReasonChange(reason: String)
    fun onConfirmReject()

    sealed interface Output {
        data object Back : Output
        data object ApproveSuccess : Output
    }
}

data class AdminPendingDoctorDetailState(
    val doctor: AdminPendingDoctor? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Reject flow
    val rejectReason: String = "",
    val rejectError: String? = null,
    val isSubmitting: Boolean = false,
    /** True when [error] came from an HTTP 404 — see [com.inclinic.app.core.error.isNotFoundError]. */
    val notFound: Boolean = false,
)

fun AdminPendingDoctorDetailState.toDetailLoadState(): DetailLoadState<AdminPendingDoctor> =
    detailLoadState(isLoading = isLoading, value = doctor, error = error, notFound = notFound)
