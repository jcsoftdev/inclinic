package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem

/** Pre-defined suspension reasons shown as selectable option cards. */
enum class SuspendReason(val label: String, val detail: String) {
    Abuse(
        label  = "Abuso de plataforma",
        detail = "Uso indebido, fraude o comportamiento inapropiado",
    ),
    Payment(
        label  = "Riesgo de pago",
        detail = "Contracargos, deuda o incumplimiento de pago",
    ),
    Other(
        label  = "Otro motivo",
        detail = "Especificar en el campo de texto",
    );
}

interface AdminSuspendUserComponent {
    val state: Value<AdminSuspendUserState>

    fun onReasonSelected(reason: SuspendReason)
    fun onFreeTextChange(text: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        /** Suspension completed — pop back to list and refresh. */
        data class SuspendSuccess(val patientUserId: String) : Output
    }
}

data class AdminSuspendUserState(
    val patient: AdminPatientListItem,
    val selectedReason: SuspendReason? = null,
    val freeText: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    /**
     * Final reason string sent to backend.
     * Composed as: selected label + detail + optional free text for "Otro".
     * Always >= 10 chars when [canSubmit] is true.
     */
    val composedReason: String
        get() {
            val base = selectedReason?.let { "${it.label} — ${it.detail}" } ?: ""
            return if (selectedReason == SuspendReason.Other && freeText.isNotBlank()) {
                "$base: ${freeText.trim()}"
            } else base
        }

    /**
     * Enable "Continuar" only when:
     * - a reason is selected
     * - if "Otro", free text is non-blank
     * - composed reason is >= 10 chars (backend minimum)
     */
    val canSubmit: Boolean
        get() {
            if (selectedReason == null) return false
            if (selectedReason == SuspendReason.Other && freeText.isBlank()) return false
            return composedReason.length >= 10
        }
}
