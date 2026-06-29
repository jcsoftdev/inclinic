package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.therapy.application.CreateNegotiationUseCase
import com.inclinic.app.features.patient.therapy.application.GetNegotiationUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyOfferDetailUseCase
import com.inclinic.app.features.patient.therapy.application.RespondNegotiationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultNegotiationComponent(
    componentContext: ComponentContext,
    private val negotiationId: String? = null,
    private val offerId: String? = null,
    private val getNegotiation: GetNegotiationUseCase,
    private val getOfferDetail: GetTherapyOfferDetailUseCase,
    private val createNegotiation: CreateNegotiationUseCase,
    private val respondNegotiation: RespondNegotiationUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (NegotiationComponent.Output) -> Unit,
) : NegotiationComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(NegotiationState())
    override val state: Value<NegotiationState> = _state

    init { load() }

    override fun onProposedPriceChange(value: String) {
        _state.update { it.copy(proposedPrice = value) }
    }

    override fun onProposedSessionsChange(value: String) {
        _state.update { it.copy(proposedSessions = value) }
    }

    override fun onMessageChange(text: String) {
        _state.update { it.copy(messageText = text) }
    }

    override fun onSubmitProposal() {
        if (_state.value.isSending) return

        val price = _state.value.proposedPrice.trim().toDoubleOrNull()
        val sessions = _state.value.proposedSessions.trim().toIntOrNull()
        if (price == null || price <= 0.0) {
            _state.update { it.copy(error = "Ingresa un precio válido") }
            return
        }
        if (sessions == null || sessions < 2) {
            _state.update { it.copy(error = "El paquete debe tener al menos 2 sesiones") }
            return
        }

        val message = _state.value.messageText.trim().ifBlank { null }
        val current = _state.value.negotiation

        _state.update { it.copy(isSending = true, error = null) }
        scope.launch {
            if (current == null) {
                // Start mode — create a brand new negotiation from the offer.
                val oid = offerId
                if (oid == null) {
                    _state.update { it.copy(isSending = false, error = "No hay una oferta para negociar") }
                    return@launch
                }
                createNegotiation(oid, price, sessions, message)
                    .onSuccess { negotiation ->
                        _state.update { it.copy(isSending = false, messageText = "", negotiation = negotiation) }
                    }
                    .onFailure { err ->
                        _state.update { it.copy(isSending = false, error = err.toUserMessage("Error al crear la negociación")) }
                    }
            } else {
                // View mode — patient sends a counter-offer.
                respondNegotiation(current.id, "COUNTER", price, sessions, message)
                    .onSuccess {
                        _state.update { it.copy(messageText = "") }
                        refresh(current.id)
                    }
                    .onFailure { err ->
                        _state.update { it.copy(isSending = false, error = err.toUserMessage("Error al enviar la contraoferta")) }
                    }
            }
        }
    }

    override fun onAccept() {
        val current = _state.value.negotiation ?: return
        if (_state.value.isSending) return

        _state.update { it.copy(isSending = true, error = null) }
        scope.launch {
            respondNegotiation(current.id, "ACCEPT", null, null, null)
                .onSuccess { refresh(current.id) }
                .onFailure { err ->
                    _state.update { it.copy(isSending = false, error = err.toUserMessage("Error al aceptar la propuesta")) }
                }
        }
    }

    override fun onReject() {
        val current = _state.value.negotiation ?: return
        if (_state.value.isSending) return

        _state.update { it.copy(isSending = true, error = null) }
        scope.launch {
            respondNegotiation(current.id, "REJECT", null, null, null)
                .onSuccess { refresh(current.id) }
                .onFailure { err ->
                    _state.update { it.copy(isSending = false, error = err.toUserMessage("Error al rechazar la propuesta")) }
                }
        }
    }

    override fun onPay() {
        val pkgId = _state.value.negotiation?.acceptedTherapyPackageId
        if (pkgId != null) {
            onOutput(NegotiationComponent.Output.NavigateToPayment(pkgId))
        } else {
            _state.update { it.copy(error = "La negociación aún no tiene un paquete por pagar") }
        }
    }

    override fun onBack() {
        onOutput(NegotiationComponent.Output.Back)
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }

    override fun onReportUser(userId: String, userName: String) {
        onOutput(NegotiationComponent.Output.NavigateToReport(userId, userName))
    }

    override fun onBlockUser(userId: String, userName: String) {
        onOutput(NegotiationComponent.Output.NavigateToBlock(userId, userName))
    }

    private fun load() {
        when {
            negotiationId != null -> {
                _state.update { it.copy(isLoading = true, error = null) }
                scope.launch {
                    getNegotiation(negotiationId)
                        .onSuccess { negotiation ->
                            _state.update { it.copy(isLoading = false, negotiation = negotiation) }
                        }
                        .onFailure { err ->
                            _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar negociación")) }
                        }
                }
            }
            offerId != null -> {
                // Start mode — prefill the proposal form from the offer's defaults.
                _state.update { it.copy(isLoading = true, error = null) }
                scope.launch {
                    getOfferDetail(offerId)
                        .onSuccess { offer ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    proposedPrice = offer.pricePerSession.toString(),
                                    proposedSessions = offer.sessions.toString(),
                                )
                            }
                        }
                        .onFailure { err ->
                            _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar la oferta")) }
                        }
                }
            }
        }
    }

    private suspend fun refresh(id: String) {
        getNegotiation(id)
            .onSuccess { negotiation ->
                _state.update { it.copy(isSending = false, negotiation = negotiation) }
            }
            .onFailure { err ->
                _state.update { it.copy(isSending = false, error = err.toUserMessage("Error al actualizar la negociación")) }
            }
    }
}
