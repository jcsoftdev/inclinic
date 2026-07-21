package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.SubscriptionTier

interface MembershipComponent {
    val state: Value<MembershipState>

    fun onUpgradeTapped()
    fun onDismissCheckout()
    fun onErrorDismissed()
    fun onBack()

    // ── Card form field updates ───────────────────────────────────────────────
    fun onCardNumberChange(value: String)
    fun onExpiryChange(value: String)
    fun onCvvChange(value: String)
    fun onCardholderNameChange(value: String)
    fun onDocTypeChange(value: String)
    fun onDocNumberChange(value: String)

    fun onSubmitPurchase()

    sealed interface Output {
        data object NavigateBack : Output
    }
}

/** Monthly price of Patient Premium, in PEN. */
const val PREMIUM_PRICE: Double = 19.9

data class MembershipState(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val expiresAt: String? = null,
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val error: String? = null,
    val showCheckout: Boolean = false,
    // ── Card form fields ──────────────────────────────────────────────────────
    val cardNumber: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val cardholderName: String = "",
    val docType: String = "DNI",
    val docNumber: String = "",
    val cardType: CardType = CardType.UNKNOWN,
    val benefits: List<String> = listOf(
        "Historia clínica completa + descarga en PDF",
        "Citas prioritarias con especialistas",
        "Descuentos en paquetes de terapia",
        "Soporte preferente 24/7",
    ),
) {
    val isPremium: Boolean get() = tier == SubscriptionTier.PREMIUM
}
