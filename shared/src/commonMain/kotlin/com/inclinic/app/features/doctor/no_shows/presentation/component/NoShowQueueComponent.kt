package com.inclinic.app.features.doctor.no_shows.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem

/** Which tab is visible on the no-shows queue screen. */
enum class NoShowTab { Pending, Resolved }

interface NoShowQueueComponent {
    val state: Value<NoShowQueueState>

    fun onTabSelected(tab: NoShowTab)
    fun onRetry()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class NoShowQueueState(
    val isLoading: Boolean = false,
    val selectedTab: NoShowTab = NoShowTab.Pending,
    /** Items with paymentHoldStatus == HELD. */
    val pending: List<NoShowItem> = emptyList(),
    /** Items with paymentHoldStatus == RELEASED or REFUNDED. */
    val resolved: List<NoShowItem> = emptyList(),
    val error: String? = null,
)
