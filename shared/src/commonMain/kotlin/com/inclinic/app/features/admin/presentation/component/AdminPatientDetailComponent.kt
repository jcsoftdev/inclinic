package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem

/**
 * Detail screen for an admin patient.
 *
 * NOTE — there is NO GET /api/admin/patients/:id endpoint. This component holds
 * the [AdminPatientListItem] passed in from the list. Rich fields not in the
 * list payload (LTV/gasto, medical records count, city) are omitted rather than faked.
 *
 * Fields shown from list item: name, email, tier, isSuspended, appointmentCount,
 * therapyPackageCount, lastLoginLabel, phone (if present).
 * Fields omitted (documented gap): LTV / gasto total, city, verified badge.
 */
interface AdminPatientDetailComponent {
    val state: Value<AdminPatientDetailState>

    fun onBack()
    fun onSuspend()
    fun onReactivate()

    sealed interface Output {
        data object Back : Output
        data class NavigateToSuspend(val patient: AdminPatientListItem) : Output
        /** Reactivate was called and succeeded — pop back to list with refresh signal. */
        data object ReactivateSuccess : Output
    }
}

data class AdminPatientDetailState(
    val patient: AdminPatientListItem,
    val isReactivating: Boolean = false,
    val reactivateError: String? = null,
)
