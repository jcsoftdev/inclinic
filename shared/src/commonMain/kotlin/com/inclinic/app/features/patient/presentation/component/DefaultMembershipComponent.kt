package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.features.patient.subscription.application.GetSubscriptionUseCase
import com.inclinic.app.features.patient.subscription.application.PurchasePremiumUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultMembershipComponent(
    componentContext: ComponentContext,
    private val getSubscription: GetSubscriptionUseCase,
    private val purchasePremium: PurchasePremiumUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (MembershipComponent.Output) -> Unit,
) : MembershipComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(MembershipState(isLoading = true))
    override val state: Value<MembershipState> = _state

    init { loadSubscription() }

    private fun loadSubscription() {
        scope.launch {
            getSubscription()
                .onSuccess { sub ->
                    _state.update { it.copy(tier = sub.tier, expiresAt = sub.expiresAt, isLoading = false) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("No se pudo cargar tu membresía")) }
                }
        }
    }

    // ── Checkout visibility ───────────────────────────────────────────────────

    override fun onUpgradeTapped() { _state.update { it.copy(showCheckout = true, error = null) } }
    override fun onDismissCheckout() { _state.update { it.copy(showCheckout = false) } }
    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }
    override fun onBack() { onOutput(MembershipComponent.Output.NavigateBack) }

    // ── Card form field updates ───────────────────────────────────────────────

    override fun onCardNumberChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(16)
        val formatted = digits.chunked(4).joinToString(" ")
        val cardType = when {
            digits.startsWith("4") -> CardType.VISA
            digits.startsWith("5") -> CardType.MASTERCARD
            else -> CardType.UNKNOWN
        }
        _state.update { it.copy(cardNumber = formatted, cardType = cardType) }
    }

    override fun onExpiryChange(value: String) { _state.update { it.copy(expiry = value) } }
    override fun onCvvChange(value: String) { _state.update { it.copy(cvv = value) } }
    override fun onCardholderNameChange(value: String) { _state.update { it.copy(cardholderName = value) } }
    override fun onDocTypeChange(value: String) { _state.update { it.copy(docType = value) } }
    override fun onDocNumberChange(value: String) { _state.update { it.copy(docNumber = value) } }

    // ── Purchase submission ───────────────────────────────────────────────────

    override fun onSubmitPurchase() {
        val s = _state.value
        if (s.isPurchasing) return

        val pan = s.cardNumber.filter { it.isDigit() }
        val expiryParts = s.expiry.split("/")
        val expMonth = expiryParts.getOrNull(0)?.trim()?.toIntOrNull() ?: run {
            _state.update { it.copy(error = "Vencimiento inválido") }
            return
        }
        val expYear = expiryParts.getOrNull(1)?.trim()?.toIntOrNull() ?: run {
            _state.update { it.copy(error = "Vencimiento inválido") }
            return
        }
        if (pan.length < 16) { _state.update { it.copy(error = "Número de tarjeta inválido") }; return }
        if (s.cvv.isBlank()) { _state.update { it.copy(error = "El CVV es requerido") }; return }
        if (s.cardholderName.isBlank()) { _state.update { it.copy(error = "El titular es requerido") }; return }

        _state.update { it.copy(isPurchasing = true, error = null) }
        val rawCard = RawCard(
            pan = pan,
            cvv = s.cvv,
            expMonth = expMonth,
            expYear = expYear,
            holderName = s.cardholderName,
            docType = s.docType,
            docNumber = s.docNumber,
        )
        scope.launch {
            purchasePremium(rawCard)
                .onSuccess { sub ->
                    _state.update {
                        it.copy(
                            tier = sub.tier,
                            expiresAt = sub.expiresAt,
                            isPurchasing = false,
                            showCheckout = false,
                            error = null,
                            // clear card fields after success
                            cardNumber = "",
                            expiry = "",
                            cvv = "",
                            cardholderName = "",
                            docNumber = "",
                            cardType = CardType.UNKNOWN,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isPurchasing = false, error = err.toUserMessage("No se pudo procesar el pago")) }
                }
        }
    }
}
