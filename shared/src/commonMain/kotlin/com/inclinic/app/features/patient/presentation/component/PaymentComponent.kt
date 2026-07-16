package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.Doctor

interface PaymentComponent {
    val state: Value<PaymentState>

    fun onCardNumberChange(value: String)
    fun onExpiryChange(value: String)
    fun onCvvChange(value: String)
    fun onCardholderNameChange(value: String)
    fun onDocTypeChange(value: String)
    fun onDocNumberChange(value: String)
    // ── Método de pago (Tarjeta / Yape) ───────────────────────────────────────
    fun onSelectMethod(method: PaymentMethodChoice)
    fun onYapePhoneChange(value: String)
    fun onYapeOtpChange(value: String)
    fun onSubmit()
    fun onBack()
    fun onErrorDismissed()

    // ── Payment outcome actions ───────────────────────────────────────────────
    /** Pago Pendiente → "Pagar ahora": transitions back to the card form. */
    fun onPayNow()
    /** Pago Pendiente → "Cancelar reserva": cancels appointment and navigates away. */
    fun onCancelReservation()
    /** Pago En Proceso / any → "Volver a mis citas". */
    fun onGoToAppointments()
    /** Pago Rechazado → "Reintentar pago": returns to card form. */
    fun onRetryPayment()
    /** Pago Rechazado → "Cambiar tarjeta": same as retry (returns to card form). */
    fun onChangeCard()

    sealed interface Output {
        data class NavigateToSuccess(val appointmentId: String) : Output
        data object Back : Output
        data object NavigateToAppointments : Output
        data object NavigateToPackages : Output
    }
}

/** Visual status of the payment flow, driving which screen is shown. */
enum class PaymentStatus {
    /** Showing the card entry form (default / initial state). */
    FORM,
    /** Appointment is reserved but unpaid — 30-min countdown is running. */
    PENDING,
    /** Payment submitted and under MercadoPago review (in_process / pending). */
    PROCESSING,
    /** Payment was rejected by the gateway. */
    REJECTED,
}

enum class CardType { VISA, MASTERCARD, UNKNOWN }

/** Método de pago elegido en el formulario. */
enum class PaymentMethodChoice { CARD, YAPE }

data class PaymentState(
    val appointment: Appointment? = null,
    val doctor: Doctor? = null,
    val therapyPackage: com.inclinic.app.core.model.TherapyPackage? = null,
    /** True when this payment is for a therapy package (drives title + summary, even before/if it fails to load). */
    val isPackagePayment: Boolean = false,
    val isSummaryLoading: Boolean = false,
    // ── Card form fields ──────────────────────────────────────────────────────
    val cardNumber: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val cardholderName: String = "",
    val docType: String = "DNI",
    val docNumber: String = "",
    val cardType: CardType = CardType.UNKNOWN,
    // ── Método de pago + campos Yape ──────────────────────────────────────────
    val selectedMethod: PaymentMethodChoice = PaymentMethodChoice.CARD,
    val yapePhone: String = "",
    val yapeOtp: String = "",
    // ── Async state ───────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val isExpired: Boolean = false,
    val error: String? = null,
    // ── Payment outcome ───────────────────────────────────────────────────────
    val paymentStatus: PaymentStatus = PaymentStatus.FORM,
    /** Remaining seconds for Pago Pendiente countdown. Null means no active countdown. */
    val secondsRemaining: Int? = null,
)
