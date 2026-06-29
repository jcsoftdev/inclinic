package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.port.TelemetryService
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.appointments.application.CancelAppointmentUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.payment.application.PaymentDeadlineExpiredException
import com.inclinic.app.features.patient.payment.application.ProcessPaymentUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackageDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock

/** Default seconds for the Pago Pendiente countdown when no backend deadline is present. */
private const val DEFAULT_PENDING_SECONDS = 30 * 60 // 30 minutes

class DefaultPaymentComponent(
    componentContext: ComponentContext,
    private val appointmentId: String?,
    private val therapyPackageId: String? = null,
    private val processPayment: ProcessPaymentUseCase,
    private val getAppointmentDetail: GetAppointmentDetailUseCase,
    private val getDoctorDetail: GetDoctorDetailUseCase,
    private val cancelAppointment: CancelAppointmentUseCase,
    private val getPackageDetail: GetTherapyPackageDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val telemetry: TelemetryService? = null,
    private val onOutput: (PaymentComponent.Output) -> Unit,
) : PaymentComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    private var countdownJob: Job? = null

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PaymentState(isSummaryLoading = true, isPackagePayment = therapyPackageId != null))
    override val state: Value<PaymentState> = _state

    init {
        if (appointmentId != null) {
            loadAppointment(appointmentId)
        } else if (therapyPackageId != null) {
            loadPackage(therapyPackageId)
        } else {
            // Misconfigured route (neither id) — resolve the summary spinner so the
            // screen shows a stable error instead of spinning forever.
            _state.update { it.copy(isSummaryLoading = false, error = "No hay nada que pagar") }
        }
    }

    private fun loadAppointment(appointmentId: String) {
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appointment ->
                    val expired = appointment.paymentDeadline?.let { it <= Clock.System.now() } ?: false
                    // A held appointment awaiting payment lands on the "Pago Pendiente"
                    // screen with a live countdown; an expired hold falls back to the form.
                    val pending = appointment.status == AppointmentStatus.PENDING_PAYMENT && !expired
                    _state.update {
                        it.copy(
                            appointment = appointment,
                            isExpired = expired,
                            paymentStatus = if (pending) PaymentStatus.PENDING else it.paymentStatus,
                            error = if (expired) "El plazo de pago ha expirado" else it.error,
                        )
                    }
                    if (pending) startCountdown()
                    getDoctorDetail(appointment.doctorId)
                        .onSuccess { doctor ->
                            _state.update { it.copy(doctor = doctor, isSummaryLoading = false) }
                        }
                        .onFailure { err ->
                            _state.update { it.copy(isSummaryLoading = false, error = err.toUserMessage("No se pudo cargar el doctor")) }
                        }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSummaryLoading = false, error = err.toUserMessage("No se pudo cargar la cita")) }
                }
        }
    }

    private fun loadPackage(therapyPackageId: String) {
        scope.launch {
            getPackageDetail(therapyPackageId)
                .onSuccess { (pkg, _) ->
                    _state.update { it.copy(therapyPackage = pkg, isSummaryLoading = false) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSummaryLoading = false, error = err.toUserMessage("No se pudo cargar el paquete")) }
                }
        }
    }

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
    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    // ── Payment submission ────────────────────────────────────────────────────

    override fun onSubmit() {
        val s = _state.value
        if (appointmentId == null && therapyPackageId == null) {
            // Misconfigured route: nothing to pay for. Fail safe rather than crash on a money flow.
            _state.update { it.copy(isLoading = false, error = "No hay nada que pagar") }
            return
        }
        if (s.isExpired) {
            _state.update { it.copy(error = "El plazo de pago ha expirado") }
            return
        }
        val pan = s.cardNumber.filter { it.isDigit() }
        val expiryParts = s.expiry.split("/")
        val expMonth = expiryParts.getOrNull(0)?.trim()?.toIntOrNull() ?: run {
            _state.update { it.copy(error = "Invalid expiry") }
            return
        }
        val expYear = expiryParts.getOrNull(1)?.trim()?.toIntOrNull() ?: run {
            _state.update { it.copy(error = "Invalid expiry") }
            return
        }
        if (pan.length < 16) { _state.update { it.copy(error = "Invalid card number") }; return }
        if (s.cvv.isBlank()) { _state.update { it.copy(error = "CVV is required") }; return }
        if (s.cardholderName.isBlank()) { _state.update { it.copy(error = "Cardholder name is required") }; return }

        _state.update { it.copy(isLoading = true, error = null) }
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
            val paymentResult = if (appointmentId != null) {
                processPayment(rawCard, appointmentId)
            } else {
                processPayment.payPackage(rawCard, therapyPackageId!!)
            }
            paymentResult
                .onSuccess { result ->
                    _state.update { it.copy(isLoading = false) }
                    if (appointmentId != null) {
                        telemetry?.track("payment_completed", mapOf("appointmentId" to appointmentId))
                        onOutput(PaymentComponent.Output.NavigateToSuccess(result.appointmentId))
                    } else {
                        telemetry?.track("payment_completed", mapOf("therapyPackageId" to therapyPackageId!!))
                        onOutput(PaymentComponent.Output.NavigateToPackages)
                    }
                }
                .onFailure { err ->
                    val expired = err is PaymentDeadlineExpiredException
                    val isInProcess = err.message?.contains("revisión", ignoreCase = true) == true ||
                        err.message?.contains("in_process", ignoreCase = true) == true
                    when {
                        isInProcess -> {
                            _state.update { it.copy(isLoading = false, paymentStatus = PaymentStatus.PROCESSING, error = null) }
                        }
                        else -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isExpired = it.isExpired || expired,
                                    paymentStatus = if (!expired) PaymentStatus.REJECTED else PaymentStatus.FORM,
                                    error = if (expired) "El plazo de pago ha expirado" else null,
                                )
                            }
                        }
                    }
                }
        }
    }

    override fun onBack() { onOutput(PaymentComponent.Output.Back) }

    // ── Payment outcome actions ───────────────────────────────────────────────

    /**
     * "Pagar ahora" on the Pago Pendiente screen — transitions to the card form
     * and starts the reservation countdown.
     */
    override fun onPayNow() {
        _state.update { it.copy(paymentStatus = PaymentStatus.FORM) }
        startCountdown()
    }

    /**
     * "Cancelar reserva" on the Pago Pendiente screen — calls the cancel API and
     * navigates back to the appointments list.
     *
     * Uses the loaded appointment's startsAt for the 3-day-advance check inside
     * [CancelAppointmentUseCase]. If the appointment is not yet loaded we navigate
     * away anyway so the user is not stuck.
     */
    override fun onCancelReservation() {
        if (appointmentId == null) {
            // Package flow has no held reservation to cancel — just navigate away.
            onOutput(PaymentComponent.Output.NavigateToPackages)
            return
        }
        val startsAt = _state.value.appointment?.startsAt
        scope.launch {
            if (startsAt != null) {
                cancelAppointment(appointmentId, startsAt, "Reserva cancelada por el paciente antes del pago")
            }
            onOutput(PaymentComponent.Output.NavigateToAppointments)
        }
    }

    /** "Volver" from a payment outcome screen — packages go to packages, appointments to appointments. */
    override fun onGoToAppointments() {
        if (appointmentId == null && therapyPackageId != null) {
            onOutput(PaymentComponent.Output.NavigateToPackages)
        } else {
            onOutput(PaymentComponent.Output.NavigateToAppointments)
        }
    }

    /** "Reintentar pago" — returns the UI to the card form. */
    override fun onRetryPayment() {
        _state.update { it.copy(paymentStatus = PaymentStatus.FORM, error = null) }
    }

    /** "Cambiar tarjeta" — clears card fields and returns to the form. */
    override fun onChangeCard() {
        _state.update {
            it.copy(
                paymentStatus = PaymentStatus.FORM,
                cardNumber = "",
                expiry = "",
                cvv = "",
                cardholderName = "",
                docNumber = "",
                cardType = CardType.UNKNOWN,
                error = null,
            )
        }
    }

    // ── Countdown (Pago Pendiente 30-min timer) ───────────────────────────────

    /**
     * Starts a 1-second-tick countdown that decrements [PaymentState.secondsRemaining].
     * Mirrors the cooldown pattern in DefaultAssistantChatComponent.startCooldown.
     *
     * If the appointment already has a `paymentDeadline`, the initial seconds are
     * calculated from it. Otherwise, [DEFAULT_PENDING_SECONDS] (30 min) is used
     * and the gap is documented: no backend field exposes remaining seconds at this point.
     */
    internal fun startCountdown(initialSeconds: Int = resolveInitialSeconds()) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            var remaining = initialSeconds
            while (remaining > 0) {
                _state.update { it.copy(secondsRemaining = remaining) }
                delay(1_000L)
                remaining--
            }
            // Deadline reached — mark expired
            _state.update {
                it.copy(
                    secondsRemaining = 0,
                    isExpired = true,
                    paymentStatus = PaymentStatus.FORM,
                    error = "El plazo de pago ha expirado",
                )
            }
        }
    }

    private fun resolveInitialSeconds(): Int {
        val deadline = _state.value.appointment?.paymentDeadline ?: return DEFAULT_PENDING_SECONDS
        val nowEpochSeconds = Clock.System.now().epochSeconds
        val remaining = (deadline.epochSeconds - nowEpochSeconds).toInt()
        return if (remaining > 0) remaining else 0
    }
}
