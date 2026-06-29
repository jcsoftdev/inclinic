package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminReviewItem

/**
 * Filter chips for the reviews list.
 *
 * Todas          → no extra query params (hidden="all" is backend default)
 * Con comentario → withComment=true
 * Ocultas        → hidden=true
 */
enum class AdminReviewsFilter(
    val label: String,
    val withComment: Boolean?,
    val hidden: Boolean?,
) {
    All("Todas", withComment = null, hidden = null),
    WithComment("Con comentario", withComment = true, hidden = null),
    Hidden("Ocultas", withComment = null, hidden = true),
}

interface AdminReviewsComponent {
    val state: Value<AdminReviewsState>

    fun onRefresh()
    fun onFilterChange(filter: AdminReviewsFilter)
    fun onHide(item: AdminReviewItem, reason: String)
    fun onUnhide(item: AdminReviewItem)
    fun onShowHideDialog(item: AdminReviewItem)
    fun onDismissHideDialog()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminReviewsState(
    val items: List<AdminReviewItem> = emptyList(),
    val activeFilter: AdminReviewsFilter = AdminReviewsFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Non-null means the hide-reason dialog is open for this item. */
    val pendingHideItem: AdminReviewItem? = null,
    val isActing: Boolean = false,
    val actionError: String? = null,
)
