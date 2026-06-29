package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminBlockedEmailItem

interface AdminBlockedEmailsComponent {
    val state: Value<AdminBlockedEmailsState>

    fun onRefresh()
    fun onShowBlockDialog()
    fun onDismissBlockDialog()
    fun onBlock(email: String, reason: String, durationDays: Int?)
    fun onUnblock(item: AdminBlockedEmailItem)
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminBlockedEmailsState(
    val items: List<AdminBlockedEmailItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Non-null = block-email dialog is open. */
    val showBlockDialog: Boolean = false,
    val isActing: Boolean = false,
    val actionError: String? = null,
) {
    /** Total count of blocked emails (real: list size). */
    val totalBlocked: Int get() = items.size

    /** Distinct domain count (derived client-side). */
    val distinctDomains: Int get() = items.distinctBy { it.domain }.size
}
