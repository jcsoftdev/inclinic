package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminNoShowItem

interface AdminDisputasComponent {
    val state: Value<AdminDisputasState>

    fun onSegmentSelected(segment: DisputasSegment)
    fun onDisputeStatusFilter(filter: String?)
    fun onRefresh()
    fun onDisputeClicked(id: String)
    fun onNoShowClicked(id: String)

    sealed interface Output {
        data class NavigateToResolveDispute(val disputeId: String) : Output
        data class NavigateToResolveNoShow(val noShowId: String) : Output
    }
}

enum class DisputasSegment { Disputes, NoShows }

data class AdminDisputasState(
    val segment: DisputasSegment = DisputasSegment.Disputes,
    val disputeStatusFilter: String? = null,
    // Disputes
    val disputes: List<AdminDisputeItem> = emptyList(),
    val disputesLoading: Boolean = false,
    val disputesError: String? = null,
    // No-shows
    val noShows: List<AdminNoShowItem> = emptyList(),
    val noShowsLoading: Boolean = false,
    val noShowsError: String? = null,
) {
    val isLoading: Boolean get() = when (segment) {
        DisputasSegment.Disputes -> disputesLoading
        DisputasSegment.NoShows -> noShowsLoading
    }
}
