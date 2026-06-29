package com.inclinic.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface DoctorConfig {
    @Serializable data object Dashboard : DoctorConfig
    @Serializable data object Schedule : DoctorConfig
    @Serializable data class DailySchedule(val date: String) : DoctorConfig
    @Serializable data class AppointmentDetail(val appointmentId: String) : DoctorConfig
    @Serializable data class CompleteWithEvidence(val appointmentId: String) : DoctorConfig
    @Serializable data class PatientDetail(val patientId: String) : DoctorConfig
    @Serializable data class MedicalRecordsList(val patientId: String) : DoctorConfig
    @Serializable data class MedicalRecordEditor(val patientId: String, val appointmentId: String? = null) : DoctorConfig
    @Serializable data class EditMedicalRecord(val recordId: String) : DoctorConfig
    @Serializable data class Chat(val appointmentId: String) : DoctorConfig
    @Serializable data object ScheduleConfig : DoctorConfig
    @Serializable data object PriceConfig : DoctorConfig

    @Serializable data object Onboarding : DoctorConfig
    @Serializable data object Enviado : DoctorConfig
    @Serializable data object CorregirSolicitud : DoctorConfig
    @Serializable data object Profile : DoctorConfig
    @Serializable data object EditSpecialties : DoctorConfig
    @Serializable data object RequestSpecialty : DoctorConfig
    @Serializable data object MySpecialtyRequests : DoctorConfig
    @Serializable data object Patients : DoctorConfig
    @Serializable data object SearchPatient : DoctorConfig
    @Serializable data object Packages : DoctorConfig
    @Serializable data object CreatePackage : DoctorConfig
    @Serializable data class PackageDetail(val packageId: String) : DoctorConfig
    @Serializable data object ShareRequests : DoctorConfig
    @Serializable data object RequestShare : DoctorConfig
    @Serializable data object Messages : DoctorConfig
    @Serializable data class Conversation(val threadId: String) : DoctorConfig
    @Serializable data object Notifications : DoctorConfig
    @Serializable data object Income : DoctorConfig
    @Serializable data object Reviews : DoctorConfig
    @Serializable data object PublicProfile : DoctorConfig
    @Serializable data object Settings : DoctorConfig
    @Serializable data object RescheduleQueue : DoctorConfig
    @Serializable data class RequestReschedule(val appointmentId: String) : DoctorConfig
    @Serializable data class RespondModality(val requestId: String) : DoctorConfig
    @Serializable data class RespondPackageNegotiation(val negotiationId: String) : DoctorConfig
    @Serializable data object TherapyOffers : DoctorConfig
    @Serializable data object CreateTherapyOffer : DoctorConfig
    @Serializable data class EditPrescription(val prescriptionId: String) : DoctorConfig
    @Serializable data object DeleteAccount : DoctorConfig
    @Serializable data object NoShowQueue : DoctorConfig
    @Serializable data object ChangePassword : DoctorConfig
}
