package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal

interface AppointmentDataSource {
    suspend fun getAvailability(doctorId: String, date: String): Result<List<AvailabilitySlot>>
    suspend fun getMonthAvailability(doctorId: String, month: String): Result<Map<String, String>>
    suspend fun createAppointment(
        doctorId: String,
        date: String,
        slotId: String,
        visitType: String,
        notes: String?,
        homeVisitAddress: String? = null,
        homeVisitLat: Double? = null,
        homeVisitLng: Double? = null,
    ): Result<Appointment>
    suspend fun getPatientAppointments(patientId: String, status: String?, page: Int): Result<List<Appointment>>
    suspend fun getAppointmentById(appointmentId: String): Result<Appointment>
    suspend fun cancelAppointment(appointmentId: String, reason: String): Result<Unit>
    suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment>
    suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult>
    suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult>
    suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?>
    suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?): Result<Unit>
    suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>): Result<Unit>
    suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?): Result<Unit>
    suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?): Result<Unit>
}
