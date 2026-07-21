package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.util.DetailLoadState
import com.inclinic.app.core.util.detailLoadState
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorDetail

interface AdminDoctorDetailComponent {
    val state: Value<AdminDoctorDetailState>

    fun onBack()

    /** Suspends the doctor's user account. [reason] must satisfy the same backend
     *  minimum (>= 10 chars) enforced client-side by [AdminSuspendUserState.canSubmit]. */
    fun onSuspend(reason: String)

    /** Reactivates a previously suspended doctor's user account. */
    fun onUnsuspend()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminDoctorDetailState(
    val detail: AdminDoctorDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** True while a suspend/unsuspend request is in flight. */
    val isSuspending: Boolean = false,
    /** Error from the last suspend/unsuspend attempt — separate from [error] (load error). */
    val suspendError: String? = null,
    /** True when [error] came from an HTTP 404 — see [com.inclinic.app.core.error.isNotFoundError]. */
    val notFound: Boolean = false,
)

fun AdminDoctorDetailState.toDetailLoadState(): DetailLoadState<AdminDoctorDetail> =
    detailLoadState(isLoading = isLoading, value = detail, error = error, notFound = notFound)
