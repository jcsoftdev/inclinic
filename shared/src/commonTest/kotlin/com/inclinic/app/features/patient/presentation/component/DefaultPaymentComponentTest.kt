@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.CardToken
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.DoctorPlan
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.Review
import com.inclinic.app.core.model.TherapyOffer
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.core.port.YapeTokenizer
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.appointments.application.CancelAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import com.inclinic.app.features.patient.payment.application.PaymentDeadlineExpiredException
import com.inclinic.app.features.patient.payment.application.ProcessPaymentUseCase
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackageDetailUseCase
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testAppointment(paymentDeadline: kotlin.time.Instant? = null): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.PENDING_PAYMENT,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + 24.hours, endsAt = now + 25.hours,
        rescheduleCount = 0, paymentDeadline = paymentDeadline, notes = null, createdAt = now,
    )
}

private fun testDoctor(): Doctor = Doctor(
    id = "doc-1", fullName = "Dr. Ana Torres", email = "ana@test.com", photoUrl = null,
    specialties = emptyList(), plan = DoctorPlan.FREE, ratingAverage = 4.5, ratingsCount = 10,
    consultationFee = 120.0, homeVisitAvailable = false, virtualVisitAvailable = true,
    bio = "Especialista", isVerified = true, cmpLicense = "CMP-123",
)

private class FakePaymentAppointmentDataSource(
    private val appointment: Appointment? = testAppointment(),
    private val paymentResult: Result<PaymentResult> = Result.success(PaymentResult(appointmentId = "apt-1")),
    val cancelCalls: MutableList<String> = mutableListOf(),
) : AppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (appointment != null) Result.success(appointment) else Result.failure(Exception("Not found"))
    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun cancelAppointment(appointmentId: String, reason: String): Result<Unit> {
        cancelCalls += appointmentId
        return Result.success(Unit)
    }
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = paymentResult
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = paymentResult
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

private class FakePaymentDoctorDataSource(
    private val doctor: Doctor? = testDoctor(),
) : DoctorSearchDataSource {
    override suspend fun getDoctorById(doctorId: String): Result<Doctor> =
        if (doctor != null) Result.success(doctor) else Result.failure(Exception("Not found"))
    override suspend fun searchDoctors(filters: DoctorFilters, page: Int) =
        Result.success(PagedDoctors(emptyList(), false))
    override suspend fun getDoctorReviews(doctorId: String, page: Int) = Result.success(emptyList<Review>())
}

private class FakeCardTokenizer(
    private val result: Result<CardToken> = Result.success(CardToken(token = "tok-123", last4 = "4242", brand = "visa")),
) : CardTokenizer {
    override suspend fun tokenize(card: RawCard): Result<CardToken> = result
}

private class FakeYapeTokenizer(
    private val result: Result<String> = Result.success("mp_stub_yape_4321"),
) : YapeTokenizer {
    override suspend fun tokenize(phoneNumber: String, otp: String): Result<String> = result
}

private class FakePaymentTherapyPackageDataSource(
    private val detail: Result<Pair<TherapyPackage, List<PackageSession>>> = Result.failure(Exception("Not found")),
) : TherapyPackageDataSource {
    override suspend fun getPatientPackages(patientId: String, status: String?) = Result.success(emptyList<TherapyPackage>())
    override suspend fun getPackageDetail(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> = detail
    override suspend fun getOffers(doctorId: String?) = Result.success(emptyList<TherapyOffer>())
    override suspend fun getOfferDetail(offerId: String): Result<TherapyOffer> = Result.failure(UnsupportedOperationException())
    override suspend fun getNegotiation(negotiationId: String): Result<PackageNegotiation> = Result.failure(UnsupportedOperationException())
    override suspend fun createNegotiation(offerId: String, pricePerSession: Double, sessions: Int, message: String?): Result<PackageNegotiation> = Result.failure(UnsupportedOperationException())
    override suspend fun respondNegotiation(negotiationId: String, action: String, pricePerSession: Double?, sessions: Int?, message: String?): Result<String?> = Result.failure(UnsupportedOperationException())
    override suspend fun purchasePackage(offerId: String): Result<String> = Result.failure(UnsupportedOperationException())
    override suspend fun getPackageStatement(packageId: String): Result<com.inclinic.app.core.model.PackageStatement> =
        Result.failure(UnsupportedOperationException())
    override suspend fun payPackageInstallment(packageId: String, amount: Double): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

class DefaultPaymentComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        apptDataSource: AppointmentDataSource = FakePaymentAppointmentDataSource(),
        doctorDataSource: DoctorSearchDataSource = FakePaymentDoctorDataSource(),
        cardTokenizer: CardTokenizer = FakeCardTokenizer(),
        yapeTokenizer: YapeTokenizer = FakeYapeTokenizer(),
        outputs: MutableList<PaymentComponent.Output> = mutableListOf(),
    ): DefaultPaymentComponent {
        return DefaultPaymentComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            therapyPackageId = null,
            processPayment = ProcessPaymentUseCase(cardTokenizer, yapeTokenizer, apptDataSource, dispatchers),
            getAppointmentDetail = GetAppointmentDetailUseCase(apptDataSource, dispatchers),
            getDoctorDetail = GetDoctorDetailUseCase(doctorDataSource, dispatchers),
            cancelAppointment = CancelAppointmentUseCase(apptDataSource, dispatchers),
            getPackageDetail = GetTherapyPackageDetailUseCase(FakePaymentTherapyPackageDataSource(), dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_appointment_and_doctor() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isSummaryLoading)
        assertNotNull(state.appointment)
        assertEquals("apt-1", state.appointment?.id)
        assertNotNull(state.doctor)
        assertEquals("Dr. Ana Torres", state.doctor?.fullName)
        assertNull(state.error)
    }

    @Test
    fun load_appointment_failure_sets_error() = runTest {
        val ds = FakePaymentAppointmentDataSource(appointment = null)
        val component = createComponent(apptDataSource = ds)

        assertFalse(component.state.value.isSummaryLoading)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onCardNumberChange_formats_and_detects_visa() = runTest {
        val component = createComponent()

        component.onCardNumberChange("4111111111111111")

        val state = component.state.value
        assertEquals("4111 1111 1111 1111", state.cardNumber)
        assertEquals(CardType.VISA, state.cardType)
    }

    @Test
    fun onCardNumberChange_detects_mastercard() = runTest {
        val component = createComponent()

        component.onCardNumberChange("5500000000000004")

        assertEquals(CardType.MASTERCARD, component.state.value.cardType)
    }

    @Test
    fun onCardNumberChange_unknown_prefix_sets_UNKNOWN_type() = runTest {
        val component = createComponent()

        component.onCardNumberChange("3714496353984312")

        assertEquals(CardType.UNKNOWN, component.state.value.cardType)
    }

    @Test
    fun onExpiryChange_updates_expiry() = runTest {
        val component = createComponent()

        component.onExpiryChange("12/27")

        assertEquals("12/27", component.state.value.expiry)
    }

    @Test
    fun onCvvChange_updates_cvv() = runTest {
        val component = createComponent()

        component.onCvvChange("123")

        assertEquals("123", component.state.value.cvv)
    }

    @Test
    fun onCardholderNameChange_updates_name() = runTest {
        val component = createComponent()

        component.onCardholderNameChange("Ana Torres")

        assertEquals("Ana Torres", component.state.value.cardholderName)
    }

    @Test
    fun onSubmit_success_emits_NavigateToSuccess() = runTest {
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmit()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is PaymentComponent.Output.NavigateToSuccess)
        assertEquals("apt-1", (output as PaymentComponent.Output.NavigateToSuccess).appointmentId)
    }

    @Test
    fun onSubmit_yape_success_emits_NavigateToSuccess() = runTest {
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onSelectMethod(PaymentMethodChoice.YAPE)
        component.onYapePhoneChange("987654321")
        component.onYapeOtpChange("123456")

        component.onSubmit()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is PaymentComponent.Output.NavigateToSuccess)
        assertEquals("apt-1", (output as PaymentComponent.Output.NavigateToSuccess).appointmentId)
    }

    @Test
    fun onSubmit_yape_short_otp_sets_error_and_does_not_navigate() = runTest {
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onSelectMethod(PaymentMethodChoice.YAPE)
        component.onYapePhoneChange("987654321")
        component.onYapeOtpChange("12")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onSubmit_invalid_card_sets_error() = runTest {
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onCardNumberChange("1234")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onSubmit_missing_cvv_sets_error() = runTest {
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertEquals("CVV is required", component.state.value.error)
    }

    @Test
    fun onSubmit_payment_failure_sets_error() = runTest {
        val failingDs = FakePaymentAppointmentDataSource(paymentResult = Result.failure(Exception("Payment declined")))
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(apptDataSource = failingDs, outputs = outputs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isLoading)
        // Non-expired payment failures now set REJECTED status instead of an error string
        assertEquals(PaymentStatus.REJECTED, component.state.value.paymentStatus)
        assertNull(component.state.value.error)
    }

    @Test
    fun load_expired_deadline_sets_isExpired_and_error() = runTest {
        val expiredAppt = testAppointment(paymentDeadline = Clock.System.now() - 1.hours)
        val ds = FakePaymentAppointmentDataSource(appointment = expiredAppt)
        val component = createComponent(apptDataSource = ds)

        val state = component.state.value
        assertTrue(state.isExpired)
        assertEquals("El plazo de pago ha expirado", state.error)
    }

    @Test
    fun onSubmit_when_expired_does_not_process_payment() = runTest {
        val expiredAppt = testAppointment(paymentDeadline = Clock.System.now() - 1.hours)
        val ds = FakePaymentAppointmentDataSource(appointment = expiredAppt)
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(apptDataSource = ds, outputs = outputs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertEquals("El plazo de pago ha expirado", component.state.value.error)
    }

    @Test
    fun onSubmit_deadline_expired_exception_sets_isExpired() = runTest {
        val failingDs = FakePaymentAppointmentDataSource(
            paymentResult = Result.failure(PaymentDeadlineExpiredException("El plazo de pago ha expirado")),
        )
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(apptDataSource = failingDs, outputs = outputs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertTrue(component.state.value.isExpired)
        assertEquals("El plazo de pago ha expirado", component.state.value.error)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(apptDataSource = FakePaymentAppointmentDataSource(appointment = null))
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PaymentComponent.Output.Back)
    }

    // ── Payment status transitions ─────────────────────────────────────────────

    @Test
    fun initial_paymentStatus_is_PENDING_for_pending_payment_appointment() = runTest {
        val component = createComponent()

        // PENDING_PAYMENT appointments now start in PENDING status (not FORM)
        assertEquals(PaymentStatus.PENDING, component.state.value.paymentStatus)
    }

    @Test
    fun onSubmit_payment_rejected_sets_REJECTED_status() = runTest {
        val failingDs = FakePaymentAppointmentDataSource(
            paymentResult = Result.failure(Exception("Fondos insuficientes")),
        )
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(apptDataSource = failingDs, outputs = outputs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmit()

        assertEquals(PaymentStatus.REJECTED, component.state.value.paymentStatus)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onSubmit_payment_in_process_sets_PROCESSING_status() = runTest {
        val inProcessDs = FakePaymentAppointmentDataSource(
            paymentResult = Result.failure(Exception("Pago en revisión. Te notificaremos cuando se confirme.")),
        )
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(apptDataSource = inProcessDs, outputs = outputs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmit()

        assertEquals(PaymentStatus.PROCESSING, component.state.value.paymentStatus)
        assertNull(component.state.value.error)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onRetryPayment_resets_status_to_FORM() = runTest {
        val failingDs = FakePaymentAppointmentDataSource(
            paymentResult = Result.failure(Exception("Tarjeta rechazada")),
        )
        val component = createComponent(apptDataSource = failingDs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")
        component.onSubmit()
        assertEquals(PaymentStatus.REJECTED, component.state.value.paymentStatus)

        component.onRetryPayment()

        assertEquals(PaymentStatus.FORM, component.state.value.paymentStatus)
        assertNull(component.state.value.error)
    }

    @Test
    fun onChangeCard_resets_status_and_clears_card_fields() = runTest {
        val failingDs = FakePaymentAppointmentDataSource(
            paymentResult = Result.failure(Exception("CVV inválido")),
        )
        val component = createComponent(apptDataSource = failingDs)
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")
        component.onSubmit()
        assertEquals(PaymentStatus.REJECTED, component.state.value.paymentStatus)

        component.onChangeCard()

        val state = component.state.value
        assertEquals(PaymentStatus.FORM, state.paymentStatus)
        assertEquals("", state.cardNumber)
        assertEquals("", state.cvv)
        assertEquals("", state.cardholderName)
        assertEquals(CardType.UNKNOWN, state.cardType)
        assertNull(state.error)
    }

    @Test
    fun onGoToAppointments_emits_NavigateToAppointments() = runTest {
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onGoToAppointments()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PaymentComponent.Output.NavigateToAppointments)
    }

    @Test
    fun onPayNow_sets_status_to_FORM() = runTest {
        val component = createComponent()
        // Manually push to a non-FORM status to verify the transition
        component.onRetryPayment() // noop from FORM, but now set up via a roundtrip
        val failingDs = FakePaymentAppointmentDataSource(
            paymentResult = Result.failure(Exception("Tarjeta rechazada")),
        )
        val pendingComponent = createComponent(apptDataSource = failingDs)
        pendingComponent.onCardNumberChange("4111111111111111")
        pendingComponent.onExpiryChange("12/27")
        pendingComponent.onCvvChange("123")
        pendingComponent.onCardholderNameChange("Ana Torres")
        pendingComponent.onSubmit()
        assertEquals(PaymentStatus.REJECTED, pendingComponent.state.value.paymentStatus)

        pendingComponent.onPayNow()

        assertEquals(PaymentStatus.FORM, pendingComponent.state.value.paymentStatus)
    }

    @Test
    fun onCancelReservation_emits_NavigateToAppointments() = runTest {
        // Use an appointment far in the future so the 3-day check passes
        val farAppt = testAppointment(paymentDeadline = Clock.System.now() + 30.minutes).copy(
            startsAt = Clock.System.now() + 24.hours * 5,
            endsAt = Clock.System.now() + 24.hours * 5 + 1.hours,
        )
        val ds = FakePaymentAppointmentDataSource(appointment = farAppt)
        val outputs = mutableListOf<PaymentComponent.Output>()
        val component = createComponent(apptDataSource = ds, outputs = outputs)

        component.onCancelReservation()

        assertTrue(outputs.any { it is PaymentComponent.Output.NavigateToAppointments })
    }

    // ── Countdown timer ───────────────────────────────────────────────────────

    @Test
    fun countdown_decrements_secondsRemaining_each_second() = runTest {
        val dispatchers = TestAppDispatchers(scheduler = testScheduler, useStandard = true)
        val lifecycle = LifecycleRegistry().also { it.resume() }
        val ctx = DefaultComponentContext(lifecycle = lifecycle)
        // Use CONFIRMED appointment so loadAppointment() does NOT trigger auto-countdown
        val ds = FakePaymentAppointmentDataSource(
            appointment = testAppointment().copy(status = AppointmentStatus.CONFIRMED),
        )
        val component = DefaultPaymentComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            therapyPackageId = null,
            processPayment = ProcessPaymentUseCase(FakeCardTokenizer(), FakeYapeTokenizer(), ds, dispatchers),
            getAppointmentDetail = GetAppointmentDetailUseCase(ds, dispatchers),
            getDoctorDetail = GetDoctorDetailUseCase(FakePaymentDoctorDataSource(), dispatchers),
            cancelAppointment = CancelAppointmentUseCase(ds, dispatchers),
            getPackageDetail = GetTherapyPackageDetailUseCase(FakePaymentTherapyPackageDataSource(), dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        component.startCountdown(initialSeconds = 3)
        advanceTimeBy(1_001L)
        assertEquals(2, component.state.value.secondsRemaining)

        advanceTimeBy(1_000L)
        assertEquals(1, component.state.value.secondsRemaining)
    }

    @Test
    fun countdown_sets_expired_when_reaches_zero() = runTest {
        val dispatchers = TestAppDispatchers(scheduler = testScheduler, useStandard = true)
        val lifecycle = LifecycleRegistry().also { it.resume() }
        val ctx = DefaultComponentContext(lifecycle = lifecycle)
        // Use CONFIRMED appointment so loadAppointment() does NOT trigger auto-countdown
        val ds = FakePaymentAppointmentDataSource(
            appointment = testAppointment().copy(status = AppointmentStatus.CONFIRMED),
        )
        val component = DefaultPaymentComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            therapyPackageId = null,
            processPayment = ProcessPaymentUseCase(FakeCardTokenizer(), FakeYapeTokenizer(), ds, dispatchers),
            getAppointmentDetail = GetAppointmentDetailUseCase(ds, dispatchers),
            getDoctorDetail = GetDoctorDetailUseCase(FakePaymentDoctorDataSource(), dispatchers),
            cancelAppointment = CancelAppointmentUseCase(ds, dispatchers),
            getPackageDetail = GetTherapyPackageDetailUseCase(FakePaymentTherapyPackageDataSource(), dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        component.startCountdown(initialSeconds = 2)
        advanceTimeBy(3_000L)

        val state = component.state.value
        assertTrue(state.isExpired)
        assertEquals("El plazo de pago ha expirado", state.error)
        assertEquals(0, state.secondsRemaining)
    }

    @Test
    fun countdown_cancels_previous_when_restarted() = runTest {
        val dispatchers = TestAppDispatchers(scheduler = testScheduler, useStandard = true)
        val lifecycle = LifecycleRegistry().also { it.resume() }
        val ctx = DefaultComponentContext(lifecycle = lifecycle)
        // Use CONFIRMED appointment so loadAppointment() does NOT trigger auto-countdown
        val ds = FakePaymentAppointmentDataSource(
            appointment = testAppointment().copy(status = AppointmentStatus.CONFIRMED),
        )
        val component = DefaultPaymentComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            therapyPackageId = null,
            processPayment = ProcessPaymentUseCase(FakeCardTokenizer(), FakeYapeTokenizer(), ds, dispatchers),
            getAppointmentDetail = GetAppointmentDetailUseCase(ds, dispatchers),
            getDoctorDetail = GetDoctorDetailUseCase(FakePaymentDoctorDataSource(), dispatchers),
            cancelAppointment = CancelAppointmentUseCase(ds, dispatchers),
            getPackageDetail = GetTherapyPackageDetailUseCase(FakePaymentTherapyPackageDataSource(), dispatchers),
            dispatchers = dispatchers,
            onOutput = {},
        )

        component.startCountdown(initialSeconds = 100)
        advanceTimeBy(500L)
        // Restart with a smaller value — previous job should be cancelled
        component.startCountdown(initialSeconds = 5)
        advanceTimeBy(1_001L)

        // Should be tracking the new countdown (4), not the old one (99)
        assertEquals(4, component.state.value.secondsRemaining)
    }
}
