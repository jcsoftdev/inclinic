package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.ShareRequest

interface ApproveShareRequestComponent {
    val state: Value<ApproveShareRequestState>
    fun onDurationSelected(days: Int)
    fun onApprove()
    fun onReject()
    fun onClose()
    sealed interface Output {
        data object Closed : Output
        data object Approved : Output
        data object Rejected : Output
    }
}

data class ApproveShareRequestState(
    val request: ShareRequest? = null,
    val selectedDuration: Int = 30,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
