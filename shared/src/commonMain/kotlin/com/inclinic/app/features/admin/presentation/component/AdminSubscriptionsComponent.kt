package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminSubscriptionItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminSubscriptionStats
import com.inclinic.app.features.admin.infrastructure.remote.SubscriptionStatus

/**
 * Filter chips for the subscriptions list.
 *
 * All     → show all items (no status filter)
 * Activas  → status == ACTIVE
 * Vencen   → status == EXPIRING (within 30 days)
 * Pausadas → status == EXPIRED (tier=FREE, was PREMIUM)
 */
enum class AdminSubscriptionsFilter(val label: String, val status: SubscriptionStatus?) {
    All("Todos", null),
    Active("Activas", SubscriptionStatus.ACTIVE),
    Expiring("Vencen", SubscriptionStatus.EXPIRING),
    Expired("Pausadas", SubscriptionStatus.EXPIRED),
}

interface AdminSubscriptionsComponent {
    val state: Value<AdminSubscriptionsState>

    fun onRefresh()
    fun onFilterChange(filter: AdminSubscriptionsFilter)
    fun onManageClicked(item: AdminSubscriptionItem)
    fun onConfirmManage(item: AdminSubscriptionItem, newTier: String, expiresAt: String?)
    fun onDismissDialog()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminSubscriptionsState(
    val stats: AdminSubscriptionStats = AdminSubscriptionStats(),
    val allItems: List<AdminSubscriptionItem> = emptyList(),
    val activeFilter: AdminSubscriptionsFilter = AdminSubscriptionsFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Item currently pending a tier-change confirmation dialog. Null = no dialog. */
    val pendingManageItem: AdminSubscriptionItem? = null,
    val isActioning: Boolean = false,
    val actionError: String? = null,
) {
    val visibleItems: List<AdminSubscriptionItem>
        get() = if (activeFilter.status == null) allItems
                else allItems.filter { it.status == activeFilter.status }
}
