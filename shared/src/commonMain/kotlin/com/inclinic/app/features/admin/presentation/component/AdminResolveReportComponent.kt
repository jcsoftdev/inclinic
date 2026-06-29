package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Decision options shown in the Resolver screen.
 *
 * Maps to resolve body status values:
 *   Reviewed    → REVIEWED
 *   Dismissed   → DISMISSED
 *   ActionTaken → ACTION_TAKEN
 */
enum class ReportDecision(
    val label: String,
    val description: String,
    val apiStatus: String,
) {
    Reviewed("Marcar revisado", "Visto, sin sanción", "REVIEWED"),
    Dismissed("Descartar", "Reporte infundado", "DISMISSED"),
    ActionTaken("Tomar acción", "Advertir o suspender al usuario", "ACTION_TAKEN"),
}

interface AdminResolveReportComponent {
    val state: Value<AdminResolveReportState>

    fun onSelectDecision(decision: ReportDecision)
    fun onAdminNoteChange(note: String)
    fun onConfirm()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object ResolvedSuccess : Output
    }
}

data class AdminResolveReportState(
    // Report fields passed in from list (flat, serializable via config)
    val reportId: String,
    val reportStatus: String,
    val category: String,
    val reason: String,
    val reportedUserFirstName: String,
    val reportedUserLastName: String,
    val reportedUserRole: String,
    val createdAt: String?,
    // Resolve form
    val selectedDecision: ReportDecision? = null,
    val adminNote: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
) {
    val reportedUserFullName: String get() = "$reportedUserFirstName $reportedUserLastName"
    val reportedUserRoleLabel: String
        get() = when (reportedUserRole) {
            "DOCTOR" -> "Doctor"
            "PATIENT" -> "Paciente"
            "SUPER_ADMIN" -> "Admin"
            else -> reportedUserRole
        }
    val categoryLabel: String
        get() = when (category) {
            "spam" -> "Spam"
            "abuse" -> "Abuso"
            "fraud" -> "Fraude"
            else -> "Otro"
        }
    val canConfirm: Boolean
        get() = selectedDecision != null && !isSubmitting
    val idShort: String
        get() = reportId.takeLast(4)
}
