package com.inclinic.app.core.navigation

import com.inclinic.app.core.model.HistoryAccessLog
import kotlinx.serialization.Serializable

@Serializable
sealed interface PatientConfig {
    @Serializable data object Home : PatientConfig
    @Serializable data object Search : PatientConfig
    @Serializable data class DoctorProfile(val doctorId: String) : PatientConfig
    @Serializable data class ConsultType(val doctorId: String) : PatientConfig
    @Serializable data class Availability(val doctorId: String, val consultType: String) : PatientConfig
    @Serializable data class Booking(val doctorId: String, val slotId: String, val date: String, val consultType: String = "office", val startTime: String = "") : PatientConfig
    @Serializable data class Payment(val appointmentId: String? = null, val therapyPackageId: String? = null) : PatientConfig
    @Serializable data object Appointments : PatientConfig
    @Serializable data class AppointmentDetail(val appointmentId: String) : PatientConfig
    @Serializable data class CancelAppointment(val appointmentId: String) : PatientConfig
    @Serializable data class Chat(val doctorId: String, val doctorName: String) : PatientConfig
    @Serializable data object MedicalHistory : PatientConfig
    @Serializable data object Profile : PatientConfig
    @Serializable data class RescheduleResponse(val appointmentId: String) : PatientConfig
    @Serializable data object AssistantChat : PatientConfig

    // Appointment lifecycle
    @Serializable data class RescheduleAppointment(val appointmentId: String) : PatientConfig
    @Serializable data class ChangeVisitType(val appointmentId: String) : PatientConfig
    @Serializable data class DisputeAppointment(val appointmentId: String) : PatientConfig
    @Serializable data class ConfirmRating(val appointmentId: String) : PatientConfig

    // Messages & Notifications
    @Serializable data object MessagesList : PatientConfig
    @Serializable data object Notifications : PatientConfig
    @Serializable data object Settings : PatientConfig

    // Medical history deep screens
    @Serializable data class MedicalRecordDetail(val recordId: String) : PatientConfig
    @Serializable data class PrescriptionDetail(val prescriptionId: String) : PatientConfig
    @Serializable data object HistoryAccessLogs : PatientConfig
    @Serializable data class HistoryAccessLogDetail(val entry: HistoryAccessLog) : PatientConfig
    @Serializable data object ShareRequests : PatientConfig
    @Serializable data class ApproveShareRequest(val requestId: String) : PatientConfig

    // Symptom analysis
    @Serializable data object SymptomInput : PatientConfig
    @Serializable data class SymptomResults(val symptoms: String) : PatientConfig

    // Therapy packages
    @Serializable data object TherapyPackages : PatientConfig
    @Serializable data class TherapyPackageDetail(val packageId: String) : PatientConfig
    @Serializable data object TherapyOffers : PatientConfig
    @Serializable data class Negotiation(val negotiationId: String? = null, val offerId: String? = null) : PatientConfig

    // Moderation
    @Serializable data class ReportUser(val userId: String, val userName: String) : PatientConfig
    @Serializable data class BlockUser(val userId: String, val userName: String) : PatientConfig

    // Profile & account management
    @Serializable data object EditProfile : PatientConfig
    @Serializable data object ClinicalProfile : PatientConfig
    @Serializable data object ChangePassword : PatientConfig
    @Serializable data object DeleteAccount : PatientConfig
    @Serializable data object Membership : PatientConfig

    // Sharing management
    @Serializable data object ActiveAccesses : PatientConfig
}
