package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
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
)
