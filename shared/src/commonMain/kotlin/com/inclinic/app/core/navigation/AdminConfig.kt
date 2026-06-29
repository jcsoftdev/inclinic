package com.inclinic.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AdminConfig {
    @Serializable data object Dashboard : AdminConfig
    @Serializable data object Notifications : AdminConfig

    // Citas tab
    @Serializable data object Appointments : AdminConfig
    @Serializable data class AppointmentDetail(val appointmentId: String) : AdminConfig

    // Doctores tab
    @Serializable data object Doctors : AdminConfig
    @Serializable data class DoctorDetail(val doctorId: String) : AdminConfig
    @Serializable data object PendingDoctors : AdminConfig
    @Serializable data class PendingDoctorDetail(val doctorId: String) : AdminConfig

    // Disputas tab
    @Serializable data object Disputes : AdminConfig
    @Serializable data class ResolveDispute(val disputeId: String) : AdminConfig
    @Serializable data class ResolveNoShow(val noShowId: String) : AdminConfig

    // Finance screen (reached from Dashboard)
    @Serializable data object Finance : AdminConfig

    // Placeholder destinations for tabs not yet implemented in this increment.
    @Serializable data object DoctorApprovals : AdminConfig

    // Más tab — hub menu + lane configs
    @Serializable data object More : AdminConfig           // root: MasMenu hub
    @Serializable data object MasPatients : AdminConfig
    /**
     * Patient detail — carries the list-item payload because there is no by-id endpoint.
     * All fields are primitives for Kotlinx serialization compatibility.
     */
    @Serializable data class MasPatientDetail(
        val id: String,
        val userId: String,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String?,
        val isSuspended: Boolean,
        val suspendedAt: String?,
        val suspensionReason: String?,
        val subscriptionTier: String,
        val lastLogin: String?,
        val createdAt: String?,
        val appointmentCount: Int,
        val therapyPackageCount: Int,
    ) : AdminConfig
    /** Suspend screen — carries patient id, userId and name only. */
    @Serializable data class MasSuspendUser(
        val id: String,
        val userId: String,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String?,
        val isSuspended: Boolean,
        val suspendedAt: String?,
        val suspensionReason: String?,
        val subscriptionTier: String,
        val lastLogin: String?,
        val createdAt: String?,
        val appointmentCount: Int,
        val therapyPackageCount: Int,
    ) : AdminConfig
    @Serializable data object MasSpecialties : AdminConfig
    @Serializable data object MasSpecialtyRequests : AdminConfig
    @Serializable data object MasReports : AdminConfig
    /**
     * Resolver detail — carries report payload as flat serializable primitives.
     * There is no by-id GET; the list item has all fields needed.
     */
    @Serializable data class MasResolveReport(
        val reportId: String,
        val reportStatus: String,
        val category: String,
        val reason: String,
        val reportedUserFirstName: String,
        val reportedUserLastName: String,
        val reportedUserRole: String,
        val createdAt: String?,
    ) : AdminConfig
    @Serializable data object MasReviews : AdminConfig
    @Serializable data object MasBlockedEmails : AdminConfig
    @Serializable data object MasSubscriptions : AdminConfig
    @Serializable data object MasProfile : AdminConfig
    @Serializable data object MasNotifications : AdminConfig
    @Serializable data object MasConfig : AdminConfig      // gear → Configuración
    /** 2FA security screen — reachable from Más tab. */
    @Serializable data object MasSecurity : AdminConfig
    /** 2FA setup screen — pushed from MasSecurity when 2FA is not enabled. */
    @Serializable data object MasTwoFactorSetup : AdminConfig
}
