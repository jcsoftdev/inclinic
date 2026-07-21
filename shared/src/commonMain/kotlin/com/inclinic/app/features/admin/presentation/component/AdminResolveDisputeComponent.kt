package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.util.DetailLoadState
import com.inclinic.app.core.util.detailLoadState
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem

interface AdminResolveDisputeComponent {
    val state: Value<AdminResolveDisputeState>

    fun onBack()
    fun onSelectResolution(resolution: String)
    fun onNoteChange(note: String)
    fun onConfirm()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminResolveDisputeState(
    val dispute: AdminDisputeItem? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val selectedResolution: String? = null,   // "PROVIDER" or "PATIENT"
    val note: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    /** True when [loadError] means "this dispute id doesn't exist" rather than a transient failure. */
    val notFound: Boolean = false,
) {
    val canConfirm: Boolean
        get() = selectedResolution != null && note.trim().length >= 10 && !isSubmitting
}

fun AdminResolveDisputeState.toDetailLoadState(): DetailLoadState<AdminDisputeItem> =
    detailLoadState(isLoading = isLoading, value = dispute, error = loadError, notFound = notFound)
