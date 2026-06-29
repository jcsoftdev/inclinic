package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminSpecialtyRequestItem

interface AdminSpecialtyRequestsComponent {
    val state: Value<AdminSpecialtyRequestsState>

    fun onRefresh()
    fun onOpenEvaluate(requestId: String)
    fun onDismissEvaluate()
    fun onSelectAction(action: String)    // "approve" | "reject"
    fun onReasonChange(reason: String)
    fun onConfirmEvaluate()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminSpecialtyRequestsState(
    val items: List<AdminSpecialtyRequestItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // Evaluate sheet state
    val evaluatingRequestId: String? = null,
    val selectedAction: String? = null,   // "approve" | "reject"
    val reason: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
) {
    val showEvaluateSheet: Boolean get() = evaluatingRequestId != null
    val canConfirm: Boolean
        get() {
            if (selectedAction == null || isSubmitting) return false
            if (selectedAction == "reject" && reason.trim().length < 10) return false
            return true
        }
    val evaluatingItem: AdminSpecialtyRequestItem?
        get() = evaluatingRequestId?.let { id -> items.find { it.id == id } }
}
