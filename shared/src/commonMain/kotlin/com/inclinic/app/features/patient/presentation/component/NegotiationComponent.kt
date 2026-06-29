package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.PackageNegotiation

interface NegotiationComponent {
    val state: Value<NegotiationState>

    fun onProposedPriceChange(value: String)
    fun onProposedSessionsChange(value: String)
    fun onMessageChange(text: String)
    fun onSubmitProposal()
    fun onAccept()
    fun onReject()
    fun onPay()
    fun onBack()
    fun onErrorDismissed()
    fun onReportUser(userId: String, userName: String)
    fun onBlockUser(userId: String, userName: String)

    sealed interface Output {
        data class NavigateToPayment(val therapyPackageId: String) : Output
        data object Back : Output
        data class NavigateToReport(val userId: String, val userName: String) : Output
        data class NavigateToBlock(val userId: String, val userName: String) : Output
    }
}

data class NegotiationState(
    val negotiation: PackageNegotiation? = null,
    val proposedPrice: String = "",
    val proposedSessions: String = "",
    val messageText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
)
